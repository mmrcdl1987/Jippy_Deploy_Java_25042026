package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Maps to jippy_fm.user_bank_details (renamed from merchant_bank_details).
 * merchant_id → recipient_id  (generic: merchantId, driverId …)
 */
@Entity
@Table(name = "user_bank_details", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmMerchantBankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_id")
    private Integer bankId;

    /** Renamed from merchant_id; holds merchantId, driverId, etc. */
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "account_number", length = 30)
    private String accountNumber;

    @Column(name = "ifsc_code", length = 15)
    private String ifscCode;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_holder_name", length = 150)
    private String accountHolderName;

    /** New column: 'MERCHANT', 'DRIVER', etc. */
    @Column(name = "user_type", length = 50)
    private String userType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

//    mapping
//    @ManyToOne
//    @JoinColumn(name = "recipient_id", nullable = false)
//    private FmMerchant merchant;
}