package com.a3m.studyassistant.backend.features.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConversationAgentResponse(
        String answer
        , String details
        , @JsonProperty("follow_up_concept") String followUpConcept
) {
}
