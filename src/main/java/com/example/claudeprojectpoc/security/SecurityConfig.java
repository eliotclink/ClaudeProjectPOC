package com.example.claudeprojectpoc.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

/**
 * Spring Security configuration for the reactive WebFlux application.
 *
 * <h2>Authentication flow</h2>
 * <ol>
 *   <li>Client calls {@code POST /api/auth/login} with username and password.</li>
 *   <li>{@link com.example.claudeprojectpoc.controller.AuthController} validates credentials
 *       against the {@link ReactiveUserDetailsService} using Argon2id password hashing,
 *       then returns a JWT signed with ML-DSA-87.</li>
 *   <li>Client includes the JWT in subsequent requests via the
 *       {@code Authorization: Bearer <token>} header.</li>
 *   <li>{@link JwtAuthenticationConverter} extracts the raw token from the header.</li>
 *   <li>{@link JwtAuthenticationManager} validates the ML-DSA-87 signature and expiry,
 *       then populates the security context with the authenticated principal.</li>
 * </ol>
 *
 * <h2>Key security decisions</h2>
 * <ul>
 *   <li><b>Stateless:</b> {@code NoOpServerSecurityContextRepository} disables server-side
 *       sessions entirely. Every request is authenticated independently via its JWT.</li>
 *   <li><b>CSRF disabled:</b> Safe for stateless REST APIs that do not use cookies for auth.</li>
 *   <li><b>Argon2id passwords:</b> Resistant to GPU and side-channel attacks. Also quantum-safe
 *       since it relies on memory-hard hashing, not asymmetric cryptography.</li>
 *   <li><b>In-memory user store:</b> Suitable for a POC. Replace with a database-backed
 *       {@link ReactiveUserDetailsService} in production.</li>
 * </ul>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Argon2id password encoder using Spring Security's recommended defaults.
     * Argon2id is the winner of the Password Hashing Competition and is resistant
     * to both GPU brute-force attacks and quantum-assisted search (Grover's algorithm
     * provides at most a square-root speedup, which Argon2id's memory-hardness negates).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    /**
     * In-memory user store with a single admin account.
     * Passwords are hashed with Argon2id at bean creation time — plain-text credentials
     * are never stored.
     */
    @Bean
    public ReactiveUserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        return new MapReactiveUserDetailsService(admin);
    }

    /**
     * Configures the security filter chain:
     * <ul>
     *   <li>{@code POST /api/auth/login} is public — this is where clients obtain a JWT.</li>
     *   <li>All other endpoints require a valid ML-DSA-87 signed JWT.</li>
     *   <li>The custom JWT filter is inserted at the standard {@code AUTHENTICATION} position
     *       in the WebFlux filter order.</li>
     * </ul>
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationManager authManager,
            JwtAuthenticationConverter authConverter) {

        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authManager);
        jwtFilter.setServerAuthenticationConverter(authConverter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .build();
    }
}
