package com.muvix.application.usecase.user;

import com.muvix.application.port.in.user.ManageUserAccountLifecycleUseCase;
import com.muvix.application.port.out.persistence.UserAccountLifecyclePort;
import com.muvix.application.port.out.security.UserAuthenticationPort;
import com.muvix.application.port.out.security.UserAuthenticationPort.AccountStatus;
import com.muvix.domain.model.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManageUserAccountLifecycleService implements ManageUserAccountLifecycleUseCase {

    private final UserAuthenticationPort authenticationPort;
    private final UserAccountLifecyclePort lifecyclePort;

    public ManageUserAccountLifecycleService(UserAuthenticationPort authenticationPort,
                                             UserAccountLifecyclePort lifecyclePort) {
        this.authenticationPort = authenticationPort;
        this.lifecyclePort = lifecyclePort;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountLifecycleStatus getErasureStatus(Long userId) {
        return state(userId);
    }

    @Override
    @Transactional
    public AccountLifecycleStatus requestDataErasure(Long userId) {
        AccountStatus current = accountStatus(userId);
        if (current == AccountStatus.ERASED || current == AccountStatus.SOFT_DELETED
                || current == AccountStatus.DEACTIVATED) {
            throw new DomainException("This account cannot request data erasure");
        }
        if (lifecyclePort.hasIncompleteTransactions(userId)) {
            authenticationPort.updateAccountStatus(userId, AccountStatus.ERASURE_PENDING_TRANSACTIONS);
        } else {
            authenticationPort.updateAccountStatus(userId, AccountStatus.ERASURE_CONFIRMATION_REQUIRED);
        }
        return state(userId);
    }

    @Override
    @Transactional
    public AccountLifecycleStatus confirmDataErasure(Long userId) {
        AccountStatus current = accountStatus(userId);
        if (current != AccountStatus.ERASURE_CONFIRMATION_REQUIRED) {
            throw new DomainException("Data erasure confirmation is not currently available");
        }
        if (lifecyclePort.hasIncompleteTransactions(userId)) {
            authenticationPort.updateAccountStatus(userId, AccountStatus.ERASURE_PENDING_TRANSACTIONS);
            return state(userId);
        }
        authenticationPort.eraseSensitiveData(userId, "erased-" + userId + "@deleted.invalid");
        return state(userId);
    }

    private AccountLifecycleStatus state(Long userId) {
        AccountStatus status = accountStatus(userId);
        return new AccountLifecycleStatus(status, status == AccountStatus.ERASURE_CONFIRMATION_REQUIRED);
    }

    private AccountStatus accountStatus(Long userId) {
        return authenticationPort.findById(userId)
                .map(UserAuthenticationPort.AuthenticationAccount::status)
                .orElseThrow(() -> new DomainException("User not found"));
    }
}
