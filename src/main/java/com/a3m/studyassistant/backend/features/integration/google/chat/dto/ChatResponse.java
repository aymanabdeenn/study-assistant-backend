package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ChatResponse(
        List<Candidate> candidates,

        @JsonProperty("usage_metadata")
        UsageMetadata usageMetadata
) {
    public record Candidate(
            Content content,
            @JsonProperty("finish_reason")
            String finishReason
    ) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public record UsageMetadata(
            @JsonProperty("prompt_token_count")
            int promptTokenCount,
            @JsonProperty("candidates_token_count")
            int candidatesTokenCount,
            @JsonProperty("total_token_count")
            int totalTokenCount
    ) {}
}
