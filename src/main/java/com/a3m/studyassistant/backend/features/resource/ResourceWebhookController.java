package com.a3m.studyassistant.backend.features.resource;

import com.a3m.studyassistant.backend.features.resource.dto.SupabaseStorageWebhookPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
public class ResourceWebhookController {

    private final ResourceService resourceService;

    @Value("${supabase.bucket.name}")
    private String supabaseBucketName;

    @Value("${supabase.storage.public-endpoint}")
    private String supabaseStoragePublicEndpoint;

    public ResourceWebhookController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/storage")
    public ResponseEntity<Void> handleStorageWebhook(@RequestBody SupabaseStorageWebhookPayload payload) {
        if ("INSERT".equals(payload.getType()) && supabaseBucketName.equals(payload.getRecord().getBucketId())) {

            String fullPath = payload.getRecord().getName();

            // 1. ISOLATE THE FILENAME FROM THE DIRECTORY FOLDERS
            // e.g., turns "user-uuid/topic/branch/resource-uuid-file.pdf" into just "resource-uuid-file.pdf"
            String[] pathSegments = fullPath.split("/");
            String isolatedFileName = pathSegments[pathSegments.length - 1];

            // 2. PERFORM REGEX STRICTLY ON THE ISOLATED FILENAME
            java.util.regex.Pattern uuidPattern = java.util.regex.Pattern.compile(
                    "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
            );
            java.util.regex.Matcher matcher = uuidPattern.matcher(isolatedFileName);

            java.util.UUID resourceId;
            if (matcher.find()) {
                resourceId = java.util.UUID.fromString(matcher.group());
            } else {
                System.err.println("CRITICAL: Webhook received filename containing no Resource UUID: " + isolatedFileName);
                return ResponseEntity.ok().build();
            }

            // 3. Reconstruct the public target URL
            String filePublicUrl = supabaseStoragePublicEndpoint +
                    payload.getRecord().getBucketId() + "/" +
                    fullPath;

            System.out.println("Webhook successfully isolated true Resource ID: " + resourceId);

            // 4. Fire the async pipeline
            resourceService.processResource(resourceId, filePublicUrl);
        }

        return ResponseEntity.ok().build();
    }
}