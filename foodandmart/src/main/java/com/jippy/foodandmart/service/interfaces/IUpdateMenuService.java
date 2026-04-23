package com.jippy.foodandmart.service.interfaces;

import com.jippy.foodandmart.dto.OutletCategoryDTO;
import com.jippy.foodandmart.dto.UpdateMenuResultDTO;

import java.util.List;
import java.util.Map;

public interface IUpdateMenuService {

    List<OutletCategoryDTO> getMenuByOutlet(Integer outletId);

    UpdateMenuResultDTO uploadMenu(List<Map<String, String>> rows, Integer outletId);
}
