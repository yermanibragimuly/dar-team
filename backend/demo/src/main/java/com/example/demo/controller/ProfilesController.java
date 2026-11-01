package com.example.demo.controller;

import com.example.demo.dto.user.UserProfileDTO;
import com.example.demo.entity.users.User;
import com.example.demo.entity.users.UserProfile;
import com.example.demo.repository.roles.UserProfileRepository;
import com.example.demo.repository.roles.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class ProfilesController {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public  ProfilesController(
    UserProfileRepository userProfileRepository, UserRepository userRepository
    ) {
    this.userProfileRepository = userProfileRepository;
    this.userRepository = userRepository;
    }


    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(Authentication auth){
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setPhone(profile.getPhone());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setBio(profile.getBio());
        dto.setBirthDate(profile.getBirthDate());

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(@RequestBody UserProfileDTO req,
                                                        Authentication auth) {
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (req.getFirstName() != null) profile.setFirstName(req.getFirstName());
        if (req.getLastName()  != null) profile.setLastName(req.getLastName());
        if (req.getPhone()     != null) profile.setPhone(req.getPhone());
        if (req.getBio()       != null) profile.setBio(req.getBio());
        if (req.getBirthDate() != null) profile.setBirthDate(req.getBirthDate());

        userProfileRepository.save(profile);

        // Возвращаем обновлённый DTO
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setPhone(profile.getPhone());
        dto.setBio(profile.getBio());
        dto.setBirthDate(profile.getBirthDate());

        return ResponseEntity.ok(dto);
    }

}
