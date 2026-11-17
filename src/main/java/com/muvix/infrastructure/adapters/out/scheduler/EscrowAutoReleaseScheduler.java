package com.muvix.infrastructure.adapters.out.scheduler;

import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.domain.model.transaction.EscrowAccount;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
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
    private final OrderRepositoryPort orderRepositoryPort;



    public EscrowAutoReleaseScheduler(
            EscrowAccountRepositoryPort escrowAccountRepositoryPort,
            MobbexGatewayPort mobbexGatewayPort, OrderRepositoryPort orderRepositoryPort) {
        this.escrowAccountRepositoryPort = escrowAccountRepositoryPort;
        this.mobbexGatewayPort = mobbexGatewayPort;
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void processAutoRelease() {
        Instant threshold = Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS);
        for (var order : orderRepositoryPort.findDeliveredBefore(threshold)) {
            try {
                EscrowAccount escrow = escrowAccountRepositoryPort.findByOrderId(order.getId()).orElse(null);
                if (escrow == null || escrow.getStatus() != EscrowAccount.EscrowStatus.HELD) continue;
                if (escrow.getExternalOperationId() != null && !escrow.getExternalOperationId().isBlank()) {
                    mobbexGatewayPort.releaseFunds(escrow.getExternalOperationId());
                    escrow.releaseToSeller(order.getDeliveredAt(), Instant.now());
                    escrowAccountRepositoryPort.save(escrow);
                }
            } catch (Exception e) {
                // Log de auditoría de error transaccional
            }
        }
    }
}
