package com.chapaturuta.identity.infrastructure.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataCompanyRepository extends JpaRepository<CompanyEntity, UUID> {
    boolean existsByRuc(String ruc);
    java.util.Optional<CompanyEntity> findByManager_Id(UUID managerId);
}