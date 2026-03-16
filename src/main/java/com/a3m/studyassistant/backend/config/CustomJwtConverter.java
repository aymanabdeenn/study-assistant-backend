package com.a3m.studyassistant.backend.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    // The default converter handles standard scopes like 'openid' or 'profile'
    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Get standard authorities (like SCOPE_read)
        Collection<GrantedAuthority> authorities = defaultAuthoritiesConverter.convert(jwt);

        // 2. Extract custom role from Supabase 'app_metadata'
        Map<String, Object> appMetadata = jwt.getClaim("app_metadata");

        if (appMetadata != null && appMetadata.containsKey("role")) {
            String role = (String) appMetadata.get("role") == null ? "STUDENT" : (String) appMetadata.get("role"); // e.g., "ADMIN"

            // 3. Add the ROLE_ prefix so @PreAuthorize("hasRole('ADMIN')") works
            GrantedAuthority customRole = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

            // Combine standard scopes with your custom role
            authorities = Stream.concat(authorities.stream(), Stream.of(customRole))
                    .collect(Collectors.toSet());
        }

        // 4. Return the Token object that Spring puts in the SecurityContext
        // We use jwt.getSubject() (the UUID) as the "name" of the user
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

//    @Override
//    public AbstractAuthenticationToken convert(Jwt jwt) {
//        // 1. Get standard authorities (like SCOPE_read)
//        Collection<GrantedAuthority> authorities = defaultAuthoritiesConverter.convert(jwt);
//
//        // 2. Extract custom role from Supabase 'app_metadata'
//        Map<String, Object> appMetadata = jwt.getClaim("app_metadata");
//
//        String role = (appMetadata != null && appMetadata.containsKey("role")) ? (String) appMetadata.get("role") : "SERVICE";
//
//        // 3. Add the ROLE_ prefix so @PreAuthorize("hasRole('ADMIN')") works
//        GrantedAuthority customRole = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
//
//        // Combine standard scopes with your custom role
//        authorities = Stream.concat(authorities.stream(), Stream.of(customRole))
//                    .collect(Collectors.toSet());
//
//        // 4. Return the Token object that Spring puts in the SecurityContext
//        // We use jwt.getSubject() (the UUID) as the "name" of the user
//        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
//    }
}
