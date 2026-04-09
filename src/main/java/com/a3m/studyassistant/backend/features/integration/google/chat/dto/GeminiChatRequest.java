package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

//public record GeminiChatRequest(
//        List<Content> contents,
//        SystemInstruction system_instruction,
//        GenerationConfig generation_config
//) {
//    public record Content(List<Part> parts) {}
//    public record Part(String text) {}
//
//    public record SystemInstruction(List<Part> parts) {}
//
//    public record GenerationConfig(
//            String responseMimeType,
//            Double temperature,
////            Integer maxOutputTokens,
////            Integer candidateCount,
//            Map<String, Object> responseSchema
//    ) {}
//}

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