package com.muvix.infrastructure.adapters.in.rest.dto.response;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        boolean isMobbexLinked
) {}