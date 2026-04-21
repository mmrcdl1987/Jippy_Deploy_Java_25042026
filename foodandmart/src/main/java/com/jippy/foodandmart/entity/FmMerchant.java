package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "merchants", schema = "jippy_fm")
public class FmMerchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Integer merchantId;

    @Column(name = "merchant_name", length = 150)
    private String merchantName;

    @Column(name = "merchant_email", length = 150, unique = true)
    private String merchantEmail;

    @Column(name = "merchant_phone", length = 20, unique = true)
    private String merchantPhone;

    @Column(name = "merchant_business_type", length = 50)
    private String merchantBusinessType;

    @Column(name = "status", length = 30)
    private String status = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "is_active", length = 1)
    private String isActive = "Y";

    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved = false;

////    mapping
//    @OneToMany(mappedBy = "merchant")
//    private List<FmMerchantBankDetails> bankDetails;
//
//    @OneToMany(mappedBy = "merchant")
//    private List<FmMerchantKyc> kycs;
}