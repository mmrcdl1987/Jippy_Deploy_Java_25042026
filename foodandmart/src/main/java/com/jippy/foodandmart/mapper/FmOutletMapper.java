package com.jippy.foodandmart.mapper;


import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.projections.FmOutletMenuProjection;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FmOutletMapper {

    public static FmOutletDetailsDto mapToOutletDto(List<FmOutletMenuProjection> rows, String userType) {

        if (rows == null || rows.isEmpty()) {
            log.warn("No data found to map");
            return null;
        }

        log.debug("Mapping {} rows into OutletDetailsDto", rows.size());

        FmOutletDetailsDto outlet = new FmOutletDetailsDto();

        Map<String, FmOutletTimingDto> outletTimingMap = new LinkedHashMap<>();
        Map<Integer, FmCategoryDto> categoryMap = new LinkedHashMap<>();

//        Each Row combination of= Outlet + Category + Product + Timing
        for (FmOutletMenuProjection row : rows) {

            if (row != null) {

                // -------- Outlet --------
                outlet.setOutletId(row.getOutletId());
                outlet.setOutletName(row.getOutletName());
                outlet.setOutletPhone(row.getOutletPhone());

                // -------- Outlet Timing --------
                String day = row.getOutletDay();
                if (day != null) {
                    FmOutletTimingDto timing = outletTimingMap.get(day);

                    if (timing == null) {
                        timing = new FmOutletTimingDto();
                        timing.setDay(day);
                        timing.setIsOpen(row.getIsOpen());
                        timing.setOpeningTime(row.getOpeningTime());
                        timing.setClosingTime(row.getClosingTime());

                        outletTimingMap.put(day, timing);
                    }
                }

                // -------- Category --------
                Integer categoryId = row.getCategoryId();
                FmCategoryDto category = null;

                if (categoryId != null) {
                    category = categoryMap.get(categoryId);

                    if (category == null) {
                        category = new FmCategoryDto();
                        category.setCategoryId(categoryId);
                        category.setCategoryName(row.getCategoryName());
                        category.setProducts(new ArrayList<>());

                        categoryMap.put(categoryId, category);
                    }
                }

                // -------- Product --------
                if (category != null && row.getProductId() != null) {

                    Integer productId = row.getProductId();
                    FmProductDto product = null;

                    // search product inside category only
                    for (FmProductDto p : category.getProducts()) {
                        if (p.getProductId().equals(productId)) {
                            product = p;
                            break;
                        }
                    }

                    // create if not exists
                    if (product == null) {
                        product = new FmProductDto();
                        product.setProductId(productId);
                        product.setProductName(row.getProductName());
                        product.setDescription(row.getDescription());

//                  Price logic based on user type and availability(CUSTOMER/MERCHANT) of online price
                        if ("CUSTOMER".equalsIgnoreCase(userType) && row.getOnlinePrice() != null) {
                            product.setPrice(row.getOnlinePrice());
                        } else {
                            product.setPrice(row.getMerchantPrice());
                        }
                        product.setIsVeg(row.getIsVeg());
                        product.setHasProductVariants(row.getHasProductVariants());
                        product.setProductTimings(new ArrayList<>());

                        category.getProducts().add(product);
                    }

                    // -------- Product Timing --------
                    if (row.getStartTime() != null) {
                        FmProductTimingDto pt = new FmProductTimingDto();
                        pt.setDay(row.getProductDay());
                        pt.setStartTime(row.getStartTime());
                        pt.setEndTime(row.getEndTime());

                        product.getProductTimings().add(pt);
                    }
                }
            }
        }

        // -------- Final Conversion --------
        //---- Convert maps to lists and set in outlet DTO ----
        outlet.setOutletTimings(new ArrayList<>(outletTimingMap.values()));
        outlet.setCategories(new ArrayList<>(categoryMap.values()));

        log.debug("Mapping completed for outletId={}", outlet.getOutletId());

        return outlet;
    }
}