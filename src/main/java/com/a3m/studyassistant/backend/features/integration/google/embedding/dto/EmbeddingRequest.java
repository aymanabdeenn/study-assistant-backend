package com.a3m.studyassistant.backend.features.integration.google.embedding.dto;

import java.util.List;

public record EmbeddingRequest(String model, Content content, int outputDimensionality) {
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
}
