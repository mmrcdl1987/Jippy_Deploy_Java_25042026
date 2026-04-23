package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.OutletCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutletCategoryRepository extends JpaRepository<OutletCategory, Integer> {
    List<OutletCategory> findByOutletId(Integer outletId);
    Optional<OutletCategory> findByOutletIdAndCategoryId(Integer outletId, Integer categoryId);
    boolean existsByOutletIdAndCategoryId(Integer outletId, Integer categoryId);
}
