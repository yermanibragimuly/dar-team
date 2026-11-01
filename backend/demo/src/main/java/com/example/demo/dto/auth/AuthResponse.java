package com.example.demo.dto.auth;

import lombok.Data;

import java.util.Set;

@Data
public class AuthResponse {
    private String token;
    private long   expiresAt;
    private String username;
    private Set<String> roles;
    public AuthResponse(String token, long expiresAt, String username, Set<String> roles) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.username = username;
        this.roles = roles;
    }
}
