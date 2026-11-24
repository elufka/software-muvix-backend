package com.muvix.infrastructure.adapters.in.rest.mapper;

import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.in.rest.controller.auth.dto.AuthDtos.RegisterRequest;
import com.muvix.infrastructure.adapters.in.rest.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserRestMapper {

    public User toDomain(RegisterRequest request) {
        if (request == null) return null;
        return new User(
                null,
                request.photo(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.country(),
                request.province(),
                request.city(),
                request.address(),
                User.Role.USER
        );
    }

    public UserResponse toResponse(User domain, boolean isMobbexLinked) {
        if (domain == null) return null;
        return new UserResponse(
                domain.getId(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getEmail(),
                domain.getPhone(),
                isMobbexLinked
        );
    }
}