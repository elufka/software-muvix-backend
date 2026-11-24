package com.muvix.infrastructure.adapters.in.rest.controller.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public sealed interface AuthDtos {

    record RegisterRequest(
            String photo,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            @NotBlank String phone,
            @NotBlank String country,
            @NotBlank String province,
            @NotBlank String city,
            @NotBlank String address,
            @NotBlank @Size(min = 8, max = 72) String password
    ) implements AuthDtos {}

    record RegisterResponse(Long userId, String fullName, String email) implements AuthDtos {}
    record LoginRequest(@NotBlank @Email String email, @NotBlank String password) implements AuthDtos {}
    record RefreshTokenRequest(@NotBlank String refreshToken) implements AuthDtos {}

    record UserResponse(
            Long id,
            String photo,
            String firstName,
            String lastName,
            String email,
            String phone,
            String country,
            String province,
            String city,
            String address,
            List<RoleResponse> roles
    ) implements AuthDtos {
        public record RoleResponse(String name) {}
    }
}
