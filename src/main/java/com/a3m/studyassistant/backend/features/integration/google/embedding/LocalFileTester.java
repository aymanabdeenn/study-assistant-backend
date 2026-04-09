package com.a3m.studyassistant.backend.features.integration.google.embedding;

import com.a3m.studyassistant.backend.features.resource.Resource;
import com.a3m.studyassistant.backend.features.resource.ResourceRepository;
import com.a3m.studyassistant.backend.features.resource.ResourceService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

//@Component
public class LocalFileTester implements CommandLineRunner {

    private final ResourceService resourceService;
    private final ResourceRepository resourceRepository;

    public LocalFileTester(ResourceService resourceService, ResourceRepository resourceRepository) {
        this.resourceService = resourceService;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Path to your test file (Update this to your actual file path!)
        File testFile = new File("C:\\Users\\ayman\\Downloads\\Ch1_Python.pptx");

        if (!testFile.exists()) {
            System.out.println("❌ File not found at: " + testFile.getAbsolutePath());
            return;
        }

        // 2. Create a "Dummy" Resource entity to link the chunks to
        Resource dummyResource = new Resource();
        dummyResource.setTitle("Local Test Book");
        dummyResource.setUrl(testFile.getAbsolutePath());
        // Save it first so it has an ID
        resourceRepository.save(dummyResource);

        System.out.println("🚀 Starting Local Ingestion Test...");

        // 3. Open the stream and process
        try (InputStream inputStream = new FileInputStream(testFile)) {
            resourceService.processResource(inputStream, dummyResource);
        }

        System.out.println("✅ Test Complete! Check your database for ResourceChunks.");
    }

}
