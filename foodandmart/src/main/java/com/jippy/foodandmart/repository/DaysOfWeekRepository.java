package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.DaysOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DaysOfWeekRepository extends JpaRepository<DaysOfWeek, Integer> {
    Optional<DaysOfWeek> findByDayNameIgnoreCase(String dayName);
}
