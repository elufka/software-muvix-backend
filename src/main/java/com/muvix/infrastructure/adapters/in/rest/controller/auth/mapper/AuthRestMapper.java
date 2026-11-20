package com.muvix.infrastructure.adapters.in.rest.controller.auth.mapper;

import com.muvix.application.port.in.security.TokenAuthenticationUseCase;
import com.muvix.application.port.in.user.RegisterUserUseCase;
import com.muvix.domain.model.user.User;
import com.muvix.infrastructure.adapters.in.rest.controller.auth.dto.AuthDtos.*;
import org.springframework.stereotype.Component;

@Component
public class AuthRestMapper {
    public RegisterUserUseCase.RegisterUserCommand toCommand(RegisterRequest r) {
        return new RegisterUserUseCase.RegisterUserCommand(r.name(), r.email(), r.phone(), r.password());
    }
    public RegisterResponse toResponse(User user) {
        return new RegisterResponse(user.getId(), user.getName(), user.getEmail());
    }
    public TokenAuthenticationUseCase.LoginCommand toCommand(LoginRequest r) {
        return new TokenAuthenticationUseCase.LoginCommand(r.email(), r.password());
    }
    public TokenAuthenticationUseCase.RefreshTokenCommand toCommand(RefreshTokenRequest r) {
        return new TokenAuthenticationUseCase.RefreshTokenCommand(r.refreshToken());
    }
}
