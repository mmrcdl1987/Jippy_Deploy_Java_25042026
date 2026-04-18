package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", schema = "jippy_fm")
@Data
public class FmProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "outlet_category_id")
    private Integer outletCategoryId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "description")
    private String description;

    @Column(name = "merchant_price")
    private BigDecimal merchantPrice;

    @Column(name = "is_veg")
    private Boolean isVeg;

    @Column(name = "has_product_variants")
    private Boolean hasProductVariants;

    @Column(name = "image_link")
    private String imageLink;

    @Column(name = "photos")
    private String photos;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;
}