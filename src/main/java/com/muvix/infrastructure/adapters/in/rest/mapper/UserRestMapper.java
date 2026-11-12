package com.muvix.infrastructure.adapters.in.rest.mapper;

import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.in.rest.dto.request.CreateUserRequest;
import com.muvix.infrastructure.adapters.in.rest.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserRestMapper {

    public User toDomain(CreateUserRequest request) {
        if (request == null) return null;
        return new User(null, request.name(), request.email(), request.phone());
    }

    public UserResponse toResponse(User domain, boolean isMobbexLinked) {
        if (domain == null) return null;
        return new UserResponse(
                domain.getId(),
                domain.getName(),
                domain.getEmail(),
                domain.getPhone(),
                isMobbexLinked
        );
    }
}