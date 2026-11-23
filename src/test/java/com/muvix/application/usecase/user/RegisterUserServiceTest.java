package com.muvix.application.usecase.user;

import com.muvix.application.port.out.persistence.UserRepositoryPort;
import com.muvix.application.port.out.security.TwoFactorProviderPort;
import com.muvix.application.port.out.security.UserAuthenticationPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterUserServiceTest {

    private final FakeUsers users = new FakeUsers();
    private final FakeAuthentication authentication = new FakeAuthentication();
    private final TwoFactorProviderPort twoFactor = new FakeTwoFactor();
    private final PasswordEncoder encoder = new FakePasswordEncoder();
    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(users, authentication, encoder);
    }

    @Test
    void hashesPasswordWithoutEnrollingTwoFactorDuringRegistration() {
        var result = service.registerUser(new com.muvix.application.port.in.user.RegisterUserUseCase.RegisterUserCommand(
                "Buyer", "buyer@example.test", "555", "a-long-password"));

        assertEquals(9L, result.getId());
        assertEquals("Buyer", result.getName());
        assertEquals("bcrypt:a-long-password", authentication.savedPasswordHash);
        assertEquals(null, authentication.savedTotpSecret);
    }

    @Test
    void rejectsDuplicateEmailBeforeCreatingCredentials() {
        users.emailExists = true;

        assertThrows(DomainException.class, () -> service.registerUser(
                new com.muvix.application.port.in.user.RegisterUserUseCase.RegisterUserCommand(
                        "Buyer", "buyer@example.test", "555", "a-long-password")));

        assertEquals(null, users.savedUser);
        assertEquals(null, authentication.savedPasswordHash);
    }

    private static final class FakeUsers implements UserRepositoryPort {
        private boolean emailExists;
        private User savedUser;

        @Override public User save(User user) {
            savedUser = new User(9L, user.getName(), user.getEmail(), user.getPhone());
            return savedUser;
        }
        @Override public Optional<User> findById(Long id) { return Optional.empty(); }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public boolean existsByEmail(String email) { return emailExists; }
        @Override public void updateMobbexConnectDetails(Long id, String connectId, boolean linked) {}
    }

    private static final class FakeAuthentication implements UserAuthenticationPort {
        private String savedPasswordHash;
        private String savedTotpSecret;
        @Override public Optional<AuthenticationAccount> findByEmail(String email) { return Optional.empty(); }
        @Override public Optional<AuthenticationAccount> findById(Long userId) { return Optional.empty(); }
        @Override public void storeCredentials(Long id, String hash, String secret) {
            savedPasswordHash = hash;
            savedTotpSecret = secret;
        }
        @Override public void enableTwoFactor(Long id, Set<String> hashes) {}
        @Override public void disableTwoFactor(Long id) {}
        @Override public boolean consumeRecoveryCode(Long id, String hash) { return false; }
        @Override public void recordSuccessfulLogin(Long id, Instant at) {}
        @Override public void updateAccountStatus(Long id, AccountStatus status) {}
        @Override public void eraseSensitiveData(Long id, String email) {}
    }

    private static final class FakePasswordEncoder implements PasswordEncoder {
        @Override public String encode(CharSequence rawPassword) { return "bcrypt:" + rawPassword; }
        @Override public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encodedPassword != null && encodedPassword.equals(encode(rawPassword));
        }
    }

    private static final class FakeTwoFactor implements TwoFactorProviderPort {
        @Override public String generateSecret() { return "totp-secret"; }
        @Override public String createProvisioningUri(String email, String secret) {
            return "otpauth://totp/Muvix:" + email;
        }
        @Override public boolean verifyCode(String secret, String code) { return false; }
        @Override public List<String> generateRecoveryCodes() { return List.of(); }
    }
}
