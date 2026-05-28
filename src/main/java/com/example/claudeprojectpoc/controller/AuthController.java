package com.example.claudeprojectpoc.controller;

import com.example.claudeprojectpoc.model.AuthResponse;
import com.example.claudeprojectpoc.model.LoginRequest;
import com.example.claudeprojectpoc.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ReactiveUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(ReactiveUserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Mono<AuthResponse> login(@RequestBody LoginRequest request) {
        return userDetailsService.findByUsername(request.getUsername())
                .onErrorResume(e -> Mono.empty())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> new AuthResponse(jwtService.generateToken(user.getUsername())))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")));
    }
}
