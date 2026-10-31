package com.muvix.domain.model.exception;

public class DomainException extends RuntimeException {
    public DomainException(String mensaje) {
        super(mensaje);
    }
}
