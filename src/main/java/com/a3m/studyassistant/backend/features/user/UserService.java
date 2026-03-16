package com.a3m.studyassistant.backend.features.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // RESPONSIBILITY 1: Keep the ID and Role correct
    @Transactional
    public User syncIdentity(UUID id, String email, String role, String firstName, String lastName, LocalDate dob) {
        UserRole incomingRole = "ADMIN".equalsIgnoreCase(role) ? UserRole.ADMIN : UserRole.STUDENT;

        return userRepository.findById(id)
            .map(user -> {
                if (user.getRole() != incomingRole) {
                    user.setRole(incomingRole);
                    return userRepository.save(user);
                }
                return user;
            })
            .orElseGet(() -> {
                // First time we've ever seen this UUID
                User newUser = new User();
                newUser.setId(id);
                newUser.setEmail(email);
                newUser.setRole(incomingRole);
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setDob(dob);
                return userRepository.save(newUser);
            });
    }

    // RESPONSIBILITY 2: Handle the "Human" data
    @Transactional
    public User updateProfile(UUID id, String firstName, String lastName, LocalDate dob) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setDob(dob);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
