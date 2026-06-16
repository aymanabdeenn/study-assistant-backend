package com.a3m.studyassistant.backend.features.user;

import com.a3m.studyassistant.backend.features.user.dto.UserUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncUserAtSignupOrLogin(@AuthenticationPrincipal Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");

        Map<String, Object> appMetadata = jwt.getClaimAsMap("app_metadata");
        String role = (appMetadata != null && appMetadata.containsKey("role"))
                ? (String) appMetadata.get("role")
                : "STUDENT";

        Map<String, Object> userMetadata = jwt.getClaimAsMap("user_metadata");
        String firstName = null;
        String lastName = null;
        LocalDate dob = null;

        if (userMetadata != null) {
            firstName = (String) userMetadata.get("first_name");
            lastName = (String) userMetadata.get("last_name");
            if (userMetadata.get("dob") != null) {
                try {
                    dob = LocalDate.parse((String) userMetadata.get("dob"));
                } catch (Exception e) {
                    System.out.println("Invalid date format for user " + email);
                }
            }
        }

        User syncedUser = userService.syncIdentity(id, email, role, firstName, lastName, dob);
        return ResponseEntity.ok(syncedUser);
    }

    @PatchMapping("/profile/{userId}")
    @PreAuthorize("#userId.toString() == authentication.name")
    public ResponseEntity<?> updateProfile(@PathVariable UUID userId, @RequestBody UserUpdateDTO dto) {
         User user = userService.updateProfile(userId, dto.getEmail(), dto.getFirstName(), dto.getLastName(), dto.getDob());
         return ResponseEntity.ok("The user " + user.getEmail() + " has been updated successfully!");
    }

    @GetMapping
    public ResponseEntity<User> getUser(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

}
