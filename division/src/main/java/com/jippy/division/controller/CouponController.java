package com.jippy.division.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/api/coupons")
@Tag(
        name = "Coupons API",
        description = "Rest API to create and perform operations on coupons")
public class CouponController {

    private static final Logger logger = LoggerFactory.getLogger(CouponController.class);

    @GetMapping("/demo")
    public String demoCoupon(){
        logger.info("Inside CouponController demoCoupon method");
        return "Get 20% off on your next purchase with code JIPPY20";
    }
}
