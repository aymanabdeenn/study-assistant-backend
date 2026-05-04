package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

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

    public record Part(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String text,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            InlineData inlineData
    ) {
        public static Part ofText(String text) { return new Part(text, null); }

        public static Part ofImage(String mimeType, String base64Data) {
            return new Part(null, new InlineData(mimeType, base64Data));
        }

        public record InlineData(
                String mimeType,
                String data
        ) {}
    }

    public record GenerationConfig(
            String responseMimeType,
            Double temperature,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            Map<String, Object> responseSchema
    ) {}
}