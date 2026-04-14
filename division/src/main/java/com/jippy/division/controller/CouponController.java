package com.jippy.division.controller;

import com.jippy.division.constants.AppConstants;
import com.jippy.division.dto.CouponDto;
import com.jippy.division.dto.ResponseDto;
import com.jippy.division.service.ICouponService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api/coupons")
@Tag(
        name = "Coupons API",
        description = "Rest API to create and perform operations on coupons")
public class CouponController {

    @Autowired
    ICouponService couponService;


    private static final Logger logger = LoggerFactory.getLogger(CouponController.class);

    @GetMapping("/demo")
    public String demoCoupon(){
        logger.info("Inside CouponController demoCoupon method");
        return "Get 20% off on your next purchase with code JIPPY20";
    }
// Create a new coupon
    @PostMapping("/createCoupon")
    public ResponseEntity<ResponseDto> createCoupon(@RequestBody CouponDto couponDto) {

        logger.info("Inside CouponController createCoupon method with request: {}", couponDto);
         couponService.createCoupon(couponDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto(AppConstants.STATUS_201,"Coupon created successfully"));
    }

}
