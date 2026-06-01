package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SummaryResponse(
        String title,
        String content,

        @JsonProperty("key_takeaways")
        List<String> keyTakeaways
) {}
