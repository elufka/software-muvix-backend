package com.muvix.application.usecase.event;

import com.muvix.application.port.in.event.BoxQueryUseCase;
import com.muvix.application.port.out.persistence.BoxRepositoryPort;
import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.domain.model.management.Box;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoxQueryService implements BoxQueryUseCase {
    private final BoxRepositoryPort repositoryPort;

    @Override
    public List<Box> findPurchaseBoxesByUserId(Long userId) {
        return repositoryPort.findPurchaseBoxesByUserId(userId);
    }
}
