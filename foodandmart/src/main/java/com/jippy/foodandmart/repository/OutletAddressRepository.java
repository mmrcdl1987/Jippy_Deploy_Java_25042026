package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.OutletAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutletAddressRepository extends JpaRepository<OutletAddress, Integer> {
    java.util.Optional<OutletAddress> findByOutletId(Integer outletId);
}
