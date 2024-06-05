package com.consentframework.consentmanagement.api.domain.exceptions;

/**
 * Exception class representing 404 Resource Not Found errors.
 */
public class ResourceNotFoundException extends Exception {
    /**
     * Construct ResourceNotFoundException with an error message.
     *
     * @param message error message.
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
