package com.muvix.application.port.in.transaction;

import com.muvix.domain.model.negotiation.Offer;
import java.math.BigDecimal;

public interface ManageOfferUseCase {
    record CreateOfferCommand(
            Long buyerId,
            Long itemId,
            BigDecimal amount
    ) {}
    Offer create(CreateOfferCommand command);
    Offer get(Long offerId, Long actorId);
    Offer reject(Long offerId, Long sellerId);
    Offer counter(Long offerId, Long sellerId, BigDecimal amount);
    Offer acceptInitial(Long offerId, Long sellerId);
    Offer acceptCounter(Long offerId, Long buyerId);
    Offer rejectCounter(Long offerId, Long buyerId);
}