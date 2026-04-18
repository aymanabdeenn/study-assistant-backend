package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import java.util.List;

public record AtomicFactResponse(
        List<String> facts
) {
}
