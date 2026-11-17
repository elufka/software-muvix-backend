
package com.muvix.infrastructure.adapters.out.payment.mobbex;

import com.muvix.application.port.out.payment.EscrowGatewayPort;
import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.EscrowProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class MobbexHttpAdapter implements EscrowGatewayPort, MobbexGatewayPort {

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
    public EscrowGatewayPort.CheckoutResponse createCheckout(EscrowGatewayPort.CheckoutRequest request) {

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

            return new EscrowGatewayPort.CheckoutResponse(checkoutUrl, paymentId);

        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException(
                    "Error al comunicarse con Mobbex"
            );
        }
    }

    @Override
    public MobbexGatewayPort.CheckoutResponse createCheckout(MobbexGatewayPort.CheckoutRequest request) {
        EscrowGatewayPort.CheckoutResponse response = createCheckout(
                new EscrowGatewayPort.CheckoutRequest(
                        request.orderId(), request.amount(), request.description(), request.returnUrl()));
        return new MobbexGatewayPort.CheckoutResponse(response.checkoutUrl(), response.externalPaymentId());
    }

    @Override
    public boolean isApprovedOperation(String reference, String operationId, BigDecimal expectedAmount) {
        if (reference == null || operationId == null || expectedAmount == null) return false;
        try {
            Map<?, ?> response = restClient.get().uri("/operations/{reference}", reference)
                    .retrieve().body(Map.class);
            if (response == null || !(response.get("data") instanceof Map<?, ?> data)) return false;
            List<Object> ids = valuesForKey(data, "id");
            List<Object> references = valuesForKey(data, "reference");
            List<Object> amounts = valuesForKey(data, "total");
            List<Object> statusCodes = valuesForKey(data, "code");
            return ids.stream().anyMatch(value -> operationId.equals(String.valueOf(value)))
                    && references.stream().anyMatch(value -> reference.equals(String.valueOf(value)))
                    && amounts.stream().anyMatch(value -> numericEquals(value, expectedAmount))
                    && statusCodes.stream().anyMatch(value -> "200".equals(String.valueOf(value)));
        } catch (Exception exception) {
            return false;
        }
    }

    private static List<Object> valuesForKey(Object value, String key) {
        List<Object> matches = new ArrayList<>();
        if (value instanceof Map<?, ?> map) {
            map.forEach((nestedKey, nestedValue) -> {
                if (key.equalsIgnoreCase(String.valueOf(nestedKey))) matches.add(nestedValue);
                matches.addAll(valuesForKey(nestedValue, key));
            });
        } else if (value instanceof Iterable<?> iterable) {
            iterable.forEach(item -> matches.addAll(valuesForKey(item, key)));
        }
        return matches;
    }

    private static boolean numericEquals(Object value, BigDecimal expected) {
        try { return new BigDecimal(String.valueOf(value)).compareTo(expected) == 0; }
        catch (NumberFormatException exception) { return false; }
    }

    @Override
    public String generateConnectUrl(String sellerReference, String returnUrl) {
        String callback = UriComponentsBuilder.fromUriString(returnUrl)
                .queryParam("reference", sellerReference)
                .build()
                .toUriString();
        try {
            Map<?, ?> response = restClient.post()
                    .uri("/developer/connect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("return_url", callback))
                    .retrieve()
                    .body(Map.class);
            if (response == null || !(response.get("data") instanceof Map<?, ?> data)
                    || !(data.get("url") instanceof String url) || url.isBlank()) {
                throw new DomainException("Respuesta inválida de Mobbex Connect");
            }
            return url;
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("Error al iniciar la conexión con Mobbex");
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
