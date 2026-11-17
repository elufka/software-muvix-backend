package com.muvix.application.usecase.transaction;

import com.muvix.application.port.in.transaction.ManageDisputeUseCase;
import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.DisputeRepositoryPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.Dispute;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Duration;

@Service
public class ManageDisputeService implements ManageDisputeUseCase {
    private final DisputeRepositoryPort disputes;
    private final EscrowAccountRepositoryPort escrows;
    private final OrderRepositoryPort orders;
    private final MobbexGatewayPort mobbex;
    private final Clock clock;
    public ManageDisputeService(DisputeRepositoryPort disputes, EscrowAccountRepositoryPort escrows,
                                OrderRepositoryPort orders, MobbexGatewayPort mobbex, Clock clock) {
        this.disputes = disputes; this.escrows = escrows; this.orders = orders; this.mobbex = mobbex; this.clock = clock;
    }

    @Override @Transactional
    public Dispute submitAdditionalEvidence(SubmitEvidenceCommand command) {
        Dispute dispute = find(command.disputeId());
        if (!dispute.getBuyerId().equals(command.buyerId())) throw new DomainException("Only the buyer may submit evidence");
        dispute.submitAdditionalEvidence(command.evidenceImages(), clock.instant());
        return disputes.save(dispute);
    }

    @Override @Transactional
    public Dispute applyModeratorDecision(Long disputeId, ModeratorDecision decision) {
        Dispute dispute = find(disputeId);
        if (decision == ModeratorDecision.REQUEST_MORE_EVIDENCE) {
            dispute.requestAdditionalEvidence(clock.instant());
            return disputes.save(dispute);
        }
        settle(dispute, decision == ModeratorDecision.REFUND_BUYER);
        return disputes.save(dispute);
    }

    @Override @Scheduled(fixedDelayString = "PT1H") @Transactional
    public void expireEvidenceDeadlines() {
        var cutoff = clock.instant().minus(Duration.ofDays(2));
        disputes.findByStatus(Dispute.Status.NEEDS_EVIDENCE).stream()
                .filter(dispute -> dispute.getEvidenceRequestedAt() != null && !dispute.getEvidenceRequestedAt().plus(Duration.ofDays(2)).isAfter(clock.instant()))
                .forEach(dispute -> {
                    settle(dispute, false);
                    disputes.save(dispute);
                });
    }

    private void settle(Dispute dispute, boolean refund) {
        dispute.resolve(refund);
        EscrowAccount escrow = escrows.findByOrderId(dispute.getOrderId()).orElseThrow(() -> new DomainException("Escrow not found"));
        if (refund) {
            if (escrow.getExternalOperationId() == null) throw new DomainException("Payment operation is not linked to escrow");
            mobbex.refundPayment(escrow.getExternalOperationId());
            escrow.refundToBuyer();
        } else {
            Order order = orders.findById(dispute.getOrderId()).orElseThrow(() -> new DomainException("Order not found"));
            if (escrow.getExternalOperationId() == null) throw new DomainException("Payment operation is not linked to escrow");
            mobbex.releaseFunds(escrow.getExternalOperationId());
            escrow.releaseToSeller(order.getDeliveredAt(), clock.instant());
        }
        escrows.save(escrow);
    }

    private Dispute find(Long id) { return disputes.findById(id).orElseThrow(() -> new DomainException("Dispute not found")); }
}
