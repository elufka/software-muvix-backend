package com.muvix.application.usecase.event;

import com.muvix.application.port.in.event.BoxQueryUseCase;
import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.domain.model.management.RelocationEvent;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.RelocationEventJpaEntity.EventTypeJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoxQueryService implements BoxQueryUseCase {
    private final RelocationEventRepositoryPort repositoryPort;

    @Override
    public List<RelocationEvent> findPurchaseBoxesByUserId(Long userId) {
        return repositoryPort.findPurchaseBoxesByUserId(userId);
    }
}
