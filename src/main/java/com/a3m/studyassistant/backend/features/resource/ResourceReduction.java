package com.a3m.studyassistant.backend.features.resource;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "resources_reductions")
public class ResourceReduction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "resource_id")
    @JsonBackReference
    private Resource resource;

    @Column(columnDefinition = "TEXT")
    private String finalSummaryString;

    public ResourceReduction() {}

    public ResourceReduction(Resource resource, String finalSummaryString) {
        this.resource = resource;
        this.finalSummaryString = finalSummaryString;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getFinalSummaryString() {
        return finalSummaryString;
    }

    public void setFinalSummaryString(String finalSummaryString) {
        this.finalSummaryString = finalSummaryString;
    }
}
