package com.muvix.application.port.out.payment;

public interface VendorPaymentAccountPort {

    /** Genera la URL de autorización para que el vendedor vincule su CUIT/cuenta */
    String generateConnectUrl(String sellerId, String returnUrl);
}