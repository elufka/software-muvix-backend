package com.muvix.infrastructure.adapters.in.rest.controller.Box.dtos;


import com.muvix.domain.model.logistic.Location;
import com.muvix.domain.model.management.DiscountRule;
import com.muvix.domain.model.management.RelocationEvent;
import com.muvix.domain.model.management.RelocationEvent.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public sealed interface BoxDtos {

    record ReplaceBoxRequest(
            @NotBlank String title,
            @NotNull Location originLocation,
            @NotNull Location destinationLocation,
            Instant saleDeadline,
            Instant arrivalDeliveryDate,
            @Positive BigDecimal takeItAllPrice,
            @Valid List<DiscountRule> discountRules
    ) implements BoxDtos {}

    record InviteMemberRequest(
            @NotNull Long targetUserId
    ) implements BoxDtos {}

    record BoxResponse(
            Long id,
            Long ownerId,
            String title,
            String uniqueShareToken,
            Status status
    ) implements BoxDtos {}
}

