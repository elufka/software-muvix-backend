package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.logistic.ChatCoordinatorUseCase;
import com.muvix.application.port.in.transaction.CancelOrderUseCase;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Duration;

@Service
public class ExpirePendingSlotOrdersService {
    private final OrderRepositoryPort orders;
    private final CancelOrderUseCase cancellation;
    private final ChatCoordinatorUseCase chat;
    private final Clock clock;
    public ExpirePendingSlotOrdersService(OrderRepositoryPort orders,CancelOrderUseCase cancellation,ChatCoordinatorUseCase chat,Clock clock){
        this.orders=orders;this.cancellation=cancellation;this.chat=chat;this.clock=clock;
    }
    @Scheduled(fixedDelayString="PT15M") @Transactional
    public void expireUnscheduledOrders(){
        var cutoff=clock.instant().minus(Duration.ofHours(48));
        for(var order:orders.findPendingSlotBefore(cutoff)){
            boolean cancelled = cancellation.cancelExpiredPendingSlot(order.getId(), cutoff);
            if (cancelled) {
                chat.recordSystemMessage(order.getId(), "Order was cancelled because pickup time was not confirmed within 48 hours. Reserved items are available again.");
            }
        }
    }
}