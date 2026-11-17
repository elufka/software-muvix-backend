package com.muvix.application.port.in.event;
import com.muvix.domain.model.logistic.Location;
import com.muvix.domain.model.management.DiscountRule;
import com.muvix.domain.model.management.RelocationEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public interface ManageRelocationEventUseCase {
    record ReplaceCommand(Long eventId,Long userId,String title,Location originLocation,Location destinationLocation,
                          Instant saleDeadline,Instant arrivalDeliveryDate,BigDecimal takeItAllPrice,List<DiscountRule> discountRules){}
    RelocationEvent get(Long eventId,Long userId);
    RelocationEvent replace(ReplaceCommand command);
    void cancel(Long eventId,Long userId);
}
