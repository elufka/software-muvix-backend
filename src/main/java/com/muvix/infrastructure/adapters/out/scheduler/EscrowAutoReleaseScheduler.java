package com.muvix.infrastructure.adapters.out.scheduler;

import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.domain.model.transaction.EscrowAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class EscrowAutoReleaseScheduler {

    private static final Logger log = LoggerFactory.getLogger(EscrowAutoReleaseScheduler.class);

    private final EscrowAccountRepositoryPort escrowAccountRepository;
    private final MobbexGatewayPort mobbexGateway;

    public EscrowAutoReleaseScheduler(EscrowAccountRepositoryPort escrowAccountRepository,
                                      MobbexGatewayPort mobbexGateway) {
        this.escrowAccountRepository = escrowAccountRepository;
        this.mobbexGateway = mobbexGateway;
    }

    /**
     * Se ejecuta cada hora (cron = "0 0 * * * *") para procesar fondos retenidos.
     * Busca cuentas Escrow en HELD cuya fecha de entrega supere las 24 horas.
     */
    @Scheduled(cron = "${mobbex.escrow.auto-release-cron:0 0 * * * *}")
    @Transactional
    public void processAutoRelease() {
        log.info("Iniciando tarea programada de liberación automática de fondos Escrow...");

        Instant threshold = Instant.now().minus(24, ChronoUnit.HOURS);
        List<EscrowAccount> pendingAccounts = escrowAccountRepository.findPendingReleaseAccounts(threshold);

        if (pendingAccounts.isEmpty()) {
            log.info("No se encontraron cuentas Escrow pendientes de liberación automática.");
            return;
        }

        log.info("Se encontraron {} cuentas pendientes de liberación.", pendingAccounts.size());

        for (EscrowAccount escrow : pendingAccounts) {
            try {
                // 1. Invocación a Mobbex para capturar/liberar el pago
                mobbexGateway.releaseFunds(String.valueOf(escrow.getOrderId()));

                // 2. Transicionar estado en el modelo de dominio a ACCREDITED_TO_SELLER
                escrow.releaseToSeller(threshold, Instant.now());
                escrowAccountRepository.save(escrow);

                log.info("Fondos liberados automáticamente para la orden ID: {}", escrow.getOrderId());
            } catch (Exception e) {
                log.error("Error al liberar automáticamente los fondos para la orden ID: {}. Causa: {}",
                        escrow.getOrderId(), e.getMessage(), e);
            }
        }
    }
}