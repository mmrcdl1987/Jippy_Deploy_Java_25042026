	package com.jippy.foodandmart.mapper;

    import com.jippy.foodandmart.constants.AppConstants;
    import com.jippy.foodandmart.dto.MerchantRequestDTO;
    import com.jippy.foodandmart.entity.*;

    import java.time.LocalDateTime;

	/**
	 * Static utility class for mapping merchant-related DTOs to JPA entities.
	 * All field mapping lives here; business validation lives in the service layer.
	 */
	public final class MerchantMapper {
	
	    private MerchantMapper() {}
	
	    public static Merchant toEntity(MerchantRequestDTO dto) {
	        String fullName = dto.getFirstName().trim() + " " + dto.getLastName().trim();
	        Merchant merchant = new Merchant();
	        merchant.setMerchantName(fullName);
	        merchant.setMerchantEmail(dto.getEmail().toLowerCase().trim());
	        merchant.setMerchantPhone(dto.getPhone().trim());
	        merchant.setMerchantBusinessType(dto.getOutletType());
	        merchant.setIsActive(AppConstants.FLAG_YES);
	        merchant.setStatus(AppConstants.STATUS_PENDING);
	        merchant.setIsApproved(Boolean.valueOf(AppConstants.UN_APPROVED));
	        return merchant;
	    }
	
	    public static MerchantKyc toKycEntity(MerchantRequestDTO dto, Merchant merchant) {
	        MerchantKyc kyc = new MerchantKyc();
	        kyc.setMerchant(merchant);
	        kyc.setAadhaarNumber(dto.getAdhar() != null ? dto.getAdhar().trim() : null);
	        kyc.setPanNumber(dto.getPan() != null ? dto.getPan().toUpperCase().trim() : null);
	        kyc.setFssaiNumber(dto.getFssai() != null ? dto.getFssai().trim() : null);
	        kyc.setVerified(Boolean.valueOf(AppConstants.UN_APPROVED));
	        return kyc;
	    }
	
	    public static MerchantBankDetails toBankEntity(MerchantRequestDTO dto, Integer merchantId) {
	        MerchantBankDetails bank = new MerchantBankDetails();
	        bank.setAccountNumber(dto.getAccountNumber());
	        bank.setBankName(dto.getBankLocation());
	        bank.setAccountHolderName(dto.getNameInBankAccount());
	        bank.setIfscCode(dto.getIfscCode() != null ? dto.getIfscCode().toUpperCase().trim() : null);
	        bank.setUserType(AppConstants.TYPE_MERCHANT);
	        bank.setRecipientId(merchantId);
	        return bank;
	    }
	
	    public static Employee toEmployeeEntity(MerchantRequestDTO dto) {
	        Employee employee = new Employee();
	        String fullName = dto.getFirstName().trim() + " " + dto.getLastName().trim();
	        employee.setEmployeeName(fullName);
	        employee.setEmail(dto.getEmail().toLowerCase().trim());
	        employee.setMobileNumber(dto.getPhone().trim());
	        employee.setIsActive(AppConstants.FLAG_YES);
	        return employee;
	    }
	
	    public static User toUserEntity(String userName, String password, Integer merchantId) {
	        User user = new User();
	        user.setUsername(userName);
	        user.setPassword(password);
	        user.setEmployeeId(merchantId);
	        user.setUserType(AppConstants.TYPE_MERCHANT);
	        user.setIsActive(AppConstants.FLAG_YES);
	        return user;
	    }

		public static UserRolePermissions toUserRolesEntity(User user, RolePermissions rp) {

			UserRolePermissions urp = new UserRolePermissions();

			urp.setUserId(user.getUserId()); // correct user PK
			urp.setRolePermissionId(rp.getRolePermissionId()); // link to role_permissions
			urp.setCreatedAt(LocalDateTime.now());

			return urp;
		}

//		public static RolePerissions toRolePermissionsEntity(Roles role) {
//
//			RolePerissions rolePerissions= new RolePerissions();
//
//			return null;
//		}
	}
