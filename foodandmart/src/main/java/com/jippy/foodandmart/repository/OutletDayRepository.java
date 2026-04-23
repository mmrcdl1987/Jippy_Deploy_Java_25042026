package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.OutletDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutletDayRepository extends JpaRepository<OutletDay, Integer> {
    List<OutletDay> findByOutletId(Integer outletId);
}
