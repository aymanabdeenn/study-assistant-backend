package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import java.util.List;

public record QuizResponse(
        String quizTitle,
        List<Question> questions
) {
    public record Question(
            String questionText,
            List<String> options,
            int correctOptionIndex,
            String explanation
    ) {}
}

