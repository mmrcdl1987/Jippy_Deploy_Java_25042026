package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.MapToProductRequest;
import com.jippy.foodandmart.dto.MapToProductResult;
import com.jippy.foodandmart.dto.MasterProductMappingResultDTO;
import com.jippy.foodandmart.service.interfaces.IProductMappingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductMappingController {

    private final IProductMappingService productMappingService;

    /**
     * POST /api/products/from-master
     * Maps selected products (from compare result) into jippy_fm.products
     * linked to the chosen outlet category.
     */
    @PostMapping("/from-master")
    public ResponseEntity<MapToProductResult> mapFromMaster(@RequestBody MapToProductRequest req) {
        log.info("[PRODUCT-MAP] POST /api/products/from-master — outletCategoryId={}, count={}",
                req.getOutletCategoryId(),
                req.getProducts() == null ? 0 : req.getProducts().size());
        return ResponseEntity.ok(productMappingService.mapToProducts(req));
    }

    /**
     * POST /api/products/map-from-master-category/{outletCategoryId}
     *
     * At outlet-mapping time: looks up the category already linked to
     * outletCategoryId, fetches ALL published master_products for that
     * category, and auto-inserts them into:
     *   → jippy_fm.products          (one row per master product)
     *   → jippy_fm.product_variants  (one row per option/variant from options jsonb)
     *
     * Already-existing products for this outlet category are skipped.
     */
    @PostMapping("/map-from-master-category/{outletCategoryId}")
    public ResponseEntity<MasterProductMappingResultDTO> mapFromMasterCategory(
            @PathVariable Integer outletCategoryId) {

        log.info("[PRODUCT-MAP] POST /api/products/map-from-master-category/{}", outletCategoryId);
        return ResponseEntity.ok(productMappingService.mapFromMasterByCategory(outletCategoryId));
    }
}
