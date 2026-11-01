package com.example.demo.controller;

import com.example.demo.dto.auth.*;
import com.example.demo.dto.user.ForgotPasswordRequestDTO;
import com.example.demo.dto.user.ResetPasswordRequestDTO;
import com.example.demo.entity.users.*;
import com.example.demo.repository.roles.UserProfileRepository;
import com.example.demo.repository.roles.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.users.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService,
                          UserRepository userRepository, PasswordEncoder passwordEncoder,
                            UserService userService,  UserProfileRepository userProfileRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error","требуется имя пользователя/пароль"));
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error","Имя пользователя уже занято"));
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Электронная почта, которая уже использовалась"));
        }

        try {
            // Хешируем пароль сразу, чтобы не хранить его в Redis открытым
            req.setPassword(passwordEncoder.encode(req.getPassword()));

            // Превращаем объект req в JSON-строку
            String jsonRequest = new ObjectMapper().writeValueAsString(req);

            userService.initiateUserconfirm(req.getEmail(), jsonRequest);

            return ResponseEntity.ok(Map.of("message", "Письмо с подтверждением отправлено на " + req.getEmail()));

        }catch (JsonProcessingException ex){
            return ResponseEntity.internalServerError().body("Error during activation");
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam String token) {
        // Достаем строку из Redis
        String jsonRequest = userService.completeRegistration(token);

        if (jsonRequest == null) {
            return ResponseEntity.badRequest().body("Link expired or invalid");
        }

        try {
            // ПРЕВРАЩАЕМ JSON ОБРАТНО В ОБЪЕКТ RegisterRequest
            RegisterRequest req = objectMapper.readValue(jsonRequest, RegisterRequest.class);

            // Сохраняем в PostgreSQL
            User user = new User();
            user.setUsername(req.getUsername());
            user.setPassword(req.getPassword()); // Пароль уже захеширован
            user.setEmail(req.getEmail());
            user.setRoles(Collections.singleton(Role.ROLE_USER));
            userRepository.save(user);

            UserProfile profile = new UserProfile();
            profile.setUser(user);
            userProfileRepository.save(profile);

            return ResponseEntity.ok("Account activated!");

        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().body("Error during activation");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            UserDetails principal = (UserDetails) auth.getPrincipal();
            Set<String> roles = principal.getAuthorities().stream()
                    .map(a -> a.getAuthority()).collect(Collectors.toSet());

            String token = jwtService.generateToken(principal, roles);
            long expiresAt = System.currentTimeMillis() + jwtService.getExpirationMs();

            return ResponseEntity.ok(new AuthResponse(token, expiresAt, principal.getUsername(), roles));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    // Шаг 1: Инициировать сброс (запросить код)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO req) {
        try {
            userService.initiatePasswordReset(req.getLoginOrEmail());
            return ResponseEntity.ok(Map.of("message", "Код подтверждения отправлен на вашу почту"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            // Best Practice: Не говорим, что пользователя нет, чтобы не помогать хакерам
            return ResponseEntity.ok(Map.of("message", "Если такой пользователь существует, мы отправили код."));
        }
    }

    // Шаг 2: Подтвердить код и установить новый пароль
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO req) {
        try {
            userService.confirmResetAndChangePassword(req.getEmail(), req.getCode(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Пароль успешно изменен"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body(Map.of("error","Missing bearer token"));
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        Set<String> roles = jwtService.extractRoles(token);
        return ResponseEntity.ok(Map.of("username", username, "roles", roles));
    }
}
