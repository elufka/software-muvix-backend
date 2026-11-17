package com.muvix.application.port.in.transaction;
import com.muvix.domain.model.transaction.Order;
public interface GetOrderUseCase { Order get(Long orderId, Long actorId); }
