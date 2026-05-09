package com.chapaturuta.routing.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "origin_district", nullable = false)
    private String originDistrict;

    @Column(name = "destination_district", nullable = false)
    private String destinationDistrict;

    @Column(nullable = false)
    private Double price;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;
}