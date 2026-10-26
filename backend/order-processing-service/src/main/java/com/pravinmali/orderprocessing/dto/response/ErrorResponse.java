package com.pravinmali.orderprocessing.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardised error response body returned for all 4xx and 5xx responses.
 *
 * <p>Structure:
 * <pre>
 * {
 *   "timestamp": "2026-10-26T10:00:00",
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "message": "One or more fields have invalid values",
 *   "fieldErrors": [
 *     { "field": "customerEmail", "message": "must be a valid email address" }
 *   ]
 * }
 * </pre>
 *
 * {@code fieldErrors} is omitted from the JSON when null (non-validation errors).
 * </p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<FieldError> fieldErrors
) {

    /**
     * Represents a single field-level validation failure.
     */
    public record FieldError(
            String field,
            String message
    ) {}

    /** Convenience factory for simple (non-validation) errors. */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, null);
    }

    /** Convenience factory for validation errors with field-level details. */
    public static ErrorResponse of(int status, String error, String message, List<FieldError> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, fieldErrors);
    }
}
