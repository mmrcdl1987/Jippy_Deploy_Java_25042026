package com.jippy.division.dto;

import lombok.Data;

@Data
public class DivCouponResponseDto {
    private Integer id;
    private String code;
    private Integer type;
    private Double discountValue;
    private Boolean isActive;
}