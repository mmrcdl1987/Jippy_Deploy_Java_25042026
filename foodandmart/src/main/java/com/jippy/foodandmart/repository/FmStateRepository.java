package com.jippy.foodandmart.repository;


import com.jippy.foodandmart.entity.FmState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FmStateRepository extends JpaRepository<FmState, Integer> {

//    List<FmCity> findByStateId(Integer stateId);
}