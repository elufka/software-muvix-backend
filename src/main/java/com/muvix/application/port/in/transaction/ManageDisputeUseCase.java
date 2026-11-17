package com.muvix.application.port.in.transaction;

import com.muvix.domain.model.transaction.Dispute;
import java.util.List;

public interface ManageDisputeUseCase {
    enum ModeratorDecision { REQUEST_MORE_EVIDENCE, REFUND_BUYER, RELEASE_TO_SELLER }
    record SubmitEvidenceCommand(Long disputeId, Long buyerId, List<String> evidenceImages) {}
    Dispute submitAdditionalEvidence(SubmitEvidenceCommand command);
    Dispute getVisible(Long disputeId, Long actorId, boolean moderator);
    Dispute applyModeratorDecision(Long disputeId, ModeratorDecision decision);
    void expireEvidenceDeadlines();
}
