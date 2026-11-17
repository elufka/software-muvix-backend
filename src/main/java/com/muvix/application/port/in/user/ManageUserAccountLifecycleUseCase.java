package com.muvix.application.port.in.user;

import com.muvix.application.port.out.security.UserAuthenticationPort.AccountStatus;

public interface ManageUserAccountLifecycleUseCase {

    record AccountLifecycleStatus(AccountStatus status, boolean confirmationRequired) {}

    AccountLifecycleStatus getErasureStatus(Long userId);

    AccountLifecycleStatus requestDataErasure(Long userId);

    AccountLifecycleStatus confirmDataErasure(Long userId);
}
