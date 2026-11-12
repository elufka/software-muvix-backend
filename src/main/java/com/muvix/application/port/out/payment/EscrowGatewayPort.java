package com.muvix.application.port.out.payment;

import com.muvix.domain.model.transaction.EscrowProvider;
import java.math.BigDecimal;

public interface EscrowGatewayPort {

    record CheckoutRequest(String orderId, BigDecimal amount, String description, String returnUrl) {}
    record CheckoutResponse(String checkoutUrl, String externalPaymentId) {}

    EscrowProvider getProvider();

    CheckoutResponse createCheckout(CheckoutRequest request);

    void releaseFunds(String externalOperationId);

    void refundPayment(String externalOperationId);
}
