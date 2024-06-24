package com.consentframework.consentmanagement.api.domain.exceptions;

/**
 * Exception class representing 500 Internal Error errors.
 *
 * This exception is thrown when receive unexpected service-side exception.
 */
public class InternalServiceException extends Exception {
    /**
     * Construct InternalServiceException with an error message.
     *
     * @param message error message
     */
    public InternalServiceException(final String message) {
        super(message);
    }
}
