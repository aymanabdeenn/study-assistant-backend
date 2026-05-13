package com.a3m.studyassistant.backend.features.flashcard;

public record FlashcardDTO(
        String front,
        String back,
        String hint,
        String category
) {}
