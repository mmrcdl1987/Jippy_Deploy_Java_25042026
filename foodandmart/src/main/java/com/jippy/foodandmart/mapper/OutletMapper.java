package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.OutletCreatedDTO;
import com.jippy.foodandmart.dto.OutletRequestDTO;
import com.jippy.foodandmart.entity.Outlet;
import com.jippy.foodandmart.entity.OutletAddress;

/**
 * Static utility class for converting between {@link OutletRequestDTO} /
 * {@link OutletCreatedDTO} and the {@link Outlet} / {@link OutletAddress} entities.
 *
 * <p>Why a separate mapper: keeps the service layer clean by moving all
 * field-level mapping logic into one testable place. The service only calls
 * {@code OutletMapper.toEntity(dto)} rather than manually setting each field.</p>
 */
public final class OutletMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private OutletMapper() {}

    // ── DTO → Entity ──────────────────────────────────────────────────────────

    /**
     * Converts an {@link OutletRequestDTO} into a new {@link Outlet} entity.
     *
     * <p>Why we use this: the Outlet entity has many fields and using setters
     * here prevents the service from growing field-by-field assignment blocks.
     * isActive defaults to "Y" because newly created outlets are immediately
     * available for menu and product operations.</p>
     *
     * @param dto the validated inbound outlet creation request
     * @return a transient {@link Outlet} entity (not yet persisted)
     */
    public static Outlet toEntity(OutletRequestDTO dto) {
        Outlet outlet = new Outlet();
        // Trim whitespace from user-supplied text fields
        outlet.setOutletName(dto.getOutletName().trim());
        outlet.setMerchantId(dto.getMerchantId());
        outlet.setCuisineType(dto.getCuisineType().trim());
        outlet.setOutletPhone(dto.getOutletPhone().trim());
        // Every new outlet starts as active
        outlet.setIsActive("Y");
        return outlet;
    }

    /**
     * Builds an {@link OutletAddress} entity from address fields in the DTO.
     *
     * <p>Why the stateId is a parameter: the service resolves the state name
     * (a string) to the actual FK integer by querying the states table. This
     * mapper only handles the field mapping, not that DB lookup.</p>
     *
     * <p>The {@code jippyAddressId} mirrors outletId — it acts as the Jippy
     * platform's internal address identifier and is always the same as the
     * outlet's PK in this design.</p>
     *
     * @param dto      the request DTO containing address fields
     * @param outletId the PK of the newly saved outlet
     * @param stateId  the resolved integer FK for the state name in the DTO
     * @return a transient {@link OutletAddress} entity ready to persist
     */
    /**
     * Builds an {@link OutletAddress} from the DTO plus the two resolved integer FKs.
     *
     * @param dto      the request DTO containing address fields
     * @param outletId the PK of the newly saved outlet
     * @param stateId  the resolved integer FK for the state name
     * @param areaId   the resolved integer FK for the area name (from ZipCode column)
     * @return a transient {@link OutletAddress} entity ready to persist
     */
    public static OutletAddress toAddressEntity(OutletRequestDTO dto, Integer outletId,
                                                Integer stateId, Integer areaId) {
        OutletAddress address = new OutletAddress();
        // FK to outlets table
        address.setOutletId(outletId);
        // Jippy platform internal address ID mirrors the outlet PK
        address.setJippyAddressId(outletId);
        address.setBuildingNumber(safe(dto.getBuildingNumber()));
        address.setRoad(safe(dto.getRoad()));
        address.setLandmark(safe(dto.getLandmark()));
        // cityId defaults to 0 when not provided to satisfy NOT NULL DB constraint
        address.setCityId(dto.getCityId() != null ? dto.getCityId() : 0);
        address.setStateId(stateId);
        // area_id — resolved from the area name supplied in the ZipCode column
        address.setAreaId(areaId);
        address.setAddressType("OUTLET");
        return address;
    }

    // ── Entity → DTO ──────────────────────────────────────────────────────────

    /**
     * Converts a saved {@link Outlet} entity into an {@link OutletCreatedDTO}.
     *
     * <p>Why we return a dedicated DTO instead of the entity: the response
     * must include the auto-generated portal credentials (loginId and password)
     * which are not stored on the entity for security reasons. The DTO carries
     * these one-time values back to the caller.</p>
     *
     * @param outlet   the persisted outlet entity
     * @param loginId  the auto-generated login ID (e.g. "ravi4567")
     * @param password the auto-generated plain-text password shown once to the admin
     * @return an {@link OutletCreatedDTO} containing entity fields plus credentials
     */
    public static OutletCreatedDTO toCreatedDTO(Outlet outlet, String loginId, String password) {
        OutletCreatedDTO dto = new OutletCreatedDTO();
        dto.setOutletId(outlet.getOutletId());
        dto.setOutletName(outlet.getOutletName());
        dto.setMerchantId(outlet.getMerchantId());
        dto.setCuisineType(outlet.getCuisineType());
        dto.setOutletPhone(outlet.getOutletPhone());
        dto.setIsActive(outlet.getIsActive());
        // Credentials are generated by CredentialUtil in the service layer and passed in here
        dto.setOutletLoginId(loginId);
        dto.setOutletPassword(password);
        return dto;
    }

    /**
     * Null-safe trimmer for address string fields.
     *
     * <p>Why we use this helper: several address fields are optional; returning
     * an empty string instead of null avoids NullPointerExceptions downstream
     * and satisfies the NOT NULL DB constraints on address columns.</p>
     *
     * @param s the raw string value, possibly null
     * @return trimmed value, or empty string if null
     */
    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }
}
