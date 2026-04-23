		package com.jippy.foodandmart.service.impl;

        import com.jippy.foodandmart.constants.AppConstants;
        import com.jippy.foodandmart.dto.BulkUploadResultDTO;
        import com.jippy.foodandmart.dto.MerchantRequestDTO;
        import com.jippy.foodandmart.entity.*;
        import com.jippy.foodandmart.exception.MerchantAlreadyExistsException;
        import com.jippy.foodandmart.mapper.MerchantMapper;
        import com.jippy.foodandmart.repository.*;
        import com.jippy.foodandmart.service.interfaces.IMerchantService;
        import com.jippy.foodandmart.validation.FileParser;
        import jakarta.validation.ConstraintViolation;
        import jakarta.validation.Validator;
        import lombok.extern.slf4j.Slf4j;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.context.annotation.Lazy;
        import org.springframework.stereotype.Service;
        import org.springframework.transaction.annotation.Propagation;
        import org.springframework.transaction.annotation.Transactional;
        import org.springframework.web.multipart.MultipartFile;

        import java.util.ArrayList;
        import java.util.List;
        import java.util.Set;
		
		/**
		 * Service implementation for merchant onboarding and bulk CSV/Excel upload.
		 *
		 * <p>Layer order: Controller → IMerchantService → MerchantServiceImpl → MerchantMapper → Repository → Entity.</p>
		 *
		 * <p>Self-injection via {@code @Lazy} is required so that {@code bulkUpload} can route
		 * each row through the Spring AOP proxy, ensuring the {@code REQUIRES_NEW} transaction
		 * on {@code createMerchant} is honoured per row.</p>
		 */
		@Service
		@Slf4j
		public class MerchantServiceImpl implements IMerchantService {
		
		    private final MerchantRepository merchantRepository;
		    private final MerchantKycRepository merchantKycRepository;
		    private final MerchantBankDetailsRepository bankDetailsRepository;
		    private final UserRepository userRepository;
		    private final EmployeeRepository employeeRepository;
		    private final Validator validator;
		    private final UserRolesRepository userRolesRepository;
		    private final RoleRepository roleRepository;
		 	private final RolePermissionsRepository rolePermissionsRepository;
		    @Lazy
		    @Autowired
		    private IMerchantService self;
		
		    @Autowired
		    public MerchantServiceImpl(MerchantRepository merchantRepository,
		                               MerchantKycRepository merchantKycRepository,
		                               MerchantBankDetailsRepository bankDetailsRepository,
		                               UserRepository userRepository,
		                               EmployeeRepository employeeRepository,
		                               Validator validator,
		                               RoleRepository roleRepository,
		                               UserRolesRepository userRolesRepository,RolePermissionsRepository rolePermissionsRepository) {
		        this.merchantRepository    = merchantRepository;
		        this.merchantKycRepository = merchantKycRepository;
		        this.bankDetailsRepository = bankDetailsRepository;
		        this.userRepository        = userRepository;
		        this.employeeRepository    = employeeRepository;
		        this.validator             = validator;
		        this.roleRepository        = roleRepository;
		        this.userRolesRepository   = userRolesRepository;
				this.rolePermissionsRepository = rolePermissionsRepository;
		    }
		
		    // ── Queries ───────────────────────────────────────────────────────────────
		
		    @Override
		    public List<Merchant> getAllMerchants() {
		        return merchantRepository.findAll();
		    }
		
		    @Override
		    public Merchant getMerchantById(Integer id) {
		        return merchantRepository.findById(id)
		                .orElseThrow(() -> new IllegalArgumentException("Merchant ID " + id + " does not exist"));
		    }
		
		    @Override
		    public long countMerchants() {
		        long count = merchantRepository.count();
		        log.info("[MERCHANT] Count fetched: {}", count);
		        return count;
		    }
		
		    // ── Single Create ─────────────────────────────────────────────────────────
		
		    @Override
		    @Transactional(propagation = Propagation.REQUIRES_NEW)
		    public Merchant createMerchant(MerchantRequestDTO dto) {
		        log.info("[MERCHANT] Creating merchant: email={}, phone={}", dto.getEmail(), dto.getPhone());
		
		        validateUniqueness(dto.getEmail(), dto.getPhone(), dto.getPan(),
		                dto.getAdhar(), dto.getFssai(), dto.getAccountNumber());
		
		        Merchant merchant = MerchantMapper.toEntity(dto);
		        merchant = merchantRepository.save(merchant);
		        log.info("[MERCHANT] Saved: merchantId={}, name={}", merchant.getMerchantId(), merchant.getMerchantName());
		
		        saveKyc(dto, merchant);
		        saveBankDetails(dto, merchant.getMerchantId());
		        createMerchantUser(dto, merchant.getMerchantId());
		
		        log.info("[MERCHANT] Onboarding complete: merchantId={}", merchant.getMerchantId());
		        return merchant;
		    }
		
		    // ── Bulk Upload ───────────────────────────────────────────────────────────
		
		    @Override
		    public BulkUploadResultDTO bulkUpload(MultipartFile file) {
		        log.info("[BULK] Starting bulk upload: filename={}, size={} bytes",
		                file.getOriginalFilename(), file.getSize());
		
		        List<MerchantRequestDTO> rows;
		        try {
		            String filename = file.getOriginalFilename() != null
		                    ? file.getOriginalFilename().toLowerCase() : "";
		            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
		                rows = FileParser.parseExcel(file);
		            } else if (filename.endsWith(".csv")) {
		                rows = FileParser.parseCsv(file);
		            } else {
		                return buildErrorResult(0, "Unsupported file type. Only CSV and Excel files are allowed.");
		            }
		        } catch (Exception e) {
		            log.error("[BULK] File parsing failed: {}", e.getMessage(), e);
		            return buildErrorResult(0, "File parsing failed: " + e.getMessage());
		        }
		
		        log.info("[BULK] Parsed {} rows from file", rows.size());
		
		        int success = 0, failure = 0;
		        List<BulkUploadResultDTO.RowErrorDTO> errors = new ArrayList<>();
		
		        for (int i = 0; i < rows.size(); i++) {
		            MerchantRequestDTO dto = rows.get(i);
		            int rowNum = i + 2;
		            try {
		                Set<ConstraintViolation<MerchantRequestDTO>> violations = validator.validate(dto);
		                if (!violations.isEmpty()) {
		                    violations.forEach(v -> errors.add(BulkUploadResultDTO.RowErrorDTO.builder()
		                            .rowNumber(rowNum)
		                            .field(v.getPropertyPath().toString())
		                            .value(v.getInvalidValue() != null ? v.getInvalidValue().toString() : "")
		                            .reason(v.getMessage())
		                            .build()));
		                    failure++;
		                    continue;
		                }
		                Merchant merchant = self.createMerchant(dto);
		                success++;
		                log.info("[BULK] Row {} saved: merchantId={}", rowNum, merchant.getMerchantId());
		            } catch (MerchantAlreadyExistsException e) {
		                log.warn("[BULK] Row {} skipped (duplicate): {}", rowNum, e.getMessage());
		                errors.add(BulkUploadResultDTO.RowErrorDTO.builder()
		                        .rowNumber(rowNum).field("duplicate").value("").reason(e.getMessage()).build());
		                failure++;
		            } catch (Exception e) {
		                log.error("[BULK] Row {} failed: {}", rowNum, e.getMessage(), e);
		                errors.add(BulkUploadResultDTO.RowErrorDTO.builder()
		                        .rowNumber(rowNum).field("unknown").value("").reason(e.getMessage()).build());
		                failure++;
		            }
		        }
		
		        log.info("[BULK] Complete: total={}, success={}, failure={}", rows.size(), success, failure);
		        return BulkUploadResultDTO.builder()
		                .totalRows(rows.size()).successCount(success).failureCount(failure).errors(errors).build();
		    }
		
		    // ── Private Helpers ───────────────────────────────────────────────────────
		
		    private void validateUniqueness(String email, String phone, String pan,
		                                    String aadhaar, String fssai, String accountNumber) {
		        if (merchantRepository.existsByMerchantEmail(email))
		            throw new MerchantAlreadyExistsException("Email already registered: " + email);
		        if (merchantRepository.existsByMerchantPhone(phone))
		            throw new MerchantAlreadyExistsException("Phone already registered: " + phone);
		        if (pan != null && merchantKycRepository.existsByPanNumber(pan))
		            throw new MerchantAlreadyExistsException("PAN already registered: " + pan);
		        if (aadhaar != null && merchantKycRepository.existsByAadhaarNumber(aadhaar))
		            throw new MerchantAlreadyExistsException("Aadhaar already registered: " + aadhaar);
		        if (fssai != null && merchantKycRepository.existsByFssaiNumber(fssai))
		            throw new MerchantAlreadyExistsException("FSSAI already registered: " + fssai);
		        if (accountNumber != null && !accountNumber.isBlank()
		                && bankDetailsRepository.existsByAccountNumber(accountNumber))
		            throw new MerchantAlreadyExistsException("Account number already registered: " + accountNumber);
		    }
		
		    private void saveKyc(MerchantRequestDTO dto, Merchant merchant) {
		        MerchantKyc kyc = MerchantMapper.toKycEntity(dto, merchant);
		        merchantKycRepository.save(kyc);
		        log.info("[KYC] Saved for merchantId={}", merchant.getMerchantId());
		    }
		
		    private void saveBankDetails(MerchantRequestDTO dto, Integer merchantId) {
		        boolean hasBankData = (dto.getAccountNumber() != null && !dto.getAccountNumber().isBlank())
		                || (dto.getIfscCode() != null && !dto.getIfscCode().isBlank());
		        if (!hasBankData) return;
		
		        MerchantBankDetails bank = MerchantMapper.toBankEntity(dto, merchantId);
		        bankDetailsRepository.save(bank);
		        log.info("[BANK] Details saved for merchantId={}", merchantId);
		    }
		
		    private void createMerchantUser(MerchantRequestDTO dto, Integer merchantId) {
		        String phone      = dto.getPhone().trim();
		        String email      = dto.getEmail().toLowerCase().trim();
		        String phoneLast4 = phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone;
		        String emailFirst4 = email.length() >= 4 ? email.substring(0, 4) : email;
		
		        String username = dto.getFirstName().trim().toLowerCase().replaceAll("\\s+", "") + phoneLast4;
		        String password = emailFirst4 + phoneLast4;

//		        Roles role = roleRepository.findByRoleName(AppConstants.TYPE_MERCHANT);
//		        User user = MerchantMapper.toUserEntity(username, password, merchantId);
//		        userRepository.save(user);
//		       RolePerissions rolePerissions = MerchantMapper.toRolePermissionsEntity(role);
//
//
//				// UserRole userRoles = MerchantMapper.toUserRolesEntity(user, role);
//		        userRolesRepository.save(userRoles);
//		        log.info("[MERCHANT] Portal user created: username={}, merchantId={}", username, merchantId);
//		    }
				//  Save user
				User user = MerchantMapper.toUserEntity(username, password, merchantId);
				user = userRepository.save(user);

				// Fetch role
				Roles role = roleRepository.findByRoleName(AppConstants.TYPE_MERCHANT);
				if (role == null) {
					throw new RuntimeException("Role not found");
				}

				//  Fetch role_permissions
				List<RolePermissions> rolePermissionsList =
						rolePermissionsRepository.findByRoleId(role.getRoleId());

				if (rolePermissionsList.isEmpty()) {
					throw new RuntimeException("No permissions mapped to role");
				}

				//  Map user → role_permissions
				for (RolePermissions rp : rolePermissionsList) {

					UserRolePermissions urp =
							MerchantMapper.toUserRolesEntity(user, rp);

					userRolesRepository.save(urp);
				}

				log.info("[MERCHANT] Portal user created with permissions: {}", username);
			}
		
		    private BulkUploadResultDTO buildErrorResult(int rowNum, String reason) {
		        return BulkUploadResultDTO.builder()
		                .totalRows(0).successCount(0).failureCount(0)
		                .errors(List.of(BulkUploadResultDTO.RowErrorDTO.builder()
		                        .rowNumber(rowNum).field("file").value("").reason(reason).build()))
		                .build();
		    }
		}
