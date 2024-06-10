package com.consentframework.consentmanagement.api.domain.exceptions;

/**
 * Exception class representing 400 Bad Request errors.
 *
 * This exception is thrown when an operation is called with invalid input.
 */
public class BadRequestException extends Exception {
    /**
     * Construct BadRequestException with an error message.
     *
     * @param message error message
     */
    public BadRequestException(final String message) {
        super(message);
    }

    /**
     * Construct BadRequestException with an error message and original cause.
     *
     * @param message error message
     * @param cause original exception thrown
     */
    public BadRequestException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
