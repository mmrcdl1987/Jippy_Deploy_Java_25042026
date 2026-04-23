package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByOutletCategoryId(Integer outletCategoryId);
    Optional<Product> findByOutletCategoryIdAndProductNameIgnoreCase(Integer outletCategoryId, String productName);
    boolean existsByOutletCategoryIdAndProductNameIgnoreCase(Integer outletCategoryId, String productName);
    long countByOutletCategoryId(Integer outletCategoryId);
}
