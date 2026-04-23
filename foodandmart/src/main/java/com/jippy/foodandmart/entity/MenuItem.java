package com.jippy.foodandmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "outlet_id", nullable = false)
    private Integer outletId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outlet_id", insertable = false, updatable = false)
    private Outlet outlet;

    @Column(name = "item_name",   length = 150, nullable = false) private String itemName;
    @Column(name = "category",    length = 100) private String category;
    @Column(name = "price",       precision = 10, scale = 2) @Builder.Default private BigDecimal price = BigDecimal.ZERO;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "image_url",   length = 500) private String imageUrl;
    @Column(name = "is_available",length = 1)  @Builder.Default private String isAvailable = "Y";
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    @PrePersist public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isAvailable == null) this.isAvailable = "Y";
        if (this.price == null)       this.price       = BigDecimal.ZERO;
    }
    @PreUpdate public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
