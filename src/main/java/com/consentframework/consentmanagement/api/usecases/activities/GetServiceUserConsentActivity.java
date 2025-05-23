package com.consentframework.consentmanagement.api.usecases.activities;

import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.GetServiceUserConsentResponseContent;
import com.consentframework.shared.api.domain.exceptions.InternalServiceException;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;

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
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     * @return specific consent for the service-user-consent ID tuple if exists
     * @throws InternalServiceException exception thrown if unexpected error querying repository
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    public GetServiceUserConsentResponseContent handleRequest(final String serviceId, final String userId, final String consentId)
            throws InternalServiceException, ResourceNotFoundException {
        final Consent retrievedConsent = consentRepository.getServiceUserConsent(serviceId, userId, consentId);

        return new GetServiceUserConsentResponseContent()
            .data(retrievedConsent);
    }
}
