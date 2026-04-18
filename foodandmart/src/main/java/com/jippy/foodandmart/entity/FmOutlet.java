package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "outlets", schema = "jippy_fm")
@Data
public class FmOutlet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outlet_id")
    private Integer outletId;

    @Column(name = "outlet_name")
    private String outletName;

    @Column(name = "merchant_id")
    private Integer merchantId;

    @Column(name = "cuisine_type")
    private String cuisineType;

    @Column(name = "outlet_phone")
    private String outletPhone;

    @Column(name = "radius")
    private BigDecimal radius;

    @Column(name = "boundary")
    private String boundary; // Geography → map as String

    @Column(name = "outlet_location")
    private String outletLocation; // Geography

    @Column(name = "review")
    private BigDecimal review;

    @Column(name = "subscription_status")
    private String subscriptionStatus;

    @Column(name = "promotion_status")
    private String promotionStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "is_active")
    private String isActive;

    @Column(name = "employee_id")
    private Integer employeeId;

    @Column(name = "is_approved")
    private Boolean isApproved;
}