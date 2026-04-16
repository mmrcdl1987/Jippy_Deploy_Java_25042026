package com.jippy.division.mapper;

import com.jippy.division.dto.DivCouponRequestDto;
import com.jippy.division.dto.DivCouponResponseDto;
import com.jippy.division.entity.DivCoupon;

import java.time.LocalDateTime;

public class DivCouponMapper {


    public static DivCoupon toEntity(DivCoupon divCoupon, DivCouponRequestDto divCouponRequestDto) {

        if(divCouponRequestDto.getCouponCode() != null){
            divCoupon.setCouponCode(divCouponRequestDto.getCouponCode());
        }
        if(divCouponRequestDto.getApplicationType() != null){
            divCoupon.setApplicationType(divCouponRequestDto.getApplicationType());
        }
        if(divCouponRequestDto.getPriceModelId() != null){
            divCoupon.setPriceModelId(divCouponRequestDto.getPriceModelId());
        }
        if(divCouponRequestDto.getDiscountValue() != null){
            divCoupon.setDiscountValue(divCouponRequestDto.getDiscountValue());
        }

        if(divCouponRequestDto.getPaymentMode() != null){
            divCoupon.setPaymentMethod(divCouponRequestDto.getPaymentMode());
        }
        if(divCouponRequestDto.getDiscountValue() != null){
            divCoupon.setMinOrderValue(divCouponRequestDto.getMinOrderValue());
        }
        if(divCouponRequestDto.getStartDate() != null){
            divCoupon.setStartTime(divCouponRequestDto.getStartDate());
        }
        if(divCouponRequestDto.getEndDate() != null){
            divCoupon.setEndTime(divCouponRequestDto.getEndDate());
        }
        if(divCouponRequestDto.getUsageLimitPerUser() != null){
            divCoupon.setUsageLimitPerUser(divCouponRequestDto.getUsageLimitPerUser());
        }
        if(divCouponRequestDto.getCreatedBy() != null){
            divCoupon.setCreatedBy(divCouponRequestDto.getCreatedBy());
        }

        divCoupon.setIsActive(true);
        divCoupon.setCreatedAt(LocalDateTime.now());

        return divCoupon;
    }

    // ENTITY → DTO
    public static DivCouponResponseDto toDTO(DivCoupon divCoupon) {

        if (divCoupon == null) return null;

        DivCouponResponseDto divCouponResponseDto = new DivCouponResponseDto();

        divCouponResponseDto.setId(divCoupon.getCouponId());
        divCouponResponseDto.setCode(divCoupon.getCouponCode());
        divCouponResponseDto.setType(divCoupon.getApplicationType());
        divCouponResponseDto.setDiscountValue(divCoupon.getDiscountValue());
        divCouponResponseDto.setIsActive(divCoupon.getIsActive());

        return divCouponResponseDto;
    }

}
