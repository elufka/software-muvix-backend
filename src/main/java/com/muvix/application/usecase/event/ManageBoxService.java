package com.muvix.application.usecase.event;

import com.muvix.application.port.in.event.ManageBoxUseCase;
import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.management.FullRelocationEvent;
import com.muvix.domain.model.management.PurchaseEvent;
import com.muvix.domain.model.management.RelocationEvent;
import com.muvix.domain.model.management.SaleEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManageBoxService implements ManageBoxUseCase {

    private final RelocationEventRepositoryPort boxRepositoryPort;
    private final SellableItemRepositoryPort itemRepositoryPort;

    public ManageBoxService(RelocationEventRepositoryPort boxRepositoryPort,
                            SellableItemRepositoryPort itemRepositoryPort) {
        this.boxRepositoryPort = boxRepositoryPort;
        this.itemRepositoryPort = itemRepositoryPort;
    }

    @Override
    @Transactional
    public void addItemToBox(AddItemToBoxCommand command) {
        RelocationEvent box = boxRepositoryPort.findById(command.boxId())
                .orElseThrow(() -> new DomainException("La Box especificada no existe"));

        // Validar que el usuario sea dueño o miembro compartido de la Box
        if (!box.isMember(command.userId())) {
            throw new DomainException("No tienes permisos para modificar esta Box");
        }

        SellableItem item = itemRepositoryPort.findById(command.itemId())
                .orElseThrow(() -> new DomainException("El producto o lote no existe"));

        // ===== VALIDACIONES CONTEXTUALES DE COMPRA Y VENTA =====
        if (box instanceof SaleEvent || box instanceof FullRelocationEvent) {
            // Contexto VENTA: El ítem a vender debe pertenecer al vendedor dueño de la Box
            if (!item.getSellerId().equals(command.userId())) {
                throw new DomainException("Solo puedes agregar a una Box de Venta productos de los cuales seas propietario.");
            }
        } else if (box instanceof PurchaseEvent) {
            // Contexto COMPRA: El usuario está armando su lista de deseos/mudanza para comprar
            if (item.getSellerId().equals(command.userId())) {
                throw new DomainException("No puedes agregar tus propios productos a una Box de Compra.");
            }
            if (!item.canBePurchased()) {
                throw new DomainException("El producto no está disponible para agregar a tu Box de Compra.");
            }
        }

        // Guardar la vinculación
        boxRepositoryPort.linkItemToBox(box.getId(), item.getId());
    }
}