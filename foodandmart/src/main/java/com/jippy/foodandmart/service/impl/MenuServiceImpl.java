package com.jippy.foodandmart.service.impl;

import com.jippy.foodandmart.constants.AppConstants;
import com.jippy.foodandmart.dto.MenuCopyRequestDTO;
import com.jippy.foodandmart.dto.MenuCopyResultDTO;
import com.jippy.foodandmart.dto.MenuItemDTO;
import com.jippy.foodandmart.dto.OutletSummaryDTO;
import com.jippy.foodandmart.entity.MenuItem;
import com.jippy.foodandmart.entity.Outlet;
import com.jippy.foodandmart.entity.OutletMenuMapping;
import com.jippy.foodandmart.repository.MenuItemRepository;
import com.jippy.foodandmart.repository.OutletMenuMappingRepository;
import com.jippy.foodandmart.repository.OutletRepository;
import com.jippy.foodandmart.service.interfaces.IMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for menu operations including listing outlets,
 * fetching menu items, and copying menus between outlets.
 *
 * <p>Why a dedicated menu service: menu copy is a multi-step operation
 * (clone items + record mapping + handle errors per item) that deserves
 * its own service rather than being stuffed into OutletService or a
 * generic CRUD service.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements IMenuService {

    private final MenuItemRepository          menuItemRepository;
    private final OutletMenuMappingRepository mappingRepository;
    private final OutletRepository            outletRepository;

    /**
     * Returns a summary of all outlets including their current menu item count.
     *
     * <p>Why include item count: the Copy Menu UI shows a badge on each outlet
     * card ("X items") so users can see at a glance which outlets have menus.</p>
     *
     * @return list of all outlets with their menu item counts
     */
    @Override
    public List<OutletSummaryDTO> listAllOutlets() {
        return outletRepository.findAll().stream()
                .map(o -> OutletSummaryDTO.from(o, menuItemRepository.countByOutletId(o.getOutletId())))
                .collect(Collectors.toList());
    }

    /**
     * Returns all menu items for the specified outlet.
     *
     * <p>Why validate outlet existence first: if the outletId doesn't exist the
     * repository would return an empty list, which looks identical to "outlet
     * has no menu items". Throwing a 404 makes the distinction clear.</p>
     *
     * @param outletId the outlet's primary key
     * @return list of {@link MenuItemDTO} for that outlet
     * @throws IllegalArgumentException if the outlet does not exist
     */
    @Override
    public List<MenuItemDTO> getMenuByOutlet(Integer outletId) {
        if (!outletRepository.existsById(outletId))
            throw new IllegalArgumentException("Outlet ID " + outletId + " does not exist");
        return menuItemRepository.findByOutletId(outletId).stream()
                .map(MenuItemDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * Copies selected menu items from a source outlet to one or more destination outlets.
     *
     * <p>Why collect errors per item rather than fail fast: a menu copy across
     * multiple outlets should not abort entirely if a single item fails at one
     * destination. The result DTO captures each failure with its outlet, item,
     * and reason so the user can see a partial success summary.</p>
     *
     * <p>Each item copy records a row in outlet_menu_mapping for audit purposes
     * regardless of whether it succeeded or failed.</p>
     *
     * @param req the copy request specifying source, destinations, item selection, and options
     * @return a {@link MenuCopyResultDTO} with success/failure counts and per-item error detail
     */
    @Override
    @Transactional
    public MenuCopyResultDTO copyMenu(MenuCopyRequestDTO req) {
        Integer srcId   = req.getSourceOutletId();
        List<Integer> destIds = req.getDestinationOutletIds();
        // Use empty CopyOptions (all defaults) if none were provided in the request
        MenuCopyRequestDTO.CopyOptions opts = req.getOptions() != null
                ? req.getOptions() : new MenuCopyRequestDTO.CopyOptions();

        log.info("[MENU] copyMenu: sourceOutlet={}, destinations={}", srcId, destIds);

        // Validate source outlet exists
        outletRepository.findById(srcId)
                .orElseThrow(() -> new IllegalArgumentException("Source outlet " + srcId + " not found"));

        // Determine which items to copy — null/empty list means "copy ALL items from source"
        List<MenuItem> itemsToCopy = (req.getMenuItemIds() == null || req.getMenuItemIds().isEmpty())
                ? menuItemRepository.findByOutletId(srcId)
                : menuItemRepository.findAllById(req.getMenuItemIds()).stream()
                        .filter(i -> i.getOutletId().equals(srcId))
                        .collect(Collectors.toList());

        if (itemsToCopy.isEmpty()) {
            MenuCopyResultDTO empty = new MenuCopyResultDTO();
            empty.setTotalItems(0);
            empty.setTotalOutlets(destIds.size());
            empty.setSuccessCount(0);
            empty.setFailureCount(0);
            empty.setErrors(List.of());
            return empty;
        }

        int successCount = 0;
        int failureCount = 0;
        List<MenuCopyResultDTO.CopyError> errors = new ArrayList<>();

        for (Integer destId : destIds) {
            Optional<Outlet> destOpt = outletRepository.findById(destId);
            if (destOpt.isEmpty()) {
                // All items fail for this destination outlet since it doesn't exist
                for (MenuItem item : itemsToCopy) {
                    failureCount++;
                    MenuCopyResultDTO.CopyError err = new MenuCopyResultDTO.CopyError();
                    err.setDestOutletId(destId);
                    err.setDestOutletName("Unknown");
                    err.setItemId(item.getItemId());
                    err.setItemName(item.getItemName());
                    err.setReason("Destination outlet not found");
                    errors.add(err);
                }
                continue;
            }
            String destName = destOpt.get().getOutletName();

            for (MenuItem srcItem : itemsToCopy) {
                try {
                    Integer clonedItemId = cloneItem(srcItem, destId, opts);
                    // Record the mapping for audit trail regardless of outcome
                    OutletMenuMapping mapping = new OutletMenuMapping();
                    mapping.setSourceOutletId(srcId);
                    mapping.setDestOutletId(destId);
                    mapping.setSourceItemId(srcItem.getItemId());
                    mapping.setDestItemId(clonedItemId);
                    mapping.setCopyPrices(opts.isCopyPrices() ? AppConstants.FLAG_YES : AppConstants.FLAG_NO);
                    mapping.setCopyAvailability(opts.isCopyAvailability() ? AppConstants.FLAG_YES : AppConstants.FLAG_NO);
                    mapping.setCopyImages(opts.isCopyImages() ? AppConstants.FLAG_YES : AppConstants.FLAG_NO);
                    mapping.setOverwriteExisting(opts.isOverwriteExisting() ? AppConstants.FLAG_YES : AppConstants.FLAG_NO);
                    mapping.setStatus(AppConstants.COPY_STATUS_SUCCESS);
                    mappingRepository.save(mapping);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    MenuCopyResultDTO.CopyError err = new MenuCopyResultDTO.CopyError();
                    err.setDestOutletId(destId);
                    err.setDestOutletName(destName);
                    err.setItemId(srcItem.getItemId());
                    err.setItemName(srcItem.getItemName());
                    err.setReason(e.getMessage());
                    errors.add(err);
                    // Record failure mapping for audit trail
                    try {
                        OutletMenuMapping failMapping = new OutletMenuMapping();
                        failMapping.setSourceOutletId(srcId);
                        failMapping.setDestOutletId(destId);
                        failMapping.setSourceItemId(srcItem.getItemId());
                        failMapping.setCopyPrices(opts.isCopyPrices() ? AppConstants.FLAG_YES : AppConstants.FLAG_NO);
                        failMapping.setCopyAvailability(opts.isCopyAvailability() ? AppConstants.FLAG_YES : AppConstants.FLAG_NO);
                        failMapping.setCopyImages(opts.isCopyImages() ? AppConstants.FLAG_YES : AppConstants.FLAG_NO);
                        failMapping.setOverwriteExisting(opts.isOverwriteExisting() ? AppConstants.FLAG_YES : AppConstants.FLAG_NO);
                        failMapping.setStatus(AppConstants.COPY_STATUS_FAILED);
                        mappingRepository.save(failMapping);
                    } catch (Exception ignored) {}
                }
            }
        }

        log.info("[MENU] Copy complete: success={}, failed={}", successCount, failureCount);
        MenuCopyResultDTO result = new MenuCopyResultDTO();
        result.setTotalItems(itemsToCopy.size());
        result.setTotalOutlets(destIds.size());
        result.setSuccessCount(successCount);
        result.setFailureCount(failureCount);
        result.setErrors(errors);
        return result;
    }

    /**
     * Clones a single menu item from the source outlet into the destination outlet.
     *
     * <p>Why handle existing item: when {@code overwriteExisting=false} and the
     * item name already exists at the destination, we throw so the caller can
     * record it as a failure. When {@code overwriteExisting=true}, we update
     * the existing item in place rather than inserting a duplicate.</p>
     *
     * <p>CopyOptions control which fields are copied: prices, availability
     * status, and image URLs. Fields not copied default to safe values
     * (zero price, available, no image).</p>
     *
     * @param src        the source menu item to clone
     * @param destOutletId the target outlet's ID
     * @param opts       copy options controlling which fields to carry over
     * @return the itemId of the cloned or updated destination item
     * @throws IllegalStateException if item already exists and overwriteExisting=false
     */
    private Integer cloneItem(MenuItem src, Integer destOutletId,
                               MenuCopyRequestDTO.CopyOptions opts) {
        Optional<MenuItem> existing = menuItemRepository
                .findByOutletIdAndItemName(destOutletId, src.getItemName());

        if (existing.isPresent()) {
            if (!opts.isOverwriteExisting())
                throw new IllegalStateException(
                        "Item '" + src.getItemName() + "' already exists at destination (overwrite=false)");
            // Update only the fields that the copy options allow
            MenuItem ex = existing.get();
            if (opts.isCopyPrices())       ex.setPrice(src.getPrice());
            if (opts.isCopyAvailability()) ex.setIsAvailable(src.getIsAvailable());
            if (opts.isCopyImages())       ex.setImageUrl(src.getImageUrl());
            ex.setCategory(src.getCategory());
            ex.setDescription(src.getDescription());
            return menuItemRepository.save(ex).getItemId();
        }

        // Create a new item at the destination; apply options for selective field copy
        MenuItem clone = new MenuItem();
        clone.setOutletId(destOutletId);
        clone.setItemName(src.getItemName());
        clone.setCategory(src.getCategory());
        clone.setDescription(src.getDescription());
        // If not copying prices, default to zero so the outlet manager sets their own price
        clone.setPrice(opts.isCopyPrices() ? src.getPrice() : BigDecimal.ZERO);
        // If not copying availability, default to "Y" so the item shows up immediately
        clone.setIsAvailable(opts.isCopyAvailability() ? src.getIsAvailable() : AppConstants.FLAG_YES);
        // If not copying images, leave null so the outlet can upload their own photo
        clone.setImageUrl(opts.isCopyImages() ? src.getImageUrl() : null);
        return menuItemRepository.save(clone).getItemId();
    }
}
