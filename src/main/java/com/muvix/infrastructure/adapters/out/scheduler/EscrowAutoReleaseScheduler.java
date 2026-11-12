package com.muvix.infrastructure.adapters.out.scheduler;

import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.domain.model.transaction.EscrowAccount;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(
        name = "muvix.escrow.auto-release.enabled",
        havingValue = "true"
)
@Component
public class EscrowAutoReleaseScheduler {

    private final EscrowAccountRepositoryPort escrowAccountRepositoryPort;
    private final MobbexGatewayPort mobbexGatewayPort;



    public EscrowAutoReleaseScheduler(
            EscrowAccountRepositoryPort escrowAccountRepositoryPort,
            MobbexGatewayPort mobbexGatewayPort) {
        this.escrowAccountRepositoryPort = escrowAccountRepositoryPort;
        this.mobbexGatewayPort = mobbexGatewayPort;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void processAutoRelease() {
        Instant threshold = Instant.now().minus(24, ChronoUnit.HOURS);
        List<EscrowAccount> eligibleAccounts = escrowAccountRepositoryPort.findPendingReleaseAccounts(threshold);

        for (EscrowAccount escrow : eligibleAccounts) {
            try {
                if (escrow.getExternalOperationId() != null && !escrow.getExternalOperationId().isBlank()) {
                    mobbexGatewayPort.releaseFunds(escrow.getExternalOperationId());
                    escrow.markAsAccreditedToSeller();
                    escrowAccountRepositoryPort.save(escrow);
                }
            } catch (Exception e) {
                // Log de auditoría de error transaccional
            }
        }
    }
}
