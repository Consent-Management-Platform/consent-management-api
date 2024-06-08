package com.consentframework.consentmanagement.api.activities;

import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.GetServiceUserConsentResponseContent;

/**
 * GetServiceUserConsent API activity.
 */
public class GetServiceUserConsentActivity {
    private final ServiceUserConsentRepository consentRepository;

    /**
     * Constructor for get consent activity.
     *
     * @param consentRepository consent data store
     */
    public GetServiceUserConsentActivity(final ServiceUserConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    /**
     * Handle request to retrieve a given ServiceUserConsent.
     *
     * @param serviceId service obtaining consent
     * @param userId user providing consent
     * @param consentId consent ID
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    public GetServiceUserConsentResponseContent handleRequest(final String serviceId, final String userId, final String consentId)
            throws ResourceNotFoundException {
        final Consent retrievedConsent = consentRepository.getServiceUserConsent(serviceId, userId, consentId);

        return new GetServiceUserConsentResponseContent()
            .data(retrievedConsent);
    }
}
