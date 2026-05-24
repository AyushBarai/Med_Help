package com.medhelp.common.catalog;

import com.medhelp.common.catalog.TestCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestCatalogRepository extends JpaRepository<TestCatalog, UUID> {
    List<TestCatalog> findAllByLabIdAndIsActiveTrue(UUID labId);
    Optional<TestCatalog> findByIdAndLabId(UUID id, UUID labId);
    List<TestCatalog> findAllByIdInAndLabId(List<UUID> ids, UUID labId);
}