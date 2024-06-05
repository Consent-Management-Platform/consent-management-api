package com.consentframework.consentmanagement.api.domain.exceptions;

/**
 * Exception class representing 400 Bad Request errors related to invalid Consent data.
 *
 * This exception is thrown when a create/update operation is called with invalid input.
 */
public class InvalidConsentDataException extends Exception {
    /**
     * Construct InvalidConsentInputException with an error message.
     *
     * @param message error message.
     */
    public InvalidConsentDataException(final String message) {
        super(message);
    }
}
