package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductId(Integer productId);
    void deleteByProductId(Integer productId);
    Optional<ProductVariant> findByProductIdAndVariantName(Integer productId, String variantName);
}
