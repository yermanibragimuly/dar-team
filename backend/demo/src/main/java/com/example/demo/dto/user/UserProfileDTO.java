    package com.example.demo.dto.user;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.time.LocalDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class UserProfileDTO {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private String phone;
        private String bio;
        private LocalDate birthDate;
    }
