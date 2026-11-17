package com.muvix.domain.model.logistic;

import com.muvix.domain.model.logistic.PickupSlot;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class PickupSlotTest {
    @Test void requiresBothPartiesAndResetsBothConfirmationsAfterCounterProposal() {
        Instant start = Instant.parse("2026-11-18T10:00:00Z");
        PickupSlot slot = PickupSlot.requestSlot(1L, start, start.plusSeconds(3600));
        slot.confirm(2L, 2L, 3L);
        assertFalse(slot.isConfirmed());
        assertTrue(slot.isBuyerConfirmed());
        slot.counterPropose(start.plusSeconds(7200), start.plusSeconds(10800), 3L, 2L);
        assertFalse(slot.isBuyerConfirmed());
        assertFalse(slot.isSellerConfirmed());
        slot.confirm(2L, 2L, 3L);
        assertFalse(slot.isConfirmed());
        slot.confirm(3L, 2L, 3L);
        assertTrue(slot.isConfirmed());
    }
}
