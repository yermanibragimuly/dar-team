package com.example.demo.service.users;

import com.example.demo.dto.user.UserAdminDto;
import com.example.demo.entity.shop.Branch;
import com.example.demo.entity.users.Role;
import com.example.demo.entity.users.User;
import com.example.demo.repository.roles.UserRepository;
import com.example.demo.repository.shop.BranchRepository;
import com.example.demo.service.Email.EmailService;
import com.example.demo.service.redis.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private BranchRepository branchRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public void initiatePasswordReset(String loginOrEmail) {
        User user = userRepository.findByUsernameOrEmail(loginOrEmail, loginOrEmail)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String code = String.valueOf(100000 + secureRandom.nextInt(900000));
        otpService.saveCode(user.getEmail(), code);
        emailService.sendOtpCode(user.getEmail(), code);
        System.out.println("Код сброса для " + user.getEmail() + ": " + code);
    }

    @Transactional
    public void confirmResetAndChangePassword(String email, String userInputCode, String newPassword) {
        String validCode = otpService.getCode(email);
        if (validCode == null) {
            throw new RuntimeException("Срок действия кода истек или код не запрашивался.");
        }
        if (!validCode.equals(userInputCode)) {
            throw new RuntimeException("Неверный код подтверждения.");
        }

        User user = userRepository.findByUsernameOrEmail(email, email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpService.deleteCode(email);
    }

    public void initiateUserconfirm(String email, String jsonRequest) {
        String token = UUID.randomUUID().toString();
        otpService.saveCode(token, jsonRequest);
        emailService.sendRegistrationConfirmLink(email, token);
    }

    public String completeRegistration(String token) {
        String jsonRequest = otpService.getCode(token);
        if (jsonRequest == null) {
            throw new RuntimeException("Ссылка устарела или неверна");
        }
        otpService.deleteCode(token);
        return jsonRequest;
    }

    public List<UserAdminDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    String branchName = null;
                    if (user.getBranchId() != null && branchRepository != null) {
                        branchName = branchRepository.findById(user.getBranchId())
                                .map(Branch::getName)
                                .orElse(null);
                    }
                    return UserAdminDto.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                            .branchId(user.getBranchId())
                            .branchName(branchName)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserRolesAndBranch(Long userId, Set<String> newRoles, Long branchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + userId));

        Set<Role> targetRoles = new HashSet<>();
        if (newRoles != null) {
            for (String r : newRoles) {
                if (r == null || r.isBlank()) continue;
                String normalized = r.trim().toUpperCase();
                if (!normalized.startsWith("ROLE_")) {
                    normalized = "ROLE_" + normalized;
                }
                try {
                    targetRoles.add(Role.valueOf(normalized));
                } catch (IllegalArgumentException e) {
                    System.err.println("Неизвестная роль: " + normalized);
                }
            }
        }

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        } else {
            user.getRoles().clear();
        }
        user.getRoles().addAll(targetRoles);

        // Привязываем филиал для шоп-менеджера
        if (targetRoles.contains(Role.ROLE_SHOP_MANAGER)) {
            user.setBranchId(branchId);
        } else if (!targetRoles.contains(Role.ROLE_ADMIN)) {
            user.setBranchId(null);
        }

        userRepository.saveAndFlush(user);
    }

    @Transactional
    public void deleteUser(Long userId, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + userId + " не найден"));

        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new IllegalStateException("Нельзя удалить собственный аккаунт");
        }

        if (user.getEmail() != null && otpService != null) {
            otpService.deleteCode(user.getEmail());
        }

        userRepository.delete(user);
        userRepository.flush();
    }
}