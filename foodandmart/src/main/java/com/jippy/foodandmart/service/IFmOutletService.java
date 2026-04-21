package com.jippy.foodandmart.service;


import com.jippy.foodandmart.dto.FmOutletByMerchantDto;
import com.jippy.foodandmart.dto.FmOutletDetailsDto;

import java.util.List;

public interface IFmOutletService {
    //     for api to get outlet details by outlet id and user type (merchant or customer)
    FmOutletDetailsDto getOutletDetails(Integer outletId, String userType);

    //    for api to get all outlets by merchant id
    List<FmOutletByMerchantDto> getOutletsByMerchantId(Integer merchantId);
}