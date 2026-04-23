package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jippy.foodandmart.entity.MenuItem;
import lombok.*;

import java.math.BigDecimal;

/**
 * Data Transfer Object representing a single menu item for API responses.
 *
 * <p>Why a DTO instead of returning the entity: the {@link MenuItem} entity
 * has a lazy-loaded {@code outlet} association and a "Y"/"N" string for
 * availability. The DTO converts those to a clean boolean and avoids
 * accidental lazy-load triggers during JSON serialisation.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItemDTO {

    /** Database primary key of the menu item. */
    private Integer itemId;

    /** FK reference to the outlet this item belongs to. */
    private Integer outletId;

    /** Display name of the menu item (e.g. "Masala Dosa"). */
    private String  itemName;

    /** Category/section name within the menu (e.g. "Main Course"). */
    private String  category;

    /** Price of the item in rupees. Defaults to zero if not set. */
    private BigDecimal price;

    /** Optional free-text description shown below the item name in the UI. */
    private String  description;

    /** URL or base64 data URI of the item's photo. May be null. */
    private String  imageUrl;

    /**
     * Whether the item is currently available for ordering.
     * Converted from the "Y"/"N" DB column to a clean boolean for JSON output.
     */
    private boolean available;

    /**
     * Static factory that converts a {@link MenuItem} entity to this DTO.
     *
     * <p>Why a static factory on the DTO: it keeps the conversion logic
     * co-located with the DTO fields, making it easy to find if the DTO
     * structure changes. The service layer calls {@code MenuItemDTO.from(entity)}
     * rather than inline field assignments.</p>
     *
     * <p>Why use setters here instead of the builder: consistent with the
     * project-wide convention of using setter-style construction so all
     * object creation looks the same regardless of context.</p>
     *
     * @param m the menu item entity from the DB
     * @return a fully populated {@link MenuItemDTO}
     */
    public static MenuItemDTO from(MenuItem m) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setItemId(m.getItemId());
        dto.setOutletId(m.getOutletId());
        dto.setItemName(m.getItemName());
        dto.setCategory(m.getCategory());
        dto.setPrice(m.getPrice());
        dto.setDescription(m.getDescription());
        dto.setImageUrl(m.getImageUrl());
        // Convert legacy "Y"/"N" char column to boolean for clean JSON
        dto.setAvailable("Y".equalsIgnoreCase(m.getIsAvailable()));
        return dto;
    }
}
