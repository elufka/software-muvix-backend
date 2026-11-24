package com.muvix.application.usecase.user;

import com.muvix.application.port.in.user.GetCurrentUserUseCase;
import com.muvix.application.port.out.persistence.UserRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.user.User;
import org.springframework.stereotype.Service;

@Service
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetCurrentUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User getById(Long userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new DomainException("User not found with id: " + userId));
    }
}
