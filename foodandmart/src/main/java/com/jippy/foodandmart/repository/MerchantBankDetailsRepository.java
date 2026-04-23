package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.MerchantBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantBankDetailsRepository extends JpaRepository<MerchantBankDetails, Integer> {
    Optional<MerchantBankDetails> findByRecipientId(Integer recipientId);
    boolean existsByAccountNumber(String accountNumber);
}
