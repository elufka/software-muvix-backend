package com.muvix.infrastructure.adapters.out.payment.mobbex;

import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.domain.model.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class MobbexHttpAdapter implements MobbexGatewayPort {

    private final RestClient restClient;

    public MobbexHttpAdapter(
            @Value("${mobbex.api-key:zJ8LFTBX6Ba8D611e9io13fDZAwj0QmKO1Hn1yIj}") String apiKey,
            @Value("${mobbex.access-token:d31f0721-2f85-44e7-bcc6-15e19d1a53cc}") String accessToken,
            @Value("${mobbex.test-mode:true}") boolean testMode) {

        this.restClient = RestClient.builder()
                .baseUrl("[suspicious link removed]")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("x-access-token", accessToken)
                .defaultHeader("x-test", String.valueOf(testMode))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
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
                    .uri("checkout")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Map<?, ?> data = (Map<?, ?>) response.get("data");
            String checkoutUrl = (String) data.get("url");
            String paymentId = (String) data.get("id");

            return new CheckoutResponse(checkoutUrl, paymentId);
        } catch (Exception e) {
            throw new DomainException("Error al comunicarse con la pasarela Mobbex: " + e.getMessage());
        }
    }

    @Override
    public void releaseFunds(String mobbexOperationId) {
        try {
            restClient.post()
                    .uri("operations/{id}/capture", mobbexOperationId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new DomainException("No se pudieron liberar los fondos retenidos en Mobbex: " + e.getMessage());
        }
    }

    @Override
    public void refundPayment(String mobbexOperationId) {
        try {
            restClient.post()
                    .uri("operations/{id}/refund", mobbexOperationId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new DomainException("Error al procesar el reembolso en Mobbex: " + e.getMessage());
        }
    }
}