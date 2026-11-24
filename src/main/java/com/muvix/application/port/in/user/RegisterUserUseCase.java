package com.muvix.application.port.in.user;

import com.muvix.domain.model.user.User;

public interface RegisterUserUseCase {
    record RegisterUserCommand(String photo,
                               String firstName,
                               String lastName,
                               String email,
                               String phone,
                               String country,
                               String province,
                               String city,
                               String address,
                               String password) {}
    User registerUser(RegisterUserCommand command);
}
