package com.muvix.domain.model.exception;

import com.muvix.domain.model.management.enums.BoxPermission;

public class BoxAccessDeniedException extends RuntimeException {
    public BoxAccessDeniedException(Long userId, BoxPermission requiredPermission) {
        super("User " + userId + " lacks permission: " + requiredPermission);
    }
}
