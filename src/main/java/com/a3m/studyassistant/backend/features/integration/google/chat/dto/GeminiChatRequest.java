package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record GeminiChatRequest(
        List<Content> contents,

        @JsonProperty("system_instruction")
        Content systemInstruction, // Reuse the Content record here!

        @JsonProperty("generation_config")
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
            @JsonProperty("inline_data")
            InlineData inlineData
    ) {
        public static Part ofText(String text) { return new Part(text, null); }

        public static Part ofImage(String mimeType, String base64Data) {
            return new Part(null, new InlineData(mimeType, base64Data));
        }

        public record InlineData(
                @JsonProperty("mime_type")
                String mimeType,

                String data
        ) {}
    }

    public record GenerationConfig(
            @JsonProperty("response_mime_type")
            String responseMimeType,

            Double temperature,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonProperty("response_schema")
            Map<String, Object> responseSchema
    ) {}
}