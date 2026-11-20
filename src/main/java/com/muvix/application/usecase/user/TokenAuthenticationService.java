package com.muvix.application.usecase.user;

import com.muvix.application.port.in.security.TokenAuthenticationUseCase;
import com.muvix.application.port.out.security.RefreshTokenPort;
import com.muvix.application.port.out.security.UserAuthenticationPort;
import com.muvix.application.port.out.security.UserAuthenticationPort.AccountStatus;
import com.muvix.application.port.out.security.UserAuthenticationPort.AuthenticationAccount;
import com.muvix.domain.model.exception.DomainException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TokenAuthenticationService implements TokenAuthenticationUseCase {

    private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(7);
    private static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);

    private final UserAuthenticationPort authenticationPort;
    private final RefreshTokenPort refreshTokenPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final Clock clock;

    public TokenAuthenticationService(UserAuthenticationPort authenticationPort,
                                      RefreshTokenPort refreshTokenPort,
                                      PasswordEncoder passwordEncoder,
                                      JwtEncoder jwtEncoder,
                                      JwtDecoder jwtDecoder,
                                      Clock clock) {
        this.authenticationPort = authenticationPort;
        this.refreshTokenPort = refreshTokenPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AccessToken login(LoginCommand command) {
        AuthenticationAccount account = authenticatePassword(command.email(), command.password());
        authenticationPort.recordSuccessfulLogin(account.userId(), clock.instant());
        return issueTokenPair(account.userId(), account.role());
    }

    @Override
    @Transactional
    public AccessToken refreshToken(RefreshTokenCommand command) {
        if (command == null) throw new DomainException("Refresh token is required");
        Jwt refreshJwt = decodeRefreshToken(command.refreshToken());
        Long userId = parseSubject(refreshJwt);
        String tokenId = refreshJwt.getId();

        if (tokenId == null || !refreshTokenPort.consume(tokenId, userId, clock.instant())) {
            refreshTokenPort.revokeAllForUser(userId);
            throw new DomainException("Invalid or already used refresh token");
        }

        AuthenticationAccount account = authenticationPort.findById(userId)
                .orElseThrow(() -> new DomainException("Invalid refresh token"));
        requireLoginAllowed(account);

        return issueTokenPair(userId, account.role());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenCommand command) {
        if (command == null || command.refreshToken() == null || command.refreshToken().isBlank()) return;
        try {
            Jwt refreshJwt = decodeRefreshToken(command.refreshToken());
            if (refreshJwt.getId() != null) {
                refreshTokenPort.revoke(refreshJwt.getId());
            }
        } catch (DomainException | JwtException ignored) {
        }
    }

    private AccessToken issueTokenPair(Long userId, String role) {
        Instant now = clock.instant();
        String accessToken = issueToken(userId, "access", ACCESS_TOKEN_LIFETIME, role, null);

        String refreshTokenId = UUID.randomUUID().toString();
        Instant refreshExpiresAt = now.plus(REFRESH_TOKEN_LIFETIME);
        String refreshToken = issueToken(userId, "refresh", REFRESH_TOKEN_LIFETIME, null, refreshTokenId);

        refreshTokenPort.store(refreshTokenId, userId, refreshExpiresAt);

        return new AccessToken(accessToken, refreshToken, "Bearer", ACCESS_TOKEN_LIFETIME.toSeconds());
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

    private Jwt decodeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) throw new DomainException("Refresh token is required");
        try {
            Jwt jwt = jwtDecoder.decode(rawToken);
            if (!"refresh".equals(jwt.getClaimAsString("scope"))) {
                throw new DomainException("Invalid token scope");
            }
            return jwt;
        } catch (JwtException ex) {
            throw new DomainException("Invalid or expired refresh token");
        }
    }

    private Long parseSubject(Jwt jwt) {
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new DomainException("Invalid token subject");
        }
    }

    private String issueToken(Long userId, String scope, Duration lifetime, String role, String jti) {
        Instant now = clock.instant();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("muvix")
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .subject(userId.toString())
                .claim("scope", scope);
        if (jti != null) claimsBuilder.id(jti);
        if (role != null) claimsBuilder.claim("roles", List.of(role));
        JwtClaimsSet claims = claimsBuilder.build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
