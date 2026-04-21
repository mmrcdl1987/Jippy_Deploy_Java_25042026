package com.jippy.foodandmart.repository;


import com.jippy.foodandmart.entity.FmArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FmAreaRepository extends JpaRepository<FmArea, Integer> {

    List<FmArea> findByCityId(Integer cityId);
}