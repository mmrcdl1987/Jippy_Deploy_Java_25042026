package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outlet_menu_mapping", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutletMenuMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Integer mappingId;

    @Column(name = "source_outlet_id", nullable = false)
    private Integer sourceOutletId;

    @Column(name = "dest_outlet_id", nullable = false)
    private Integer destOutletId;

    @Column(name = "source_item_id", nullable = false)
    private Integer sourceItemId;

    @Column(name = "dest_item_id")
    private Integer destItemId;

    @Column(name = "copy_prices", length = 1)
    @Builder.Default private String copyPrices = "Y";

    @Column(name = "copy_availability", length = 1)
    @Builder.Default private String copyAvailability = "Y";

    @Column(name = "copy_images", length = 1)
    @Builder.Default private String copyImages = "N";

    @Column(name = "overwrite_existing", length = 1)
    @Builder.Default private String overwriteExisting = "N";

    @Column(name = "status", length = 20)
    @Builder.Default private String status = "SUCCESS";

    @Column(name = "copied_at")
    private LocalDateTime copiedAt;

    @PrePersist
    public void prePersist() {
        this.copiedAt = LocalDateTime.now();
    }
}
