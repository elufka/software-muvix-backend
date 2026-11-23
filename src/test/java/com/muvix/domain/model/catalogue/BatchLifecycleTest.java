package com.muvix.domain.model.catalogue;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class BatchLifecycleTest {
    @Test void removingMemberBelowMinimumSoftDeletesBatchAndKeepsRemainingProductIndependent() {
        Batch batch = new Batch(10L, 3L, "set", "", BigDecimal.TEN, SellableItem.State.AVAILABLE,
                "Other", Set.of(1L, 2L));
        batch.removeProduct(1L);
        assertEquals(SellableItem.State.SOFT_DELETED, batch.getState());
        assertEquals(Set.of(2L), batch.getProductIds());
    }
}
