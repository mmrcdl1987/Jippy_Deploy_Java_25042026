package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.MenuItemDTO;
import com.jippy.foodandmart.entity.MenuItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Static utility class for converting between {@link MenuItemDTO} and
 * the {@link MenuItem} JPA entity.
 *
 * <p>Why a separate mapper: isolates field-mapping logic from the service
 * so changes to the DTO or entity only require edits in one place.</p>
 */
public final class MenuItemMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private MenuItemMapper() {}

    /**
     * Converts a {@link MenuItemDTO} into a new {@link MenuItem} entity.
     *
     * <p>Why we use this: avoids inline field assignments in the service layer.
     * Price defaults to BigDecimal.ZERO when null to satisfy the DB NOT NULL
     * constraint and prevent unexpected NPEs during arithmetic.</p>
     *
     * <p>isAvailable is mapped from the boolean DTO field to the legacy "Y"/"N"
     * character column because the DB schema predates boolean columns.</p>
     *
     * @param dto the inbound menu item data from the HTTP request
     * @return a transient {@link MenuItem} entity ready to persist
     */
    public static MenuItem toEntity(MenuItemDTO dto) {
        MenuItem entity = new MenuItem();
        entity.setOutletId(dto.getOutletId());
        // Trim item name to remove accidental leading/trailing whitespace
        entity.setItemName(dto.getItemName() != null ? dto.getItemName().trim() : null);
        entity.setCategory(dto.getCategory() != null ? dto.getCategory().trim() : null);
        // Default price to zero if not provided — prevents NPE in price calculations
        entity.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        entity.setDescription(dto.getDescription());
        entity.setImageUrl(dto.getImageUrl());
        // DB schema uses "Y"/"N" flag instead of a boolean column
        entity.setIsAvailable(dto.isAvailable() ? "Y" : "N");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    /**
     * Converts a {@link MenuItem} entity into a {@link MenuItemDTO}.
     *
     * <p>Why we use this: the controller should never return raw entities to
     * avoid exposing JPA internals or lazy-load triggers. The DTO is a safe,
     * serialisable snapshot of the entity's data.</p>
     *
     * <p>isAvailable is converted back from "Y"/"N" to a Java boolean for
     * clean JSON output ({@code "available": true}).</p>
     *
     * @param entity the persisted menu item entity from the DB
     * @return a {@link MenuItemDTO} safe for JSON serialisation
     */
    public static MenuItemDTO toDTO(MenuItem entity) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setItemId(entity.getItemId());
        dto.setOutletId(entity.getOutletId());
        dto.setItemName(entity.getItemName());
        dto.setCategory(entity.getCategory());
        dto.setPrice(entity.getPrice());
        dto.setDescription(entity.getDescription());
        dto.setImageUrl(entity.getImageUrl());
        // Convert legacy "Y"/"N" flag back to boolean for clean JSON
        dto.setAvailable("Y".equalsIgnoreCase(entity.getIsAvailable()));
        return dto;
    }
}
