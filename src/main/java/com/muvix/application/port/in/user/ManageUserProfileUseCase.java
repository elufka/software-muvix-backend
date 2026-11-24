package com.muvix.application.port.in.user;

import com.muvix.domain.model.user.User;

public interface ManageUserProfileUseCase {

    record UpdateProfileCommand(Long userId,
                                String firstName,
                                String lastName,
                                String email,
                                String phone,
                                String country,
                                String province,
                                String city,
                                String address,
                                String password) {}

    User getProfile(Long userId);

    User replaceProfile(UpdateProfileCommand command);
}
