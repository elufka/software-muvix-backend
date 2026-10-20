package com.muvix.infraestructure.entrypoints.rest.dto;

import com.muvix.domain.model.Item;
import com.muvix.domain.model.PublicationStatus;

import java.math.BigDecimal;
import java.util.List;

public class CreatePublicationRequest {
    String title;
    String description;
    private List<Item> items;
    private Integer quantity;
    private BigDecimal price;
}
