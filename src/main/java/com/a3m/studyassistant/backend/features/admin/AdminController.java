package com.a3m.studyassistant.backend.features.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PatchMapping("/promote/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> promoteToAdmin(@PathVariable UUID userId) {
        adminService.promoteToAdmin(userId);
        return ResponseEntity.ok("User promoted to Admin successfully. Changes are live!");
    }

}
