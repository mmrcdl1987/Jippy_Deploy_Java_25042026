package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.modal.COOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface COOrderRepository extends JpaRepository<COOrders,Long> {
}
