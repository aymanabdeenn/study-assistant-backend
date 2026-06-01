package com.a3m.studyassistant.backend.features.quiz;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QuizParametersDTO(String difficulty, @JsonProperty("num_of_questions") int numOfQuestions) {
}
