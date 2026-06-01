package com.a3m.studyassistant.backend.features.resource.dto;

import com.a3m.studyassistant.backend.features.resource.ResourceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ResourceCreationResponse(
        @JsonProperty("resource_id")
        UUID resourceId,

        String title,

        @JsonProperty("processing_status")
        ResourceStatus processingStatus,

        @JsonProperty("branch_id")
        UUID branchId,

        @JsonProperty("topic_id")
        UUID topicId
) {}
