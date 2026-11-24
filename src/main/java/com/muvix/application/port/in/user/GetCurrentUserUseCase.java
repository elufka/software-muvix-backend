package com.muvix.application.port.in.user;

import com.muvix.domain.model.user.User;

public interface GetCurrentUserUseCase {
    User getById(Long userId);
}
