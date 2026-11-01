package com.example.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAdminDto {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private Long branchId;
    private String branchName;
}