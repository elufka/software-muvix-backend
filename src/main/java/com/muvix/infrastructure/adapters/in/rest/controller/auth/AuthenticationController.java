package com.muvix.infrastructure.adapters.in.rest.controller.auth;

import com.muvix.application.port.in.security.TokenAuthenticationUseCase;
import com.muvix.application.port.in.user.GetCurrentUserUseCase;
import com.muvix.application.port.in.user.RegisterUserUseCase;
import com.muvix.infrastructure.adapters.in.rest.controller.auth.dto.AuthDtos;
import com.muvix.infrastructure.adapters.in.rest.controller.auth.dto.AuthDtos.*;
import com.muvix.infrastructure.adapters.in.rest.controller.auth.mapper.AuthRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final TokenAuthenticationUseCase authenticationUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthRestMapper mapper;
    private final GetCurrentUserUseCase getCurrentUserUseCase;


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = registerUserUseCase.registerUser(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenAuthenticationUseCase.AccessToken> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationUseCase.login(mapper.toCommand(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenAuthenticationUseCase.AccessToken> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationUseCase.refreshToken(mapper.toCommand(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticationUseCase.logout(mapper.toCommand(request));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthDtos.UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = Long.valueOf(jwt.getSubject());
        var user = getCurrentUserUseCase.getById(userId);
        return ResponseEntity.  ok(mapper.toUserResponse(user));
    }
}
