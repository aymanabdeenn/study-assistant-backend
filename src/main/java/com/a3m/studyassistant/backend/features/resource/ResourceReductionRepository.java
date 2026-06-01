package com.a3m.studyassistant.backend.features.resource;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResourceReductionRepository extends JpaRepository<ResourceReduction, UUID> {
}
