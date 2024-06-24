package com.consentframework.consentmanagement.api.domain.validators;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.models.Consent;
import software.amazon.smithy.utils.StringUtils;

/**
 * Utility class for validating Consent objects before
 * pushing them to a backend repository.
 */
public final class ConsentValidator {
    public static final String CONSENT_NULL_MESSAGE = "consent must not be null";
    public static final String SERVICE_ID_BLANK_MESSAGE = "serviceId must not be blank";
    public static final String USER_ID_BLANK_MESSAGE = "userId must not be blank";
    public static final String CONSENT_ID_BLANK_MESSAGE = "consentId must not be blank";
    public static final String CONSENT_VERSION_NULL_MESSAGE = "consentVersion must not be null";
    public static final String STATUS_NULL_MESSAGE = "status must not be null";
    public static final String VERSION_CONFLICT_MESSAGE = "Expected consent version %d, received %d, indicating state conflict";

    private ConsentValidator() {}

    /**
     * Validate Consent satisfies model constraints.
     *
     * @param consent input consent data
     * @throws BadRequestException exception thrown if consent violates model constraints
     */
    public static void validate(final Consent consent) throws BadRequestException {
        if (consent == null) {
            throw new BadRequestException(CONSENT_NULL_MESSAGE);
        }
        if (StringUtils.isBlank(consent.getServiceId())) {
            throw new BadRequestException(SERVICE_ID_BLANK_MESSAGE);
        }
        if (StringUtils.isBlank(consent.getUserId())) {
            throw new BadRequestException(USER_ID_BLANK_MESSAGE);
        }
        if (StringUtils.isBlank(consent.getConsentId())) {
            throw new BadRequestException(CONSENT_ID_BLANK_MESSAGE);
        }
        if (consent.getConsentVersion() == null) {
            throw new BadRequestException(CONSENT_VERSION_NULL_MESSAGE);
        }
        if (consent.getStatus() == null) {
            throw new BadRequestException(STATUS_NULL_MESSAGE);
        }
    }

    /**
     * Validate updated Consent increments the stored version, to protect against state conflicts.
     *
     * @param existingConsent consent currently in data store
     * @param updatedConsent updated consent being submitted
     * @throws ConflictingResourceException exception thrown if updated consent does not have expected version
     */
    public static void validateNextConsentVersion(final Consent existingConsent, final Consent updatedConsent)
            throws ConflictingResourceException {
        final Integer expectedNextVersion = existingConsent.getConsentVersion() + 1;
        final Integer receivedVersion = updatedConsent.getConsentVersion();
        if (receivedVersion != expectedNextVersion) {
            throw new ConflictingResourceException(String.format(VERSION_CONFLICT_MESSAGE, expectedNextVersion.longValue(),
                receivedVersion.longValue()));
        }
    }
}
