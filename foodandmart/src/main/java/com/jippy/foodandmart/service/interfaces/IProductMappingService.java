package com.jippy.foodandmart.service.interfaces;

import com.jippy.foodandmart.dto.MapToProductRequest;
import com.jippy.foodandmart.dto.MapToProductResult;
import com.jippy.foodandmart.dto.MasterProductMappingResultDTO;

public interface IProductMappingService {

    MapToProductResult mapToProducts(MapToProductRequest req);

    MasterProductMappingResultDTO mapFromMasterByCategory(Integer outletCategoryId);
}
