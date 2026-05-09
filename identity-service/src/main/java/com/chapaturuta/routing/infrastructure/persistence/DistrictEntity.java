package com.chapaturuta.routing.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "districts")
@Data
public class DistrictEntity {
    @Id
    private Long id; // Ej: 150101 (Ubigeo)

    @Column(nullable = false)
    private String name; // Ej: San Juan de Lurigancho

    @Column(nullable = false)
    private String province; // Ej: Lima

    @Column(nullable = false)
    private String region; // Ej: Lima Metropolitana
}