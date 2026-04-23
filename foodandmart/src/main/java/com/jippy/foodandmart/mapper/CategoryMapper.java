package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.dto.OutletCategoryDTO;
import com.jippy.foodandmart.entity.Category;
import com.jippy.foodandmart.entity.OutletCategory;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Static utility class for converting Category-related DTOs and entities.
 *
 * <p>Why a separate mapper: centralises the field mapping so the service
 * and controller layers don't duplicate assignment logic.</p>
 */
public final class CategoryMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private CategoryMapper() {}

    /**
     * Creates a new {@link Category} entity from a category name and the ID
     * of the user creating it.
     *
     * <p>Why we set createdAt here instead of relying on {@code @PrePersist}:
     * some callers (e.g. batch imports) need the timestamp before the entity
     * hits the DB, so we set it explicitly to be safe.</p>
     *
     * @param categoryName the display name for the category (will be trimmed)
     * @param createdBy    the user ID creating this category (audit trail)
     * @return a transient {@link Category} entity ready to persist
     */
    public static Category toEntity(String categoryName, Integer createdBy) {
        Category entity = new Category();
        // Trim the category name to remove accidental whitespace from user input
        entity.setCategoryName(categoryName.trim());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);
        return entity;
    }

    /**
     * Creates a new {@link OutletCategory} join-table entity linking an outlet
     * to a global category.
     *
     * <p>Why this is its own method: the outlet_categories table is a many-to-many
     * join between outlets and categories. This factory method makes creating
     * those links readable without inline setters scattered across the codebase.</p>
     *
     * @param outletId   the FK to the outlet
     * @param categoryId the FK to the global category
     * @param createdBy  the user ID performing this linkage (audit trail)
     * @return a transient {@link OutletCategory} entity ready to persist
     */
    public static OutletCategory toOutletCategoryEntity(Integer outletId, Integer categoryId, Integer createdBy) {
        OutletCategory entity = new OutletCategory();
        entity.setOutletId(outletId);
        entity.setCategoryId(categoryId);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);
        return entity;
    }

    /**
     * Converts an {@link OutletCategory} entity into an {@link OutletCategoryDTO}.
     *
     * <p>Why products is initialised to empty list: the DTO is used in read
     * scenarios where product list may be populated lazily by the caller.
     * Returning an empty list instead of null prevents NPEs in the frontend.</p>
     *
     * <p>The category name is resolved from the lazy-loaded {@link OutletCategory#getCategory()}
     * association — returns null safely if the association wasn't loaded.</p>
     *
     * @param outletCategory the entity (category association should be loaded if name is needed)
     * @return an {@link OutletCategoryDTO} with an empty products list
     */
    public static OutletCategoryDTO toOutletCategoryDTO(OutletCategory outletCategory) {
        OutletCategoryDTO dto = new OutletCategoryDTO();
        dto.setOutletCategoryId(outletCategory.getOutletCategoryId());
        dto.setCategoryId(outletCategory.getCategoryId());
        // Null-safe: only resolves name if the lazy association was loaded
        dto.setCategoryName(
                outletCategory.getCategory() != null
                        ? outletCategory.getCategory().getCategoryName()
                        : null
        );
        // Products populated separately by the service if needed
        dto.setProducts(Collections.emptyList());
        return dto;
    }
}
