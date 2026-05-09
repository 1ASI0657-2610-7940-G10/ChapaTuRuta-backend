package com.chapaturuta.routing.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "stops")
@Data
public class StopEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name; // Ej: "Paradero Óvalo Santa Anita"

    @Column(nullable = false, length = 200)
    private String address; // Ej: "Cruce Carretera Central con Vía de Evitamiento"

    @Column(length = 200)
    private String reference; // Ej: "Frente al Mall Aventura"

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // Relación con el distrito
    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private DistrictEntity district;
}