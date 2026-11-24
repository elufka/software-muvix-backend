package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.management.Box;
import com.muvix.domain.model.management.FullRelocationBox;
import com.muvix.infrastructure.adapters.in.rest.controller.BoxController;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida para la gestión de Boxes (Venta, Compra, Mudanza Completa) en MySQL.
 */
public interface BoxRepositoryPort {

    Box save(Box event);

    Optional<Box> findById(Long id);

    Optional<Box> findByShareToken(String shareToken);

    List<Box> findByUserId(Long userId);

    List<Long> findItemIdsByEventId(Long eventId);

    void linkItemToBox(Long boxId, Long itemId);

    void unlinkItemFromBox(Long boxId, Long itemId);

    List<Box> findPurchaseBoxesByUserId(Long userId);
}
