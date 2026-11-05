package com.muvix.infrastructure.adapters.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "El teléfono es obligatorio")
        String phone
) {}
