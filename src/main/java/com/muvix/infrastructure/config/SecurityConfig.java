package com.muvix.infrastructure.config;

import com.muvix.application.port.out.security.UserAuthenticationPort;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/users/register", "/api/v1/users/connect/callback",
                                "/api/v1/auth/login",
                                "/api/v1/auth/2fa/enable", "/api/v1/auth/2fa/verify",
                                "/api/v1/escrow/webhook/mobbex",
                                "/api/v1/locations/**")
                        .permitAll()
                        .anyRequest().hasAuthority("SCOPE_access"))
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${muvix.security.jwt.secret}") String secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey(secret)));
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${muvix.security.jwt.secret}") String secret,
                          UserAuthenticationPort authenticationPort) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtKey(secret))
                .macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt> accountValidator = token -> {
            if (!"access".equals(token.getClaimAsString("scope"))) return OAuth2TokenValidatorResult.success();
            try {
                Long userId = Long.valueOf(token.getSubject());
                boolean allowed = authenticationPort.findById(userId)
                        .map(account -> account.status() == UserAuthenticationPort.AccountStatus.ACTIVE
                                || account.status() == UserAuthenticationPort.AccountStatus.ERASURE_PENDING_TRANSACTIONS
                                || account.status() == UserAuthenticationPort.AccountStatus.ERASURE_CONFIRMATION_REQUIRED)
                        .orElse(false);
                return allowed ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Account is not active", null));
            } catch (NumberFormatException exception) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid subject", null));
            }
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), accountValidator));
        return decoder;
    }

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }

    private static SecretKey jwtKey(String secret) {
        byte[] keyBytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("muvix.security.jwt.secret must contain at least 32 UTF-8 bytes");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .forEach(authorities::add);
            }
            return authorities;
        });
        return converter;
    }
}
