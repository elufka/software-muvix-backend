package com.muvix.application.port.in.security;

import java.util.List;

public interface TokenAuthenticationUseCase {

    enum LoginStatus { TWO_FACTOR_SETUP_REQUIRED, TWO_FACTOR_CHALLENGE }

    record LoginCommand(String email, String password) {}

    record LoginResult(LoginStatus status, String challengeToken, String provisioningUri) {}

    record EnableTwoFactorCommand(String email, String password, String verificationCode) {}

    record TwoFactorChallengeCommand(String challengeToken, String verificationCode) {}

    record AccessToken(String accessToken, String tokenType, long expiresInSeconds) {}

    record EnableTwoFactorResult(List<String> recoveryCodes) {}

    LoginResult login(LoginCommand command);

    EnableTwoFactorResult enableTwoFactor(EnableTwoFactorCommand command);

    AccessToken verifyTwoFactor(TwoFactorChallengeCommand command);

    void disableTwoFactor(Long userId, String password, String verificationCode);
}
