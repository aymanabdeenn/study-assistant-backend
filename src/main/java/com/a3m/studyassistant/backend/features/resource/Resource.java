package com.a3m.studyassistant.backend.features.resource;

import com.a3m.studyassistant.backend.features.branch.Branch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "url")
    private String url;

    @Column(name = "type")
    private String type;

    @Column(name = "size")
    private Float size;

    private String processingStatus = "PENDING";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceChunk> resourceChunks;

    public Resource() {
        this.resourceChunks = new ArrayList<>();
    }

    public Resource(String title, String url, String type, float size, Branch branch) {
        this.title = title;
        this.url = url;
        this.type = type;
        this.size = size;
        this.branch = branch;
        this.resourceChunks = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Float getSize() {
        return size;
    }

    public void setSize(Float size) {
        this.size = size;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void addChunk(ResourceChunk chunk) {
        if(this.resourceChunks == null) {
            this.resourceChunks = new ArrayList<>();
        }
        this.resourceChunks.add(chunk);
        chunk.setResource(this);
    }

    public void removeChunk(ResourceChunk chunk) {
        if(this.resourceChunks != null) {
            this.resourceChunks.remove(chunk);
            chunk.setResource(null);
        }
    }

}
