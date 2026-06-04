package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ChatResponse(
        List<Candidate> candidates,

        UsageMetadata usageMetadata
) {
    public record Candidate(
            Content content,
            String finishReason
    ) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public record UsageMetadata(
            int promptTokenCount,
            int candidatesTokenCount,
            int totalTokenCount
    ) {}
}
