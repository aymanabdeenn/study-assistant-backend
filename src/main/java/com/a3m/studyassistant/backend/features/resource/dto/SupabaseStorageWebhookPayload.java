package com.a3m.studyassistant.backend.features.resource.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SupabaseStorageWebhookPayload {
    private String type; // e.g., "INSERT"
    private Record record;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Record getRecord() { return record; }
    public void setRecord(Record record) { this.record = record; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Record {
        private String id;

        @JsonProperty("bucket_id")
        private String bucketId;

        private String name; // This is the file path inside the bucket (e.g., "folder/lecture1.pptx")

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getBucketId() { return bucketId; }
        public void setBucketId(String bucketId) { this.bucketId = bucketId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
