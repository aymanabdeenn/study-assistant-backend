package com.a3m.studyassistant.backend.features.integration.google.embedding.dto;

public record EmbeddingResponse(Embedding embedding) {
    public record Embedding(float[] values) {}
}
