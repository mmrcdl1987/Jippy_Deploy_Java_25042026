package com.jippy.foodandmart.service.impl;

import com.jippy.foodandmart.constants.AppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.mapper.MerchantMapper;
import com.jippy.foodandmart.mapper.OutletMapper;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.interfaces.IOutletService;
import com.jippy.foodandmart.util.CredentialUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for outlet management.
 *
 * <p>Layer order: Controller → IOutletService → OutletServiceImpl → OutletMapper → Repository → Entity.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutletServiceImpl implements IOutletService {
    private final OutletRepository        outletRepository;
    private final OutletAddressRepository addressRepository;
    private final OutletDayRepository     dayRepository;
    private final MerchantRepository      merchantRepository;
    private final UserRepository          userRepository;
    private final EmployeeRepository      employeeRepository;
    private final StateRepository         stateRepository;
    private final AreaRepository          areaRepository;
    private final RoleRepository  roleRepository;
    private final UserRolesRepository userRolesRepository;
    private final RolePermissionsRepository rolePermissionsRepository;

    private static final GeometryFactory GEO_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public long countOutlets() {
        return outletRepository.count();
    }

    @Override
    public List<Outlet> getAllOutlets() {
        return outletRepository.findAll();
    }

    @Override
    public List<OutletSummaryDTO> getAllOutletsSummary() {
        List<Outlet> outlets = outletRepository.findAll();
        List<OutletSummaryDTO> result = new ArrayList<>();
        for (Outlet o : outlets) {
            OutletAddress addr = addressRepository.findByOutletId(o.getOutletId()).orElse(null);
            result.add(OutletSummaryDTO.from(o, 0, addr));
        }
        return result;
    }

    @Override
    public List<OutletSummaryDTO> getOutletsByMerchantId(Integer merchantId) {
        List<Outlet> outlets = outletRepository.findByMerchantId(merchantId);
        List<OutletSummaryDTO> result = new ArrayList<>();
        for (Outlet o : outlets) {
            OutletAddress addr = addressRepository.findByOutletId(o.getOutletId()).orElse(null);
            result.add(OutletSummaryDTO.from(o, 0, addr));
        }
        return result;
    }

    @Override
    public Outlet getOutletById(Integer id) {
        return outletRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Outlet ID " + id + " does not exist"));
    }

    // ── Single Create ─────────────────────────────────────────────────────────

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutletCreatedDTO createOutlet(OutletRequestDTO dto) {
        log.info("[OUTLET] Creating outlet: name={}, merchantId={}, phone={}",
                dto.getOutletName(), dto.getMerchantId(), dto.getOutletPhone());

        validateOutletRequest(dto);

        if (!merchantRepository.existsById(dto.getMerchantId()))
            throw new IllegalArgumentException("Merchant ID " + dto.getMerchantId() + " does not exist");
        if (outletRepository.existsByOutletPhone(dto.getOutletPhone()))
            throw new IllegalArgumentException("An outlet with phone " + dto.getOutletPhone() + " already exists");
        if (outletRepository.existsByMerchantIdAndOutletName(dto.getMerchantId(), dto.getOutletName()))
            throw new IllegalArgumentException("Outlet '" + dto.getOutletName() + "' already exists for this merchant");

        Point location = buildPoint(dto.getLatitude(), dto.getLongitude());
        Outlet outlet = OutletMapper.toEntity(dto);
        outlet.setOutletLocation(location);
        outlet = outletRepository.save(outlet);
        log.info("[OUTLET] Saved: outletId={}", outlet.getOutletId());

        saveAddress(dto, outlet.getOutletId());
        saveOperatingDays(dto, outlet.getOutletId());

        String loginId  = CredentialUtil.generateOutletLoginId(dto.getOutletName(), dto.getOutletPhone());
        String password = CredentialUtil.generateOutletPassword(dto.getOutletName(), dto.getOutletPhone());
        saveOutletUser(loginId, password, dto.getOutletPhone(), outlet.getOutletId(), outlet.getOutletName());
        log.info("[OUTLET] Onboarding complete: outletId={}, loginId={}", outlet.getOutletId(), loginId);

        return OutletMapper.toCreatedDTO(outlet, loginId, password);
    }

    // ── Bulk Upload ───────────────────────────────────────────────────────────

    @Override
    public BulkOutletResultDTO bulkUpload(List<OutletRequestDTO> rows) {
        int total = rows.size(), success = 0;
        List<BulkOutletResultDTO.OutletCredential> credentials = new ArrayList<>();
        List<BulkOutletResultDTO.OutletError>      errors      = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 3;
            OutletRequestDTO dto = rows.get(i);
            try {
                OutletCreatedDTO created = createOutlet(dto);
                success++;
                BulkOutletResultDTO.OutletCredential cred = new BulkOutletResultDTO.OutletCredential();
                cred.setOutletId(created.getOutletId());
                cred.setOutletName(created.getOutletName());
                cred.setOutletLoginId(created.getOutletLoginId());
                cred.setOutletPassword(created.getOutletPassword());
                credentials.add(cred);
            } catch (Exception e) {
                log.warn("[BULK] Row {} failed: {}", rowNum, e.getMessage());
                BulkOutletResultDTO.OutletError err = new BulkOutletResultDTO.OutletError();
                err.setRowNumber(rowNum);
                err.setOutletName(dto.getOutletName());
                err.setReason(e.getMessage());
                errors.add(err);
            }
        }

        BulkOutletResultDTO result = new BulkOutletResultDTO();
        result.setTotalRows(total);
        result.setSuccessCount(success);
        result.setFailureCount(total - success);
        result.setCredentials(credentials);
        result.setErrors(errors);
        return result;
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateOutletRequest(OutletRequestDTO dto) {
        List<String> errs = new ArrayList<>();

        if (isBlank(dto.getOutletName()))
            errs.add("Outlet name is required");
        else if (dto.getOutletName().length() > 100)
            errs.add("Outlet name must not exceed 100 characters");

        if (dto.getMerchantId() == null)
            errs.add("Merchant ID is required");

        if (isBlank(dto.getCuisineType()))
            errs.add("Cuisine type is required");
        else if (dto.getCuisineType().length() > 100)
            errs.add("Cuisine type must not exceed 100 characters");

        if (isBlank(dto.getOutletPhone()))
            errs.add("Outlet phone is required");
        else if (!dto.getOutletPhone().trim().matches("^[6-9]\\d{9}$"))
            errs.add("Outlet phone must be a valid 10-digit Indian mobile number, got: '"
                    + dto.getOutletPhone().trim() + "'");

        if (!isBlank(dto.getBuildingNumber()) || !isBlank(dto.getRoad())) {
            if (isBlank(dto.getAreaName())) {
                errs.add("Area name is required when address is provided");
            } else if (dto.getAreaName().trim().length() > 50) {
                errs.add("Area name must not exceed 50 characters");
            }
            if (!isBlank(dto.getBuildingNumber()) && dto.getBuildingNumber().trim().length() > 50)
                errs.add("Building number must not exceed 50 characters");
            if (!isBlank(dto.getRoad()) && dto.getRoad().trim().length() > 100)
                errs.add("Road must not exceed 100 characters");
            if (!isBlank(dto.getLandmark()) && dto.getLandmark().trim().length() > 150)
                errs.add("Landmark must not exceed 150 characters");
        }

        if (!errs.isEmpty())
            throw new IllegalArgumentException("Validation failed: " + String.join("; ", errs));
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Integer resolveStateId(String stateName) {
        if (isBlank(stateName))
            throw new IllegalArgumentException("State name is required");
        return stateRepository.findByStateNameIgnoreCase(stateName.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "State '" + stateName.trim() + "' not found in states table."))
                .getStateId();
    }

    /**
     * Resolves an area name (as supplied in the upload sheet's ZipCode column) to
     * the integer area_id PK stored in jippy_fm.area.
     *
     * <p>Why: the address table stores area_id (FK), not a free-text area name.
     * Upload sheets use a human-readable name; this lookup bridges that gap.</p>
     *
     * @param areaName the area name string from the upload row
     * @return the matching area_id integer
     * @throws IllegalArgumentException if the name is blank or not found in the area table
     */
    private Integer resolveAreaId(String areaName) {
        if (isBlank(areaName))
            throw new IllegalArgumentException("Area name is required");
        return areaRepository.findByAreaNameIgnoreCase(areaName.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Area '" + areaName.trim() + "' not found in area table."))
                .getAreaId();
    }

    private Point buildPoint(String latStr, String lonStr) {
        if (isBlank(latStr) || isBlank(lonStr)) return null;
        try {
            double lat = Double.parseDouble(latStr.trim());
            double lon = Double.parseDouble(lonStr.trim());
            Point p = GEO_FACTORY.createPoint(new Coordinate(lon, lat));
            p.setSRID(4326);
            return p;
        } catch (NumberFormatException e) {
            log.warn("[OUTLET] Could not parse lat/lon: lat='{}', lon='{}' — skipping", latStr, lonStr);
            return null;
        }
    }

    private void saveAddress(OutletRequestDTO dto, Integer outletId) {
        if (isBlank(dto.getBuildingNumber()) && isBlank(dto.getRoad())) return;
        Integer stateId = resolveStateId(dto.getStateName());
        Integer areaId  = resolveAreaId(dto.getAreaName());
        OutletAddress address = OutletMapper.toAddressEntity(dto, outletId, stateId, areaId);
        addressRepository.save(address);
        log.info("[OUTLET] Address saved for outletId={}", outletId);
    }

    private void saveOperatingDays(OutletRequestDTO dto, Integer outletId) {
        if (dto.getOperatingDays() == null || dto.getOperatingDays().isEmpty()) return;
        for (OutletDayDTO d : dto.getOperatingDays()) {
            if (d.getDayOfWeekId() == null) continue;
            boolean isEvening  = "evening".equalsIgnoreCase(d.getSlotType());
            LocalTime defOpen  = isEvening ? LocalTime.of(17, 0) : LocalTime.of(9, 0);
            LocalTime defClose = isEvening ? LocalTime.of(22, 0) : LocalTime.of(14, 0);

            OutletDay day = new OutletDay();
            day.setOutletId(outletId);
            day.setDayOfWeekId(d.getDayOfWeekId());
            day.setIsOpen(d.getIsOpen() != null ? d.getIsOpen() : true);
            day.setOpeningTime(parseTime(d.getOpeningTime(), defOpen));
            day.setClosingTime(parseTime(d.getClosingTime(), defClose));
            dayRepository.save(day);
        }
        log.info("[OUTLET] Operating slots saved for outletId={}", outletId);
    }

    private void saveOutletUser(String loginId, String password,
                                String phone, Integer outletId, String outletName) {
        String username = loginId;
        if (userRepository.existsByUsername(username)) {
            String base = username;
            int suffix = 1;
            while (userRepository.existsByUsername(username)) {
                username = base + suffix++;
            }
            log.warn("[OUTLET] Username collision resolved: final={}", username);
        }

//        Employee employee = new Employee();
//        employee.setEmployeeName(outletName);
//        employee.setEmail("outlet" + outletId + "@jippy.internal");
//        employee.setMobileNumber(phone);
//        employee.setIsActive(AppConstants.FLAG_YES);
//        employee = employeeRepository.save(employee);

//        User user = new User();
//        user.setUsername(username);
//        user.setPassword(password);
//        user.setEmployeeId(employee.getEmployeeId());
//        user.setUserType(AppConstants.TYPE_OUTLET);
//        user.setIsActive(AppConstants.FLAG_YES);
//        userRepository.save(user);
       // log.info("[OUTLET] User saved: username={}, outletId={}", username, outletId);
//        Roles role =roleRepository.findByRoleName(AppConstants.TYPE_OUTLET);
//
//        MerchantMapper.toUserRolesEntity()
        User users = MerchantMapper.toUserEntity(username, password, outletId);
        users = userRepository.save(users);
        log.info("[OUTLET] User saved: username={}, outletId={}", username, outletId);

        // Fetch role
        Roles role = roleRepository.findByRoleName(AppConstants.TYPE_OUTLET);
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
                    MerchantMapper.toUserRolesEntity(users, rp);

            userRolesRepository.save(urp);
        }

        log.info("[MERCHANT] Portal user created with permissions: {}", username);
    }


    private LocalTime parseTime(String s, LocalTime fallback) {
        if (s == null || s.isBlank()) return fallback;
        try {
            return s.length() == 5
                    ? LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm"))
                    : LocalTime.parse(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
