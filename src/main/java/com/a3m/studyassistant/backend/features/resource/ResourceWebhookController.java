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
    private String supabaseStoragePublicEndpoint; // Clean, pre-packaged root path

    public ResourceWebhookController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/storage")
    public ResponseEntity<Void> handleStorageWebhook(@RequestBody SupabaseStorageWebhookPayload payload) {
        if ("INSERT".equals(payload.getType()) && supabaseBucketName.equals(payload.getRecord().getBucketId())) {

            // 1. Get the full path string (e.g. "54831360-.../8bc3.../.../067223bd-bb6b-404d-bc34-656fc00490df-2_2_process.pdf")
            String fullPath = payload.getRecord().getName();

            // 2. Extract the actual filename segment (the part after the last forward slash)
            String filenameSegment = fullPath.substring(fullPath.lastIndexOf("/") + 1);

            // 3. Extract the leading resource UUID from that filename segment
            String resourceIdStr = filenameSegment.split("-")[0] + "-" +
                    filenameSegment.split("-")[1] + "-" +
                    filenameSegment.split("-")[2] + "-" +
                    filenameSegment.split("-")[3] + "-" +
                    filenameSegment.split("-")[4];

            // Clean up the trailing parts if a dash was inside the original file title
            if (resourceIdStr.contains(".")) {
                resourceIdStr = resourceIdStr.split("\\.")[0];
            }

            java.util.UUID resourceId = java.util.UUID.fromString(resourceIdStr.substring(0, 36));

            // 4. Reconstruct the public target URL
            String filePublicUrl = supabaseStoragePublicEndpoint +
                    payload.getRecord().getBucketId() + "/" +
                    payload.getRecord().getName();

            System.out.println("Targeting verified Resource ID directly: " + resourceId);

            // 5. Pass it safely to your async polling loop
            resourceService.processResource(resourceId, filePublicUrl);
        }

        return ResponseEntity.ok().build();
    }
}
