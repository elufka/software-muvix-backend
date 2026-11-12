
package com.muvix.infrastructure.adapters.out.payment.mobbex;

import com.muvix.application.port.out.payment.EscrowGatewayPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.EscrowProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class MobbexHttpAdapter implements EscrowGatewayPort {

    private final RestClient restClient;

    public MobbexHttpAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${mobbex.url}") String baseUrl,
            @Value("${mobbex.api-key}") String apiKey,
            @Value("${mobbex.access-token}") String accessToken
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("x-access-token", accessToken)
                .build();
    }

    @Override
    public EscrowProvider getProvider() {
        return EscrowProvider.MOBBEX;
    }

    @Override
    public CheckoutResponse createCheckout(CheckoutRequest request) {

        Map<String, Object> body = Map.of(
                "total", request.amount(),
                "currency", "ARS",
                "reference", request.orderId(),
                "description", request.description(),
                "return_url", request.returnUrl()
        );

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/checkout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !(response.get("data") instanceof Map<?, ?> data)) {
                throw new DomainException("Respuesta inválida de Mobbex");
            }

            Object url = data.get("url");
            Object id = data.get("id");

            if (!(url instanceof String checkoutUrl)
                    || !(id instanceof String paymentId)) {
                throw new DomainException(
                        "Mobbex no devolvió los datos del checkout"
                );
            }

            return new CheckoutResponse(checkoutUrl, paymentId);

        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException(
                    "Error al comunicarse con Mobbex"
            );
        }
    }

    @Override
    public void releaseFunds(String externalOperationId) {
        restClient.post()
                .uri("/operations/{id}/capture", externalOperationId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void refundPayment(String externalOperationId) {
        restClient.post()
                .uri("/operations/{id}/refund", externalOperationId)
                .retrieve()
                .toBodilessEntity();
    }
}
