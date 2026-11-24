package com.muvix.domain.model.management.enums;

public enum BoxPermission {
    // Gestión de contenido
    VIEW_BOX,
    ADD_ITEMS,
    REMOVE_ITEMS,
    EDIT_PRICES,

    // Gestión de miembros
    INVITE_MEMBERS,
    REMOVE_MEMBERS,
    PROMOTE_TO_ADMIN,
    REVOKE_ADMIN,

    // Ciclo de vida crítico
    UPDATE_BOX_SETTINGS,
    COMPLETE_BOX,
    CANCEL_BOX
}