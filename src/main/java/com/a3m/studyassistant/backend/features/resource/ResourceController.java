package com.a3m.studyassistant.backend.features.resource;

import com.a3m.studyassistant.backend.features.resource.dto.ResourceCreationDTO;
import com.a3m.studyassistant.backend.features.resource.dto.ResourceModificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<?> getResource(@PathVariable UUID resourceId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Resource resource = resourceService.getResourceById(userId, resourceId);
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<?> getResourcesForBranch(@PathVariable UUID branchId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<Resource> resources = resourceService.getResourcesListForBranch(userId, branchId);
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{branchId}")
    public ResponseEntity<?> createResource(@PathVariable UUID branchId, @AuthenticationPrincipal Jwt jwt, @RequestBody ResourceCreationDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Resource resource = resourceService.createResource(userId, branchId, dto.getTitle(), dto.getUrl(), dto.getType(), dto.getSize());
        return ResponseEntity.ok(resource);
    }

    @PatchMapping("/{resourceId}")
    public ResponseEntity<?> modifyResource(@PathVariable UUID resourceId, @AuthenticationPrincipal Jwt jwt, @RequestBody ResourceModificationDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Resource resource = resourceService.modifyResource(userId, resourceId, dto.getTitle(), dto.getUrl(), dto.getType(), dto.getSize());
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable UUID resourceId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        resourceService.deleteResource(userId, resourceId);
        return ResponseEntity.ok("Resource with id " + resourceId + " has been deleted successfully!");
    }

}
