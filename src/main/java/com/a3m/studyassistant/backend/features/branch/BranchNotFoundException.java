package com.a3m.studyassistant.backend.features.branch;

public class BranchNotFoundException extends RuntimeException {
    public BranchNotFoundException(String message) {
        super(message);
    }
}
