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
public class RouteStop {
    private UUID id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String address;
    private Integer stopOrder;
}
