package com.a3m.studyassistant.backend.features.branch;

import com.a3m.studyassistant.backend.features.branch.dto.BranchCreationDTO;
import com.a3m.studyassistant.backend.features.branch.dto.BranchModificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/branches")
public class BranchController {

    private final BranchService branchService;

    @Autowired
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/{branchId}")
    public ResponseEntity<?> getBranch(@PathVariable UUID branchId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Branch branch = branchService.getBranchById(userId, branchId);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<?> getBranchesForTopic(@PathVariable UUID topicId,@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        List<Branch> branches = branchService.getBranchesListForTopic(userId, topicId);
        return ResponseEntity.ok(branches);
    }

    @PostMapping("/{topicId}")
    public ResponseEntity<?> createBranch(@PathVariable UUID topicId, @AuthenticationPrincipal Jwt jwt, @RequestBody BranchCreationDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Branch branch = branchService.createBranch(userId, topicId, dto.getTitle(), dto.getDescription());
        return ResponseEntity.ok(branch);
    }

    @PatchMapping("/{branchId}")
    public ResponseEntity<?> modifyBranch(@PathVariable UUID branchId, @AuthenticationPrincipal Jwt jwt, @RequestBody BranchModificationDTO dto) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Branch branch = branchService.modifyBranch(userId, branchId, dto.getTitle(), dto.getDescription());
        return ResponseEntity.ok(branch);
    }

    @DeleteMapping("/{branchId}")
    public ResponseEntity<?> deleteBranch(@PathVariable UUID branchId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        branchService.deleteBranch(userId, branchId);
        return ResponseEntity.ok("Branch with id " + branchId + " has been deleted successfully!");
    }

}
