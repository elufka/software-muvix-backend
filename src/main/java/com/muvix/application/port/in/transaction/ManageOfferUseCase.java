package com.muvix.application.port.in.transaction;
import com.muvix.domain.model.negotiation.Offer;
import java.math.BigDecimal;
public interface ManageOfferUseCase {
    record CreateOfferCommand(Long buyerId, Long itemId, BigDecimal amount) {}
    Offer create(CreateOfferCommand command);
    Offer reject(Long offerId, Long sellerId);
}
