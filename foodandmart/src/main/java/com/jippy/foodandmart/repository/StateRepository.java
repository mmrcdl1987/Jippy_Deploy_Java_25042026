package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Integer> {

    /**
     * Case-insensitive exact match on state_name.
     */
    @Query("SELECT s FROM State s WHERE LOWER(TRIM(s.stateName)) = LOWER(TRIM(:name))")
    Optional<State> findByStateNameIgnoreCase(@Param("name") String name);
}
