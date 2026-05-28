package com.example.claudeprojectpoc.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Validates the JWT extracted by {@link JwtAuthenticationConverter} and produces
 * an authenticated {@link Authentication} for the Spring Security context.
 *
 * <p>This is the second step in the WebFlux authentication pipeline. It delegates
 * signature verification and expiry checking to {@link JwtService}. If the token is
 * valid, it returns a fully authenticated token with {@code ROLE_USER} granted authority.
 * If invalid or expired, it returns {@code Mono.empty()}, which causes Spring Security
 * to treat the request as unauthenticated and return {@code 401 Unauthorized}.
 *
 * <p>The authentication is stateless — no session is created. Each request must carry
 * a valid JWT. This is enforced by {@link SecurityConfig} via
 * {@code NoOpServerSecurityContextRepository}.
 */
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    public JwtAuthenticationManager(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Validates the JWT in {@code authentication.credentials} and returns an authenticated
     * principal if the token is valid, or {@code Mono.empty()} if it is not.
     *
     * @param authentication unauthenticated token produced by {@link JwtAuthenticationConverter}
     * @return authenticated {@link Authentication} wrapping the username, or empty on failure
     */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        return jwtService.validateTokenAndGetUsername(token)
                .map(username -> (Authentication) new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                ));
    }
}
