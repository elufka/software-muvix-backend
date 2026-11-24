package com.muvix.application.usecase.user;

import com.muvix.application.port.in.user.ManageUserProfileUseCase;
import com.muvix.application.port.out.persistence.UserRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManageUserProfileService implements ManageUserProfileUseCase {

    private final UserRepositoryPort userRepository;

    public ManageUserProfileService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found"));
    }

    @Override
    @Transactional
    public User replaceProfile(UpdateProfileCommand command) {
        if (command == null) throw new DomainException("Profile data is required");
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new DomainException("User not found"));
        if (!user.getEmail().equals(command.email()) && userRepository.existsByEmail(command.email())) {
            throw new DomainException("An account with this email already exists");
        }
        user.updateProfile(
                command.firstName(),
                command.lastName(),
                command.email(),
                command.phone(),
                command.country(),
                command.province(),
                command.city(),
                command.address()
        );
        return userRepository.save(user);
    }
}
