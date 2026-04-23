package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.OutletMenuMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutletMenuMappingRepository extends JpaRepository<OutletMenuMapping, Integer> {

    List<OutletMenuMapping> findBySourceOutletIdAndDestOutletId(Integer sourceOutletId, Integer destOutletId);
}
