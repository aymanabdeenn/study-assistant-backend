package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import com.a3m.studyassistant.backend.features.flashcard.FlashcardDTO;

import java.util.List;

public record FlashcardResponse (
    List<FlashcardDTO> cards
) {}
