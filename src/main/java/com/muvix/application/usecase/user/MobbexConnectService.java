package com.muvix.application.usecase.user;

import com.muvix.application.port.out.payment.MobbexGatewayPort;
import com.muvix.application.port.out.persistence.UserRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MobbexConnectService {

    private final MobbexGatewayPort mobbexGatewayPort;
    private final UserRepositoryPort userRepositoryPort;
    private final String connectReturnUrl;

    public MobbexConnectService(MobbexGatewayPort mobbexGatewayPort,
                                UserRepositoryPort userRepositoryPort,
                                @Value("${mobbex.connect-return-url:http://localhost:8080/api/v1/users/connect/callback}") String connectReturnUrl) {
        this.mobbexGatewayPort = mobbexGatewayPort;
        this.userRepositoryPort = userRepositoryPort;
        this.connectReturnUrl = connectReturnUrl;
    }

    /**
     * Genera la URL de autorización de Mobbex Connect para que el vendedor
     * autorice a Muvix a gestionar cobros en su nombre dentro del iframe/modal.
     */
    public String generateConnectUrl(Long sellerId) {
        // Validar que el usuario/vendedor existe en el sistema antes de llamar a la pasarela
        User user = userRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new DomainException("Vendedor no encontrado con ID: " + sellerId));

        return mobbexGatewayPort.generateConnectUrl(String.valueOf(user.getId()), connectReturnUrl);
    }

    /**
     * Procesa el resultado de la autorización tras la interacción del vendedor en el iframe.
     * Actualiza el CUIT/ID de Mobbex Connect y el estado de vinculación usando el UserRepositoryPort.
     */
    @Transactional
    public boolean processConnectCallback(Long sellerId, String status, String connectId) {
        // Validar existencia del usuario
        userRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new DomainException("Vendedor no encontrado con ID: " + sellerId));

        boolean isSuccess = "success".equalsIgnoreCase(status);

        // Delegar la actualización de los datos de vinculación al puerto de persistencia
        userRepositoryPort.updateMobbexConnectDetails(
                sellerId,
                isSuccess ? connectId : null,
                isSuccess
        );

        return isSuccess;
    }
}