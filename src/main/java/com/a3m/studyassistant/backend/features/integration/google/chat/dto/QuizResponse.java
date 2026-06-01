package com.a3m.studyassistant.backend.features.integration.google.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QuizResponse(
        @JsonProperty("quiz_title")
        String quizTitle,

        List<Question> questions
) {
    public record Question(
            @JsonProperty("question_text")
            String questionText,

            List<String> options,
            @JsonProperty("correct_option_index")
            int correctOptionIndex,

            String explanation
    ) {}
}


