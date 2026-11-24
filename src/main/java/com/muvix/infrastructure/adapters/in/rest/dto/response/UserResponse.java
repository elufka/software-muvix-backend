package com.muvix.infrastructure.adapters.in.rest.dto.response;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        boolean isMobbexLinked
) {}