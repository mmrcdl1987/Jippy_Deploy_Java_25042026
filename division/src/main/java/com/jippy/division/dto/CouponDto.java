package com.jippy.division.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CouponDto {

    private  Integer couponId;

    @NotEmpty(message = "Coupon code should not be empty")
    private String couponCode;

    @NotEmpty(message = "Price Model Id should not be empty")
    private Integer priceModelId;

    @NotEmpty(message = "Max discount should not be empty")
    private Double maxDiscount;

    @NotEmpty(message = "Is Active should not be empty")
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotEmpty(message = "Start time should not be empty")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotEmpty(message = "End Time should not be empty")
    private LocalDateTime endTime;

    @NotEmpty(message = "Created by should not be empty")
    private Integer createdBy;





}
