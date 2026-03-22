package com.a3m.studyassistant.backend.features.admin;

import com.a3m.studyassistant.backend.features.user.User;
import com.a3m.studyassistant.backend.features.user.UserNotFoundException;
import com.a3m.studyassistant.backend.features.user.UserRepository;
import com.a3m.studyassistant.backend.features.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;

    @Value("${supabase.project.url}")
    private String supabaseProjectUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Autowired
    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void promoteToAdmin(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with id" + userId + "couldn't be found!"));
        user.setRole(UserRole.ADMIN);
        updateUserRoleInAuthProvider(userId, user.getRole().toString());
    }

    private void updateUserRoleInAuthProvider(UUID userId, String role) {
        String url = supabaseProjectUrl + "/auth/v1/admin/users/" + userId;

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("app_metadata", Map.of("role", role));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
    }

}
