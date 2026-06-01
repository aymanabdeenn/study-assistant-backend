package com.a3m.studyassistant.backend.features.topic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class TopicModificationDTO {
    private String title;
    private String description;

    private LocalDateTime createdAt;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("created_at")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
