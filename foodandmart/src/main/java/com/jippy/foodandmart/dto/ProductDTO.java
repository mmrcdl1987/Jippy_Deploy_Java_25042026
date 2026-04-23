package com.jippy.foodandmart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO {
    private Integer    productId;
    private String     productName;
    private String     description;
    private BigDecimal merchantPrice;
    private Boolean    isVeg;
    private Boolean    hasProductVariants;
    private List<ProductVariantDTO> variants;
}
