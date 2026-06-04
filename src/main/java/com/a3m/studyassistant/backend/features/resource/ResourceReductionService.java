package com.a3m.studyassistant.backend.features.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ResourceReductionService {
    private final ResourceReductionRepository resourceReductionRepository;

    @Autowired
    public ResourceReductionService(ResourceReductionRepository resourceReductionRepository) {
        this.resourceReductionRepository = resourceReductionRepository;
    }

    public ResourceReduction getReductionByResourceId(UUID resourceId) {
        return resourceReductionRepository.findByResourceId(resourceId);
    }

    @Transactional
    public ResourceReduction createReduction(Resource resource, String summaryText) {
        ResourceReduction reduction = new ResourceReduction();
        reduction.setFinalSummaryString(summaryText);

        reduction.setResource(resource);
        resource.setResourceReduction(reduction);

        return resourceReductionRepository.save(reduction);
    }
}
