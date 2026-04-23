package com.jippy.foodandmart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FmProductDto {
    private Integer productId;
    private String productName;
    private String description;
//    accept both merchant price and online price as the same field "price".
    private BigDecimal Price;
    private Boolean isVeg;
    private Boolean hasProductVariants;

    private List<FmProductTimingDto> productTimings;
}