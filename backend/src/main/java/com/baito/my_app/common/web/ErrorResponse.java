package com.baito.my_app.common.web;

/**
 * Uniform error body. {@code code} is a stable machine-readable string the frontend can switch on
 * (e.g. to raise the assignment alerts of requirement 7); {@code message} is human-readable.
 */
public record ErrorResponse(String code, String message) {
}
