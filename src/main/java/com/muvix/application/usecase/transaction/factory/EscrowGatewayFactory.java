package com.muvix.application.usecase.transaction.factory;

import com.muvix.application.port.out.payment.EscrowGatewayPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.transaction.EscrowProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EscrowGatewayFactory {

    private final Map<EscrowProvider, EscrowGatewayPort> providers;

    /**
     * Spring inyecta automáticamente todas las implementaciones de EscrowGatewayPort
     */
    public EscrowGatewayFactory(List<EscrowGatewayPort> gatewayPorts) {
        this.providers = gatewayPorts.stream()
                .collect(Collectors.toMap(EscrowGatewayPort::getProvider, Function.identity()));
    }

    public EscrowGatewayPort getGateway(EscrowProvider provider) {
        EscrowGatewayPort gateway = providers.get(provider);
        if (gateway == null) {
            throw new DomainException("El proveedor de pago '" + provider + "' no está soportado o configurado actualmente.");
        }
        return gateway;
    }
}
