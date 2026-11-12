package com.muvix.application.port.out.payment;

import java.math.BigDecimal;

public interface MobbexGatewayPort {

    record CheckoutRequest(String orderId, BigDecimal amount, String description, String returnUrl) {}
    record CheckoutResponse(String checkoutUrl, String mobbexPaymentId) {}


    /** Genera el link de pago en Mobbex en modo pre-autorización / retención */
    CheckoutResponse createCheckout(CheckoutRequest request);

    /** Libera los fondos retenidos al vendedor (Endpoint de Capture / Release) */
    void releaseFunds(String mobbexOperationId);

    /** Reembolsa el 100% de los fondos retenidos al comprador */
    void refundPayment(String mobbexOperationId);

    String generateConnectUrl(String sellerReference, String returnUrl);
}