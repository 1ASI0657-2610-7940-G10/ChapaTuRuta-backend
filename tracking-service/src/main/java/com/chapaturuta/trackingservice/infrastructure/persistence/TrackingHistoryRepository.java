package com.chapaturuta.trackingservice.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TrackingHistoryRepository extends JpaRepository<TrackingHistoryEntity, UUID> {
}