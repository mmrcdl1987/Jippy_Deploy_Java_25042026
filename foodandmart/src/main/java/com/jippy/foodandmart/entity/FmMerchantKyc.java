package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_kyc", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmMerchantKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kyc_id")
    private Integer kycId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "aadhaar_number", length = 20)
    private String aadhaarNumber;

    @Column(name = "fssai_number", length = 30)
    private String fssaiNumber;

    /** New column in updated schema */
    @Column(name = "gst_number", length = 30)
    private String gstNumber;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.verified == null) this.verified = false;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

//    @ManyToOne
//    @JoinColumn(name = "merchant_id", nullable = false)
//    private FmMerchant merchant;
}
