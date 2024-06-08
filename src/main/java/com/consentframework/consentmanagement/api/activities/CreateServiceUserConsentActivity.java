package com.consentframework.consentmanagement.api.activities;

import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.IllegalArgumentException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.domain.validators.CreateServiceUserConsentRequestValidator;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentResponseContent;

import java.util.UUID;

/**
 * CreateServiceUserConsent API activity.
 */
public class CreateServiceUserConsentActivity {
    private final ServiceUserConsentRepository consentRepository;

    /**
     * Constructor for create consent activity.
     *
     * @param consentRepository consent data store
     */
    public CreateServiceUserConsentActivity(final ServiceUserConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    /**
     * Handle request to create a new ServiceUserConsent.
     *
     * @param serviceId service obtaining consent
     * @param userId user providing consent
     * @param request consent data
     * @return response including the UUID of the created consent
     * @throws ConflictingResourceException exception thrown if data store has conflicting data
     * @throws IllegalArgumentException exception thrown if provided invalid input
     */
    public CreateServiceUserConsentResponseContent handleRequest(final String serviceId, final String userId,
            final CreateServiceUserConsentRequestContent request) throws ConflictingResourceException, IllegalArgumentException {
        CreateServiceUserConsentRequestValidator.validate(request);

        final String consentId = UUID.randomUUID().toString();
        final Consent consent = new Consent()
            .serviceId(serviceId)
            .userId(userId)
            .consentId(consentId)
            .consentVersion(1)
            .consentData(request.getConsentData())
            .expiryTime(request.getExpiryTime())
            .status(request.getStatus());

        consentRepository.createServiceUserConsent(consent);

        return new CreateServiceUserConsentResponseContent()
            .consentId(consentId);
    }
}
