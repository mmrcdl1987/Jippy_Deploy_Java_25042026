package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.ProductAvailableTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAvailableTimingRepository extends JpaRepository<ProductAvailableTiming, Integer> {
    List<ProductAvailableTiming> findByProductId(Integer productId);
    void deleteByProductId(Integer productId);
}
