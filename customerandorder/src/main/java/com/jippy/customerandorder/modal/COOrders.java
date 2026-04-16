package com.jippy.customerandorder.modal;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Table(name = "orders")
@Entity
@Data
public class COOrders {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long orderId;
    private Integer customerId;
    private Long driverId;
    private Long outletId;
    private String orderStatus;
    private String deliveryAddress;
    private String customerPhoneNumber;
    private LocalDateTime preparationTime;
    private LocalDateTime estimatedDeliveryTime;
    private Double distanceKms;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;

}
