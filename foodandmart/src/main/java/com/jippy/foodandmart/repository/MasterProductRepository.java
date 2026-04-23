package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.MasterProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasterProductRepository extends JpaRepository<MasterProduct, Integer> {

    List<MasterProduct> findAllByOrderByMasterProductIdAsc();

    List<MasterProduct> findByCategoryIdOrderByMasterProductIdAsc(Integer categoryId);

    boolean existsByMasterProductNameIgnoreCase(String name);

    boolean existsByMasterProductNameIgnoreCaseAndCategoryId(String name, Integer categoryId);

    @Query("""
            SELECT p FROM MasterProduct p WHERE
            LOWER(p.masterProductName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY p.masterProductId ASC
            """)
    List<MasterProduct> searchByName(@Param("keyword") String keyword);

    @Query("""
            SELECT p FROM MasterProduct p WHERE
            (:type = 'all')
            OR (:type = 'veg'    AND p.veg = 1)
            OR (:type = 'nonveg' AND p.nonVeg = 1)
            ORDER BY p.masterProductId ASC
            """)
    List<MasterProduct> filterByType(@Param("type") String type);
}
