package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutletRepository extends JpaRepository<Outlet, Integer> {
    Optional<Outlet> findByOutletPhone(String phone);
    boolean existsByOutletPhone(String phone);
    boolean existsByMerchantIdAndOutletName(Integer merchantId, String outletName);
    List<Outlet> findByMerchantId(Integer merchantId);
}
