package com.muvix.infrastructure.adapters.in.rest.controller.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public sealed interface AuthDtos {

    record RegisterRequest(@NotBlank String name, @NotBlank @Email String email, @NotBlank String phone, @NotBlank @Size(min = 8, max = 72) String password) implements AuthDtos {}
    record RegisterResponse(Long userId, String name, String email) implements AuthDtos {}
    record LoginRequest(@NotBlank @Email String email, @NotBlank String password) implements AuthDtos {}
    record RefreshTokenRequest(@NotBlank String refreshToken) implements AuthDtos {}

}
