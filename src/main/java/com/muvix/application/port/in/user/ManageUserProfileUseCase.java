package com.muvix.application.port.in.user;

import com.muvix.domain.model.user.User;

public interface ManageUserProfileUseCase {

    record UpdateProfileCommand(Long userId, String name, String email, String phone) {}

    User getProfile(Long userId);

    User replaceProfile(UpdateProfileCommand command);
}
