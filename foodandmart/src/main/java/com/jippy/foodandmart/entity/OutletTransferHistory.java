package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outlet_transfer_history", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutletTransferHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Integer transferId;

    @Column(name = "outlet_id",       nullable = false) private Integer outletId;
    @Column(name = "from_merchant_id",nullable = false) private Integer fromMerchantId;
    @Column(name = "to_merchant_id",  nullable = false) private Integer toMerchantId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id",        insertable = false, updatable = false)
    private Outlet outlet;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_merchant_id", insertable = false, updatable = false)
    private Merchant fromMerchant;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_merchant_id",   insertable = false, updatable = false)
    private Merchant toMerchant;

    @Column(name = "transfer_reason", length = 500) private String transferReason;
    @Column(name = "transfer_status", length = 30, nullable = false) @Builder.Default private String transferStatus = "COMPLETED";
    @Column(name = "transferred_at",  nullable = false) private LocalDateTime transferredAt;
    @Column(name = "transferred_by")  private Integer transferredBy;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "created_by") private Integer createdBy;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "updated_by") private Integer updatedBy;

    @PrePersist public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.transferredAt == null) this.transferredAt = LocalDateTime.now();
        if (this.transferStatus == null) this.transferStatus = "COMPLETED";
    }
    @PreUpdate public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
