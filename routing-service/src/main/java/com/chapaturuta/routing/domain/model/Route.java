package com.chapaturuta.routing.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    private UUID id;
    private String originDistrict;
    private String destinationDistrict;
    private Double price;
    private Integer durationMin;
}