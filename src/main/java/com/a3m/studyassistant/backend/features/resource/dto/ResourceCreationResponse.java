package com.a3m.studyassistant.backend.features.resource.dto;

import com.a3m.studyassistant.backend.features.resource.ResourceStatus;

import java.util.UUID;

public record ResourceCreationResponse(
        UUID resourceId,
        String title,
        ResourceStatus processingStatus,
        UUID branchId,
        UUID topicId
) {}
