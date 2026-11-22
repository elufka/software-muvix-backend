package com.muvix.application.port.in.event;

import com.muvix.domain.model.management.RelocationEvent;

import java.util.List;

public interface BoxQueryUseCase {

    List<RelocationEvent> findPurchaseBoxesByUserId(Long userId);
}
