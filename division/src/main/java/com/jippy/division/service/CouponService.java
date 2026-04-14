package com.jippy.division.service;

import com.jippy.division.dto.CouponDto;
import com.jippy.division.entity.Coupon;
import com.jippy.division.exception.CouponCodeAlreadyExistsException;
import com.jippy.division.mapper.CouponMapper;
import com.jippy.division.repositary.CouponRepository;
import com.jippy.division.repositary.CouponRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CouponService implements ICouponService{

    @Autowired
    public CouponRepository couponRepository;

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    @Override
    public void createCoupon(CouponDto couponDto) {
        logger.info("Inside CouponService createCoupon method with request: ");

        Coupon couponEntity = CouponMapper.mapToEntity(couponDto);
        Optional<Coupon> optionalCoupon =  couponRepository.findByCouponCode(couponDto.getCouponCode());
       if(optionalCoupon.isPresent()){
           throw new CouponCodeAlreadyExistsException("Coupon already exists with given code, " +
                   "Please give some other code");
       }
        Coupon savedCoupon = couponRepository.save(couponEntity);

    }
}
