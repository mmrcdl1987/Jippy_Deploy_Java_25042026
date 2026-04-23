package com.jippy.foodandmart.service;


import com.jippy.foodandmart.dto.FmMerchantWithBankDto;

public interface IFmMerchantService {

//    // Get--> only merchant
//    FmMerchantDto getMerchantProfile(int merchantId);

    // Get--> merchant + bank
    FmMerchantWithBankDto getMerchantWithBank(Long merchantId);

    // Update--> merchant + bank
    FmMerchantWithBankDto updateMerchantProfile(FmMerchantWithBankDto dto);


}
