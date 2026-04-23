package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.RolePermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionsRepository extends JpaRepository<RolePermissions, Integer> {
    List<RolePermissions> findByRoleId(Integer roleId);
}