package com.muvix.application.port.in.security;

public interface TokenAuthenticationUseCase {

    record LoginCommand(String email, String password) {}

    record RefreshTokenCommand(String refreshToken) {}

    record AccessToken(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresInSeconds
    ) {}

    AccessToken login(LoginCommand command);

    AccessToken refreshToken(RefreshTokenCommand command);

    void logout(RefreshTokenCommand command);
}
