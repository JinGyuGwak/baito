package com.baito.my_app.common.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Uniform error body. {@code code} is a stable machine-readable string the frontend can switch on
 * (e.g. to raise the assignment alerts of requirement 7); {@code message} is human-readable.
 */
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
}
