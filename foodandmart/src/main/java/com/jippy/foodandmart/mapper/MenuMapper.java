package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.MenuItemDTO;
import com.jippy.foodandmart.dto.OutletTransferResponseDTO;
import com.jippy.foodandmart.entity.MenuItem;
import com.jippy.foodandmart.entity.OutletTransferHistory;

/**
 * Static utility class for converting Menu and Outlet Transfer related
 * entities into their DTO counterparts.
 *
 * <p>Why a combined mapper: both {@link MenuItem} and {@link OutletTransferHistory}
 * are used together in the menu-transfer workflow. Grouping them avoids
 * creating a trivially small separate mapper for each.</p>
 */
public final class MenuMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private MenuMapper() {}

    /**
     * Converts a {@link MenuItem} entity into a {@link MenuItemDTO}.
     *
     * <p>Why we delegate to {@link MenuItemDTO#from(MenuItem)}: the static
     * factory on the DTO is the canonical conversion path. This method exists
     * so the service can call {@code MenuMapper.toDTO(item)} without needing
     * to import the DTO directly — keeping import lists consistent.</p>
     *
     * @param item the persisted menu item entity
     * @return a {@link MenuItemDTO} safe for JSON serialisation
     */
    public static MenuItemDTO toDTO(MenuItem item) {
        return MenuItemDTO.from(item);
    }

    /**
     * Converts an {@link OutletTransferHistory} entity (plus resolved names)
     * into an {@link OutletTransferResponseDTO}.
     *
     * <p>Why resolved names are passed as parameters: the entity only stores
     * integer FKs for outlet, fromMerchant, and toMerchant. The service
     * resolves those IDs to human-readable names and passes them in, keeping
     * this mapper free of repository dependencies.</p>
     *
     * @param h                the persisted transfer history entity
     * @param outletName       the name of the transferred outlet (resolved by service)
     * @param fromMerchantName the name of the current/previous owner merchant
     * @param toMerchantName   the name of the new owner merchant
     * @return a fully populated {@link OutletTransferResponseDTO}
     */
    public static OutletTransferResponseDTO toTransferDTO(OutletTransferHistory h,
                                                          String outletName,
                                                          String fromMerchantName,
                                                          String toMerchantName) {
        OutletTransferResponseDTO dto = new OutletTransferResponseDTO();
        dto.setTransferId(h.getTransferId());
        dto.setOutletId(h.getOutletId());
        // outletName is passed in because the entity only holds the FK
        dto.setOutletName(outletName);
        dto.setFromMerchantId(h.getFromMerchantId());
        dto.setFromMerchantName(fromMerchantName);
        dto.setToMerchantId(h.getToMerchantId());
        dto.setToMerchantName(toMerchantName);
        dto.setTransferReason(h.getTransferReason());
        dto.setTransferStatus(h.getTransferStatus());
        dto.setTransferredAt(h.getTransferredAt());
        return dto;
    }
}
