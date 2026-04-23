package com.jippy.foodandmart.service.interfaces;

import com.jippy.foodandmart.dto.OutletTransferRequestDTO;
import com.jippy.foodandmart.dto.OutletTransferResponseDTO;

import java.util.List;

public interface IOutletTransferService {

    OutletTransferResponseDTO transferOutlet(OutletTransferRequestDTO request);

    List<OutletTransferResponseDTO> getHistoryByOutlet(Integer outletId);

    List<OutletTransferResponseDTO> getInboundTransfers(Integer merchantId);

    List<OutletTransferResponseDTO> getOutboundTransfers(Integer merchantId);

    List<OutletTransferResponseDTO> getAllTransfers();
}
