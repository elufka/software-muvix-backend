package com.muvix.domain.model.transaction;

import com.muvix.domain.model.transaction.Dispute;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DisputeEvidenceDeadlineTest {
    @Test void acceptsOneEvidenceRoundWithinTwoDaysOnly() {
        Instant now = Instant.parse("2026-11-17T12:00:00Z");
        Dispute dispute = new Dispute(null, 4L, 8L, "damaged", List.of(), now.minusSeconds(60), now);
        dispute.requestAdditionalEvidence(now);
        dispute.submitAdditionalEvidence(List.of("proof"), now.plusSeconds(172800));
        assertEquals(Dispute.Status.UNDER_REVIEW, dispute.getStatus());
        assertEquals(1, dispute.getAdditionalEvidenceRounds());
        assertThrows(RuntimeException.class, () -> dispute.requestAdditionalEvidence(now.plusSeconds(172801)));
    }
}
