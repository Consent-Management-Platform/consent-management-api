package com.consentframework.consentmanagement.api.domain.exceptions;

/**
 * Exception class representing 409 Conflict errors.
 *
 * This exception is thrown when a create/update operation
 * conflicts with the currently stored state.
 */
public class ConflictingResourceException extends Exception {
    /**
     * Construct ConflictingResourceException with an error message.
     *
     * @param message error message.
     */
    public ConflictingResourceException(final String message) {
        super(message);
    }
}
