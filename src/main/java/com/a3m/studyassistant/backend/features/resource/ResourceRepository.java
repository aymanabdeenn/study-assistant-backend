package com.a3m.studyassistant.backend.features.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    List<Resource> findByBranchId(UUID branchId);
}
