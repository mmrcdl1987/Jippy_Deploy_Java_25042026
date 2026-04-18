package com.jippy.foodandmart.mapper;


import com.jippy.foodandmart.dto.FmMerchantDto;
import com.jippy.foodandmart.dto.FmMerchantWithBankDto;
import com.jippy.foodandmart.entity.FmMerchant;
import com.jippy.foodandmart.projections.FmMerchantWithBankProjection;

public class FmMerchantMapper {

    public static FmMerchant mapToMerchantEntity(FmMerchantDto merchantDto) {

        FmMerchant entity = new FmMerchant();
        entity.setMerchantId(merchantDto.getMerchantId());
        entity.setMerchantName(merchantDto.getMerchantName());
        entity.setMerchantEmail(merchantDto.getMerchantEmail());
        entity.setMerchantPhone(merchantDto.getMerchantPhone());
        entity.setMerchantBusinessType(merchantDto.getMerchantBusinessType());
        entity.setStatus(merchantDto.getStatus());
        entity.setCreatedAt(merchantDto.getCreatedAt());
        entity.setCreatedBy(merchantDto.getCreatedBy());
        entity.setUpdatedAt(merchantDto.getUpdatedAt());
        entity.setUpdatedBy(merchantDto.getUpdatedBy());
        entity.setIsActive(merchantDto.getIsActive());
        entity.setIsApproved(merchantDto.getIsApproved());


        // Default / system values
//        entity.setStatus("PENDING");
//        entity.setCreatedAt(LocalDateTime.now());
//        entity.setCreatedBy(merchantDto.getCreatedBy());
//
//        entity.setIsActive("Y");
//        entity.setIsApproved(false);

        return entity;
    }
    public static FmMerchantDto mapToMerchantDto(FmMerchant entityFromDb) {

        FmMerchantDto dto = new FmMerchantDto();

        dto.setMerchantId(entityFromDb.getMerchantId());
        dto.setMerchantName(entityFromDb.getMerchantName());
        dto.setMerchantEmail(entityFromDb.getMerchantEmail());
        dto.setMerchantPhone(entityFromDb.getMerchantPhone());
        dto.setMerchantBusinessType(entityFromDb.getMerchantBusinessType());
        dto.setStatus(entityFromDb.getStatus());
        dto.setCreatedAt(entityFromDb.getCreatedAt());
        dto.setCreatedBy(entityFromDb.getCreatedBy());
        dto.setUpdatedAt(entityFromDb.getUpdatedAt());
        dto.setUpdatedBy(entityFromDb.getUpdatedBy());
        dto.setIsActive(entityFromDb.getIsActive());
        dto.setIsApproved(entityFromDb.getIsApproved());

        return dto;
    }
//     projection to dto -->used (join tables) of 1)merchant & 2)merchant bank details
    public static FmMerchantWithBankDto mapToMerchantWithBankDto(FmMerchantWithBankProjection data) {
        if (data == null) {
            return null;
        }
        FmMerchantWithBankDto dto = new FmMerchantWithBankDto();
        // merchant details
        dto.setMerchantId(data.getMerchantId());
        dto.setMerchantName(data.getMerchantName());
        dto.setMerchantEmail(data.getMerchantEmail());
        dto.setMerchantPhone(data.getMerchantPhone());
        dto.setBusinessType(data.getBusinessType());
        dto.setStatus(data.getStatus());

        // bank details
        dto.setBankId(data.getBankId());
        dto.setRecipientId(data.getRecipientId());
        dto.setAccountNumber(data.getAccountNumber());
        dto.setIfscCode(data.getIfscCode());
        dto.setBankName(data.getBankName());
        dto.setAccountHolderName(data.getAccountHolderName());
        dto.setUserType(data.getUserType());

        return dto;
    }
}