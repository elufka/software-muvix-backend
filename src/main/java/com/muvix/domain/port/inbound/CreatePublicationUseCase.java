package com.muvix.domain.port.inbound;

import com.muvix.domain.model.Item;
import com.muvix.domain.model.Publication;

import java.math.BigDecimal;
import java.util.List;

public interface CreatePublicationUseCase  {
    Publication execute(String title,
                        String description,
                        Integer quantity,
                        BigDecimal price,
                        List<Item> items);
}
