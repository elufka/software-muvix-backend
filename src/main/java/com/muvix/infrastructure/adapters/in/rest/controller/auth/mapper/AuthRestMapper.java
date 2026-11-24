package com.muvix.infrastructure.adapters.in.rest.controller.auth.mapper;

import com.muvix.application.port.in.security.TokenAuthenticationUseCase;
import com.muvix.application.port.in.user.RegisterUserUseCase;
import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.in.rest.controller.auth.dto.AuthDtos.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthRestMapper {
    public RegisterUserUseCase.RegisterUserCommand toCommand(RegisterRequest r) {
        return new RegisterUserUseCase.RegisterUserCommand(
                r.photo(),
                r.firstName(),
                r.lastName(),
                r.email(),
                r.phone(),
                r.country(),
                r.province(),
                r.city(),
                r.address(),
                r.password()
        );
    }
    public RegisterResponse toResponse(User user) {
        return new RegisterResponse(user.getId(), user.getFirstName() + user.getLastName(), user.getEmail());
    }
    public TokenAuthenticationUseCase.LoginCommand toCommand(LoginRequest r) {
        return new TokenAuthenticationUseCase.LoginCommand(r.email(), r.password());
    }
    public TokenAuthenticationUseCase.RefreshTokenCommand toCommand(RefreshTokenRequest r) {
        return new TokenAuthenticationUseCase.RefreshTokenCommand(r.refreshToken());
    }

    public UserResponse toUserResponse(User user) {
        List<UserResponse.RoleResponse> roles = (user.getRole() == null)
                ? List.of()
                : List.of(new UserResponse.RoleResponse(user.getRole().name()));

        return new UserResponse(
                user.getId(),
                user.getPhoto(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getCountry(),
                user.getProvince(),
                user.getCity(),
                user.getAddress(),
                roles
        );
    }
}
