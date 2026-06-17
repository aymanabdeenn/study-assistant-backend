package com.a3m.studyassistant.backend.features.resource;

import java.util.UUID;

public interface ResourceChunkProjection {
    UUID getId();
    String getContent();
    Integer getPageNumber();
    Long getChunkIndex();
}
