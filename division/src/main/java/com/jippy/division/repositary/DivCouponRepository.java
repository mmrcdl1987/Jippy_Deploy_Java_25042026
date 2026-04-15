package com.jippy.division.repositary;

import com.jippy.division.entity.DivCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DivCouponRepository extends JpaRepository <DivCoupon, Integer> {

    Optional<DivCoupon> findByCouponCode(String couponCode);
}

