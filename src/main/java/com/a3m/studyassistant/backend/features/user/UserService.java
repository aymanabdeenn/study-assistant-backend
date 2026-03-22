package com.a3m.studyassistant.backend.features.user;

import com.a3m.studyassistant.backend.features.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AdminService adminService;

    @Value("${app.admin.bootstrap-email}")
    private String bootstrapEmail;

    @Autowired
    public UserService(UserRepository userRepository, AdminService adminService) {
        this.userRepository = userRepository;
        this.adminService = adminService;
    }

    public User getUserById(UUID uuid) {
        return userRepository.findById(uuid).orElseThrow(() -> new UserNotFoundException("User with id " + uuid + "couldn't be found!"));
    }

    @Transactional
    public User syncIdentity(UUID id, String email, String role, String firstName, String lastName, LocalDate dob) {
         User user = userRepository.findById(id)
            .map(existingUser -> {
                // "Self-healing": Update core fields if Supabase has new info
                if(email != null) existingUser.setEmail(email);
                if(firstName != null) existingUser.setFirstName(firstName);
                if(lastName != null) existingUser.setLastName(lastName);
                return existingUser;
            })
            .orElseGet(() -> {
                // First time registration
                User newUser = new User();
                newUser.setId(id);
                newUser.setEmail(email);
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setDob(dob);
                newUser.setRole(UserRole.STUDENT);
                return userRepository.save(newUser);
            });

        if(email.equalsIgnoreCase(bootstrapEmail) && user.getRole() != UserRole.ADMIN) {
            adminService.promoteToAdmin(id);
        }

        return user;
    }

    @Transactional
    public User updateProfile(UUID userId, String email, String firstName, String lastName, LocalDate dob) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with id " + userId + "couldn't be found!"));
        if(email != null) user.setEmail(email);
        if(firstName != null) user.setFirstName(firstName);
        if(lastName != null) user.setLastName(lastName);
        if(dob != null) user.setDob(dob);
        return user;
    }

}
