package com.muvix.application.port.in.transaction;
import com.muvix.domain.model.transaction.EscrowAccount;
public interface GetEscrowUseCase { EscrowAccount getForOrder(Long orderId, Long actorId, boolean moderator); }
