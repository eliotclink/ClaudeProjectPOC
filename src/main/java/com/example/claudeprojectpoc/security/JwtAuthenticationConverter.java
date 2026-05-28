package com.example.claudeprojectpoc.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Extracts a JWT from the {@code Authorization: Bearer <token>} header of an incoming request.
 *
 * <p>This is the first step in the WebFlux authentication pipeline. It converts a raw HTTP
 * request into an unauthenticated {@link Authentication} object holding the raw token string.
 * The token is not validated here — that responsibility belongs to {@link JwtAuthenticationManager}.
 *
 * <p>Returns {@code Mono.empty()} if the header is absent or does not start with {@code "Bearer "},
 * which causes Spring Security to skip JWT authentication for that request (the authorization
 * layer will then enforce access rules independently).
 */
@Component
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) return Mono.empty();
        String token = auth.substring(7);
        return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
    }
}
