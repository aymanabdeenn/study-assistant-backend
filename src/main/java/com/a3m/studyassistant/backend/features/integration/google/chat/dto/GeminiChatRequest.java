package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import java.util.List;
import java.util.Map;

public record GeminiChatRequest(
        List<Content> contents,
        Content systemInstruction, // Reuse the Content record here!
        GenerationConfig generationConfig
) {
    public record Content(
            String role, // Important: "user" for contents, "system" for instruction
            List<Part> parts
    ) {}

    public record Part(String text) {}

    public record GenerationConfig(
            String responseMimeType,
            Double temperature,
            Map<String, Object> responseSchema
    ) {}
}