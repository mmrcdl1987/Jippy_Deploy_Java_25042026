package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Integer> {
    Optional<Merchant> findByMerchantEmail(String email);
    Optional<Merchant> findByMerchantPhone(String phone);
    boolean existsByMerchantEmail(String email);
    boolean existsByMerchantPhone(String phone);
}
