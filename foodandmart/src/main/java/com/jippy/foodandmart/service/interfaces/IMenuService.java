package com.jippy.foodandmart.service.interfaces;

import com.jippy.foodandmart.dto.MenuCopyRequestDTO;
import com.jippy.foodandmart.dto.MenuCopyResultDTO;
import com.jippy.foodandmart.dto.MenuItemDTO;
import com.jippy.foodandmart.dto.OutletSummaryDTO;

import java.util.List;

public interface IMenuService {

    List<OutletSummaryDTO> listAllOutlets();

    List<MenuItemDTO> getMenuByOutlet(Integer outletId);

    MenuCopyResultDTO copyMenu(MenuCopyRequestDTO req);
}
