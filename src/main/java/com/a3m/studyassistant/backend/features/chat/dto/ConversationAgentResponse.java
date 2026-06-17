package com.a3m.studyassistant.backend.features.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ConversationAgentResponse(
        String answer
        , List<String> details
        , String followUpConcept
) {
}
