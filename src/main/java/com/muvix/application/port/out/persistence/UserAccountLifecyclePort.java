package com.muvix.application.port.out.persistence;

import java.time.Instant;
import java.util.List;

public interface UserAccountLifecyclePort {

    List<Long> findActiveAccountIdsInactiveBefore(Instant cutoff);

    List<Long> findAccountsPendingErasureConfirmation();

    boolean hasIncompleteTransactions(Long userId);

    boolean hasAnyTransactions(Long userId);

    boolean hasActiveBusinessData(Long userId);
}
