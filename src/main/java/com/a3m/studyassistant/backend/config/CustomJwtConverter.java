package com.a3m.studyassistant.backend.config;

import com.a3m.studyassistant.backend.features.user.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CustomJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    public CustomJwtConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        // 1. Fetch Role from Database instead of JWT Claims
        Collection<GrantedAuthority> authorities = userRepository.findById(userId)
                .map(user -> Collections.<GrantedAuthority>singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ))
                .orElse(Collections.emptyList()); // No roles for new/unsynced users

        // 2. Return the token with the DB-sourced authorities
        return new JwtAuthenticationToken(jwt, authorities);
    }
}
