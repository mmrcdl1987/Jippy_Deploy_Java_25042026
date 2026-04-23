package com.jippy.foodandmart.service.interfaces;

import com.jippy.foodandmart.dto.BulkOutletResultDTO;
import com.jippy.foodandmart.dto.OutletCreatedDTO;
import com.jippy.foodandmart.dto.OutletRequestDTO;
import com.jippy.foodandmart.dto.OutletSummaryDTO;
import com.jippy.foodandmart.entity.Outlet;

import java.util.List;

public interface IOutletService {

    long countOutlets();

    List<Outlet> getAllOutlets();

    List<OutletSummaryDTO> getAllOutletsSummary();

    List<OutletSummaryDTO> getOutletsByMerchantId(Integer merchantId);

    Outlet getOutletById(Integer id);

    OutletCreatedDTO createOutlet(OutletRequestDTO dto);

    BulkOutletResultDTO bulkUpload(List<OutletRequestDTO> rows);
}
