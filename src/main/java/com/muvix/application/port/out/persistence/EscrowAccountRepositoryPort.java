package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.domain.model.transaction.EscrowAccount.EscrowStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Output Port (Puerto de Salida) para la gestión y persistencia de cuentas Escrow (Custodia de fondos).
 * Permite desacoplar la lógica de dominio y casos de uso de la infraestructura (MySQL / JPA).
 */
public interface EscrowAccountRepositoryPort {

    /**
     * Guarda o actualiza una cuenta de custodia temporal.
     */
    EscrowAccount save(EscrowAccount escrowAccount);

    /**
     * Busca la cuenta de custodia asociada a un pedido por su ID de pedido.
     */
    Optional<EscrowAccount> findByOrderId(Long orderId);

    /**
     * Obtiene todas las cuentas en estado HELD (Retenido) que superaron el plazo de 24 horas
     * desde la entrega sin disputas abiertas, listas para acreditación automática.
     */
    List<EscrowAccount> findPendingReleaseAccounts(Instant deliveredBefore);

    /**
     * Busca cuentas Escrow filtrando por su estado actual (HELD, ACCREDITED_TO_SELLER, DISPUTED, REFUNDED).
     */
    List<EscrowAccount> findByStatus(EscrowStatus status);
}
