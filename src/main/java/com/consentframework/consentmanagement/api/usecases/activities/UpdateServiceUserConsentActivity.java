package com.consentframework.consentmanagement.api.usecases.activities;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.UpdateServiceUserConsentRequestContent;

/**
 * UpdateServiceUserConsent API activity.
 */
public class UpdateServiceUserConsentActivity {
    public static final String MISSING_CONSENT_DATA_MESSAGE = "Missing consent data for update";

    private final ServiceUserConsentRepository consentRepository;

    /**
     * Constructor for update consent activity.
     *
     * @param consentRepository consent data store
     */
    public UpdateServiceUserConsentActivity(final ServiceUserConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    /**
     * Handle request to update an existing ServiceUserConsent.
     *
     * @param serviceId service obtaining consent
     * @param userId user providing consent
     * @param consentId consent to update
     * @param updatedContent updated consent data
     * @throws BadRequestException exception thrown if provided invalid input
     * @throws ConflictingResourceException exception thrown if data store has conflicting data
     * @throws ResourceNotFoundException exception thrown if consent does not exist
     */
    public void handleRequest(final String serviceId, final String userId, final String consentId,
            final UpdateServiceUserConsentRequestContent updatedContent)
            throws BadRequestException, ConflictingResourceException, ResourceNotFoundException {
        if (updatedContent == null) {
            throw new BadRequestException(MISSING_CONSENT_DATA_MESSAGE);
        }

        final Consent updatedConsent = new Consent()
            .serviceId(serviceId)
            .userId(userId)
            .consentId(consentId)
            .consentVersion(updatedContent.getConsentVersion())
            .status(updatedContent.getStatus())
            .consentData(updatedContent.getConsentData())
            .expiryTime(updatedContent.getExpiryTime());

        consentRepository.updateServiceUserConsent(updatedConsent);
    }
}
