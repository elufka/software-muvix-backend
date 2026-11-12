package com.muvix.application.port.in.user;

import com.muvix.domain.model.user.User;

public interface RegisterUserUseCase {

    record RegisterUserCommand(String name, String email, String phone, String password) {}

    record RegistrationResult(User user, String qrCodeTwoFactorUrl) {}

    RegistrationResult registerUser(RegisterUserCommand command);
}
