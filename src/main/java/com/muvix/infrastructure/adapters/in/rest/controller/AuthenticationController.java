package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.security.TokenAuthenticationUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record EnableTwoFactorRequest(@NotBlank @Email String email,
                                         @NotBlank String password,
                                         @NotBlank String verificationCode) {}
    public record VerifyTwoFactorRequest(@NotBlank String challengeToken,
                                         @NotBlank String verificationCode) {}
    public record DisableTwoFactorRequest(@NotBlank String password,
                                         @NotBlank String verificationCode) {}

    private final TokenAuthenticationUseCase authenticationUseCase;

    public AuthenticationController(TokenAuthenticationUseCase authenticationUseCase) {
        this.authenticationUseCase = authenticationUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenAuthenticationUseCase.LoginResult> login(
            @Valid @RequestBody LoginRequest request) {
        var result = authenticationUseCase.login(
                new TokenAuthenticationUseCase.LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<TokenAuthenticationUseCase.EnableTwoFactorResult> enableTwoFactor(
            @Valid @RequestBody EnableTwoFactorRequest request) {
        var result = authenticationUseCase.enableTwoFactor(
                new TokenAuthenticationUseCase.EnableTwoFactorCommand(
                        request.email(), request.password(), request.verificationCode()));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<TokenAuthenticationUseCase.AccessToken> verifyTwoFactor(
            @Valid @RequestBody VerifyTwoFactorRequest request) {
        var result = authenticationUseCase.verifyTwoFactor(
                new TokenAuthenticationUseCase.TwoFactorChallengeCommand(
                        request.challengeToken(), request.verificationCode()));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<Void> disableTwoFactor(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DisableTwoFactorRequest request) {
        authenticationUseCase.disableTwoFactor(
                Long.valueOf(jwt.getSubject()), request.password(), request.verificationCode());
        return ResponseEntity.noContent().build();
    }
}
