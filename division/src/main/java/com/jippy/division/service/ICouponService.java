package com.jippy.division.service;

import com.jippy.division.dto.DivCouponRequestDto;
import com.jippy.division.dto.DivCouponResponseDto;

import java.util.List;

public interface ICouponService {

    void createCoupon(DivCouponRequestDto dto);

    void updateCoupon(DivCouponRequestDto divCouponRequestDto);

    void disableCoupon(Integer couponId);

    void enableCoupon(Integer couponId);

    // List<DivCouponResponseDto> getAllCoupons();

    List<DivCouponResponseDto> getAllCoupons(int page, int size);

}
