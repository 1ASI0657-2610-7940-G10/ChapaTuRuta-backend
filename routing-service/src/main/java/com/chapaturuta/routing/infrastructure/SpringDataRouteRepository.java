package com.chapaturuta.routing.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SpringDataRouteRepository extends JpaRepository<com.chapaturuta.routing.infrastructure.persistence.RouteEntity, UUID> {
    List<com.chapaturuta.routing.infrastructure.persistence.RouteEntity> findByOriginDistrictIgnoreCaseAndDestinationDistrictIgnoreCase(String origin, String destination);

    List<com.chapaturuta.routing.infrastructure.persistence.RouteEntity> findByOriginDistrictIgnoreCase(String origin);
    List<com.chapaturuta.routing.infrastructure.persistence.RouteEntity> findByDestinationDistrictIgnoreCase(String destination);
}