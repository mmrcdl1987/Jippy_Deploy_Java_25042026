package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {

    /**
     * Case-insensitive exact match on area_name.
     *
     * <p>Used during outlet bulk upload to resolve the human-readable area name
     * supplied in the ZipCode column to the integer area_id FK stored in the
     * address table.</p>
     */
    @Query("SELECT a FROM Area a WHERE LOWER(TRIM(a.areaName)) = LOWER(TRIM(:name))")
    Optional<Area> findByAreaNameIgnoreCase(@Param("name") String name);
}
