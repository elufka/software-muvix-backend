package com.muvix.application.usecase.user;

import com.muvix.application.port.in.security.TokenAuthenticationUseCase;
import com.muvix.application.port.out.security.TwoFactorProviderPort;
import com.muvix.application.port.out.security.UserAuthenticationPort;
import com.muvix.application.port.out.security.UserAuthenticationPort.AuthenticationAccount;
import com.muvix.application.port.out.security.UserAuthenticationPort.AccountStatus;
import com.muvix.domain.model.exception.DomainException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

@Service
public class TokenAuthenticationService implements TokenAuthenticationUseCase {

    private static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);
    private static final Duration TWO_FACTOR_CHALLENGE_LIFETIME = Duration.ofMinutes(5);

    private final UserAuthenticationPort authenticationPort;
    private final TwoFactorProviderPort twoFactorProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final Clock clock;

    public TokenAuthenticationService(UserAuthenticationPort authenticationPort,
                                     TwoFactorProviderPort twoFactorProvider,
                                     PasswordEncoder passwordEncoder,
                                     JwtEncoder jwtEncoder,
                                     JwtDecoder jwtDecoder,
                                     Clock clock) {
        this.authenticationPort = authenticationPort;
        this.twoFactorProvider = twoFactorProvider;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        AuthenticationAccount account = authenticatePassword(command.email(), command.password());
        if (!account.twoFactorEnabled()) {
            String uri = account.twoFactorSecret() == null ? null
                    : twoFactorProvider.createProvisioningUri(account.email(), account.twoFactorSecret());
            return new LoginResult(LoginStatus.TWO_FACTOR_SETUP_REQUIRED, null, uri);
        }

        return new LoginResult(
                LoginStatus.TWO_FACTOR_CHALLENGE,
                issueToken(account.userId(), "two_factor", TWO_FACTOR_CHALLENGE_LIFETIME),
                null
        );
    }

    @Override
    @Transactional
    public EnableTwoFactorResult enableTwoFactor(EnableTwoFactorCommand command) {
        AuthenticationAccount account = authenticatePassword(command.email(), command.password());
        if (account.twoFactorEnabled()) throw new DomainException("Two-factor authentication is already enabled");
        if (account.twoFactorSecret() == null) throw new DomainException("Two-factor setup is unavailable");
        if (!twoFactorProvider.verifyCode(account.twoFactorSecret(), command.verificationCode())) {
            throw new DomainException("Invalid two-factor verification code");
        }

        List<String> recoveryCodes = twoFactorProvider.generateRecoveryCodes();
        HashSet<String> recoveryHashes = new HashSet<>();
        recoveryCodes.forEach(code -> recoveryHashes.add(passwordEncoder.encode(code)));
        authenticationPort.enableTwoFactor(account.userId(), recoveryHashes);
        return new EnableTwoFactorResult(recoveryCodes);
    }

    @Override
    @Transactional
    public AccessToken verifyTwoFactor(TwoFactorChallengeCommand command) {
        Jwt challenge = decodeChallenge(command.challengeToken());
        Long userId = parseSubject(challenge);
        AuthenticationAccount account = authenticationPort.findById(userId)
                .orElseThrow(() -> new DomainException("Invalid authentication challenge"));
        requireLoginAllowed(account);
        if (!account.twoFactorEnabled()) throw new DomainException("Two-factor authentication is not enabled");

        boolean verified = twoFactorProvider.verifyCode(account.twoFactorSecret(), command.verificationCode());
        if (!verified) {
            String candidate = command.verificationCode() == null
                    ? "" : command.verificationCode().trim().toUpperCase(Locale.ROOT);
            for (String codeHash : account.recoveryCodeHashes()) {
                if (passwordEncoder.matches(candidate, codeHash)) {
                    verified = authenticationPort.consumeRecoveryCode(userId, codeHash);
                    if (verified) break;
                }
            }
        }
        if (!verified) throw new DomainException("Invalid two-factor verification code");

        Instant now = clock.instant();
        authenticationPort.recordSuccessfulLogin(userId, now);
        return new AccessToken(issueToken(userId, "access", ACCESS_TOKEN_LIFETIME, account.role()), "Bearer",
                ACCESS_TOKEN_LIFETIME.toSeconds());
    }

    @Override
    @Transactional
    public void disableTwoFactor(Long userId, String password, String verificationCode) {
        AuthenticationAccount account = authenticationPort.findById(userId)
                .orElseThrow(() -> new DomainException("Invalid credentials"));
        requireLoginAllowed(account);
        if (!passwordEncoder.matches(password, account.passwordHash())) throw new DomainException("Invalid credentials");
        if (!account.twoFactorEnabled()
                || !twoFactorProvider.verifyCode(account.twoFactorSecret(), verificationCode)) {
            throw new DomainException("Invalid two-factor verification code");
        }
        authenticationPort.disableTwoFactor(userId);
    }

    private AuthenticationAccount authenticatePassword(String email, String password) {
        if (email == null || password == null) throw new DomainException("Invalid credentials");
        AuthenticationAccount account = authenticationPort.findByEmail(email)
                .orElseThrow(() -> new DomainException("Invalid credentials"));
        requireLoginAllowed(account);
        if (account.passwordHash() == null || !passwordEncoder.matches(password, account.passwordHash())) {
            throw new DomainException("Invalid credentials");
        }
        return account;
    }

    private void requireLoginAllowed(AuthenticationAccount account) {
        if (account.status() != AccountStatus.ACTIVE
                && account.status() != AccountStatus.ERASURE_PENDING_TRANSACTIONS
                && account.status() != AccountStatus.ERASURE_CONFIRMATION_REQUIRED) {
            throw new DomainException("This account is not active");
        }
    }

    private String issueToken(Long userId, String scope, Duration lifetime) {
        return issueToken(userId, scope, lifetime, null);
    }

    private String issueToken(Long userId, String scope, Duration lifetime, String role) {
        Instant now = clock.instant();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("muvix")
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .subject(userId.toString())
                .claim("scope", scope);
        if (role != null) claimsBuilder.claim("roles", List.of(role));
        JwtClaimsSet claims = claimsBuilder.build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private Jwt decodeChallenge(String token) {
        if (token == null || token.isBlank()) throw new DomainException("Authentication challenge is required");
        try {
            Jwt jwt = jwtDecoder.decode(token);
            if (!"two_factor".equals(jwt.getClaimAsString("scope"))) {
                throw new DomainException("Invalid authentication challenge");
            }
            return jwt;
        } catch (JwtException exception) {
            throw new DomainException("Invalid or expired authentication challenge");
        }
    }

    private static Long parseSubject(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new DomainException("Invalid authentication challenge");
        }
    }
}
