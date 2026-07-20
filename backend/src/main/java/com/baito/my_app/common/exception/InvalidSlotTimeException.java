package com.baito.my_app.common.exception;

/**
 * Raised when a time range is not valid for the 30-minute slot model
 * (misaligned bounds, or end not after start).
 */
public class InvalidSlotTimeException extends DomainException {
    public InvalidSlotTimeException(String message) {
        super(message);
    }
}
