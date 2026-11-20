package com.muvix.application.port.in.event;

import com.muvix.domain.model.management.Box;

public interface ManageBoxUseCase {

    record AddItemToBoxCommand(
            Long userId,
            Long boxId,
            Long itemId
    ) {}

    void addItemToBox(AddItemToBoxCommand command);
}
