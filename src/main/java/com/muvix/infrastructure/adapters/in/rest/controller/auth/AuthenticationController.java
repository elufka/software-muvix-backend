package com.muvix.infrastructure.adapters.in.rest.controller.auth;

import com.muvix.application.port.in.security.TokenAuthenticationUseCase;
import com.muvix.application.port.in.user.RegisterUserUseCase;
import com.muvix.infrastructure.adapters.in.rest.controller.auth.dto.AuthDtos.*;
import com.muvix.infrastructure.adapters.in.rest.controller.auth.mapper.AuthRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final TokenAuthenticationUseCase authenticationUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthRestMapper mapper;

    public AuthenticationController(TokenAuthenticationUseCase authenticationUseCase,
                                    RegisterUserUseCase registerUserUseCase,
                                    AuthRestMapper mapper) {
        this.authenticationUseCase = authenticationUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.mapper = mapper;
    }

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
}
