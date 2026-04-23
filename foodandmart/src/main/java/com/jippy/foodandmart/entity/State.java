package com.jippy.foodandmart.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "states", schema = "jippy_fm")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "state_id")
    private Integer stateId;

    @Column(name = "state_name", length = 100, nullable = false)
    private String stateName;
}
