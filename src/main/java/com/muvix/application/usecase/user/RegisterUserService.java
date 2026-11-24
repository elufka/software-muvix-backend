package com.muvix.application.usecase.user;

import com.muvix.application.port.in.user.RegisterUserUseCase;
import com.muvix.application.port.out.persistence.UserRepositoryPort;
import com.muvix.application.port.out.security.UserAuthenticationPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final UserAuthenticationPort authenticationPort;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(UserRepositoryPort userRepository,
                               UserAuthenticationPort authenticationPort,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationPort = authenticationPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(RegisterUserCommand command) {
        if (command == null) throw new DomainException("Registration data is required");
        if (command.password() == null || command.password().length() < 8) {
            throw new DomainException("Password must contain at least 8 characters");
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new DomainException("An account with this email already exists");
        }

        User user = userRepository.save(new User(
                null,
                command.photo(),
                command.firstName(),
                command.lastName(),
                command.email(),
                command.phone(),
                command.country(),
                command.province(),
                command.city(),
                command.address(),
                User.Role.USER
        ));
        authenticationPort.storeCredentials(
                user.getId(),
                passwordEncoder.encode(command.password()),
                null
        );

        return user;
    }
}
