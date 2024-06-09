package com.consentframework.consentmanagement.api.usecases.activities;

import com.consentframework.consentmanagement.api.domain.entities.GetServiceUserConsentRequestContent;
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
     * @param request request content including service/user/consent IDs
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    public GetServiceUserConsentResponseContent handleRequest(final GetServiceUserConsentRequestContent request)
            throws ResourceNotFoundException {
        final Consent retrievedConsent = consentRepository.getServiceUserConsent(
            request.serviceId(), request.userId(), request.consentId());

        return new GetServiceUserConsentResponseContent()
            .data(retrievedConsent);
    }
}
