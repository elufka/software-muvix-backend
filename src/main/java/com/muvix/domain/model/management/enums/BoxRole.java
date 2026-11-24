package com.muvix.domain.model.management.enums;

import java.util.EnumSet;
import java.util.Set;

public enum BoxRole {

    OWNER(EnumSet.allOf(BoxPermission.class)), // Tiene absolutamente todos los permisos

    ADMIN(EnumSet.of(
            BoxPermission.VIEW_BOX,
            BoxPermission.ADD_ITEMS,
            BoxPermission.REMOVE_ITEMS,
            BoxPermission.EDIT_PRICES,
            BoxPermission.INVITE_MEMBERS,
            BoxPermission.REMOVE_MEMBERS,
            BoxPermission.PROMOTE_TO_ADMIN, // Puede nombrar otros admins
            BoxPermission.UPDATE_BOX_SETTINGS
    )),

    MEMBER(EnumSet.of(
            BoxPermission.VIEW_BOX,
            BoxPermission.ADD_ITEMS
    ));

    private final Set<BoxPermission> permissions;

    BoxRole(Set<BoxPermission> permissions) {
        this.permissions = Set.copyOf(permissions);
    }

    public boolean hasPermission(BoxPermission permission) {
        return permissions.contains(permission);
    }
}