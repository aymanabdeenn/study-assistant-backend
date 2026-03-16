package com.a3m.studyassistant.backend.features.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncUserAtSignup(@AuthenticationPrincipal Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");

        Map<String, Object> appMetadata = jwt.getClaimAsMap("app_metadata");
        String roleStr = (appMetadata != null) ? (String) appMetadata.get("role") : "STUDENT";

        Map<String, Object> userMetadata = jwt.getClaimAsMap("user_metadata");

        String firstName = null;
        String lastName = null;
        LocalDate dob = null;

        if (userMetadata != null) {
             firstName = (String) userMetadata.get("first_name");
             lastName = (String) userMetadata.get("last_name");
             if (userMetadata.get("dob") != null) {
                 dob = LocalDate.parse((String) userMetadata.get("dob"));
             }
        }

        // 3. Send everything to the service
        User syncedUser = userService.syncIdentity(id, email, roleStr, firstName, lastName, dob);
        return ResponseEntity.ok(syncedUser);
    }

}
