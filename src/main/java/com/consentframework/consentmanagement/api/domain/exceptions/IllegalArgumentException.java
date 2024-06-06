package com.consentframework.consentmanagement.api.domain.exceptions;

/**
 * Exception class representing 400 Bad Request errors related to invalid input data.
 *
 * This exception is thrown when an operation is called with invalid input.
 */
public class IllegalArgumentException extends Exception {
    /**
     * Construct IllegalArgumentException with an error message.
     *
     * @param message error message.
     */
    public IllegalArgumentException(final String message) {
        super(message);
    }
}
