package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.application.port.out.persistence.UserAccountLifecyclePort;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.EscrowAccountJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.OrderJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.UserJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.EscrowAccountSpringDataJpaRepository;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.OrderSpringDataJpaRepository;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.SellableItemJpaRepository;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.UserMysqlRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.StateJpa;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class UserAccountLifecycleMySQLAdapter implements UserAccountLifecyclePort {

    private static final List<OrderJpaEntity.StatusJpa> INCOMPLETE_ORDER_STATUSES =
            List.of(OrderJpaEntity.StatusJpa.PENDING_SLOT, OrderJpaEntity.StatusJpa.SLOT_CONFIRMED);

    private final UserMysqlRepository userRepository;
    private final OrderSpringDataJpaRepository orderRepository;
    private final EscrowAccountSpringDataJpaRepository escrowRepository;
    private final SellableItemJpaRepository itemRepository;
    private final RelocationEventRepositoryPort eventRepository;

    public UserAccountLifecycleMySQLAdapter(UserMysqlRepository userRepository,
                                           OrderSpringDataJpaRepository orderRepository,
                                           EscrowAccountSpringDataJpaRepository escrowRepository,
                                           SellableItemJpaRepository itemRepository,
                                           RelocationEventRepositoryPort eventRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.escrowRepository = escrowRepository;
        this.itemRepository = itemRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public List<Long> findActiveAccountIdsInactiveBefore(Instant cutoff) {
        return userRepository.findActiveIdsInactiveBefore(UserJpaEntity.AccountStatusJpa.ACTIVE, cutoff);
    }

    @Override
    public List<Long> findAccountsPendingErasureConfirmation() {
        return userRepository.findByAccountStatus(UserJpaEntity.AccountStatusJpa.ERASURE_PENDING_TRANSACTIONS)
                .stream().map(UserJpaEntity::getId).toList();
    }

    @Override
    public boolean hasIncompleteTransactions(Long userId) {
        List<OrderJpaEntity> orders = findOrdersFor(userId);
        if (orders.stream().anyMatch(order -> INCOMPLETE_ORDER_STATUSES.contains(order.getStatus()))) return true;
        List<Long> orderIds = orders.stream().map(OrderJpaEntity::getId).toList();
        return !orderIds.isEmpty() && escrowRepository.existsByOrderIdInAndStatus(
                orderIds, EscrowAccountJpaEntity.EscrowStatusJpa.HELD);
    }

    @Override
    public boolean hasAnyTransactions(Long userId) {
        return !findOrdersFor(userId).isEmpty();
    }

    @Override
    public boolean hasActiveBusinessData(Long userId) {
        return !eventRepository.findByUserId(userId).isEmpty()
                || itemRepository.existsBySellerIdAndState(userId, StateJpa.AVAILABLE);
    }

    private List<OrderJpaEntity> findOrdersFor(Long userId) {
        List<OrderJpaEntity> orders = new ArrayList<>(orderRepository.findByBuyerId(userId));
        List<Long> ownedEventIds = eventRepository.findByUserId(userId).stream()
                .map(event -> event.getId())
                .toList();
        if (!ownedEventIds.isEmpty()) orders.addAll(orderRepository.findByEventIdIn(ownedEventIds));
        return orders.stream().distinct().toList();
    }
}
