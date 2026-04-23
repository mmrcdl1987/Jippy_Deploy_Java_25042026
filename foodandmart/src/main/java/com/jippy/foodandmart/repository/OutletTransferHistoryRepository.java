package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.OutletTransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutletTransferHistoryRepository extends JpaRepository<OutletTransferHistory, Integer> {

    /** All transfers for a specific outlet, newest first. */
    List<OutletTransferHistory> findByOutletIdOrderByTransferredAtDesc(Integer outletId);

    /** All outbound transfers made by a merchant (they gave away these outlets). */
    List<OutletTransferHistory> findByFromMerchantIdOrderByTransferredAtDesc(Integer fromMerchantId);

    /** All inbound transfers received by a merchant. */
    List<OutletTransferHistory> findByToMerchantIdOrderByTransferredAtDesc(Integer toMerchantId);

    /** All transfers across all outlets, newest first — for admin overview. */
    List<OutletTransferHistory> findAllByOrderByTransferredAtDesc();
}
