package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    List<MenuItem> findByOutletId(Integer outletId);

    Optional<MenuItem> findByOutletIdAndItemName(Integer outletId, String itemName);

    boolean existsByOutletIdAndItemName(Integer outletId, String itemName);

    long countByOutletId(Integer outletId);
}
