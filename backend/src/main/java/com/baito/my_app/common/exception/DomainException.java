package com.baito.my_app.common.exception;

/**
 * Base type for all domain / business rule violations.
 * Intentionally free of any web (HttpStatus) or persistence (JPA) types so that the
 * domain layer stays framework-agnostic. HTTP mapping happens in the web adapter
 * ({@code GlobalExceptionHandler}).
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
