package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.management.RelocationEvent;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida para la gestión de Boxes (Venta, Compra, Mudanza Completa) en MySQL.
 */
public interface RelocationEventRepositoryPort {

    RelocationEvent save(RelocationEvent event);

    Optional<RelocationEvent> findById(Long id);

    Optional<RelocationEvent> findByShareToken(String shareToken);

    List<RelocationEvent> findByUserId(Long userId);

    void linkItemToBox(Long boxId, Long itemId);

    void unlinkItemFromBox(Long boxId, Long itemId);
}