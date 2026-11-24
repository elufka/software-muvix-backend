package com.muvix.application.port.in.event;

import com.muvix.domain.model.management.Box;


import java.util.List;

public interface BoxQueryUseCase {

    List<Box> findPurchaseBoxesByUserId(Long userId);
}
