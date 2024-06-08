package com.consentframework.consentmanagement.api.domain.validators;

import com.consentframework.consentmanagement.api.domain.exceptions.IllegalArgumentException;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;

/**
 * Validator for CreateServiceUserConsent API request body.
 */
public final class CreateServiceUserConsentRequestValidator {
    public static final String MISSING_REQUIRED_FIELDS_MESSAGE = "Missing required inputs, must provide status";

    /**
     * No constructor needed since this is a utility class.
     */
    private CreateServiceUserConsentRequestValidator() {}

    /**
     * Validate that request content has all required fields.
     *
     * @param requestContent create consent request body
     * @throws IllegalArgumentException exception thrown if missing required fields
     */
    public static void validate(final CreateServiceUserConsentRequestContent requestContent) throws IllegalArgumentException {
        if (requestContent == null || requestContent.getStatus() == null) {
            throw new IllegalArgumentException(MISSING_REQUIRED_FIELDS_MESSAGE);
        }
    }
}
