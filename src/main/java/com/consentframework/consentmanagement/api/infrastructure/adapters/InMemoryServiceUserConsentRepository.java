package com.consentframework.consentmanagement.api.infrastructure.adapters;

import com.consentframework.consentmanagement.api.domain.entities.ConsentValidator;
import com.consentframework.consentmanagement.api.domain.entities.ServiceUserConsentKey;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InvalidConsentDataException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of ServiceUserConsentRepository.
 */
public class InMemoryServiceUserConsentRepository implements ServiceUserConsentRepository {

    private Map<ServiceUserConsentKey, Consent> inMemoryConsentStore = new HashMap<ServiceUserConsentKey, Consent>();

    /**
     * Add consent to in-memory store if does not yet exist.
     *
     * @param consent Consent object to save to the repository
     * @throws ConflictingResourceException exception thrown if consent already exists with same key
     * @throws InvalidConsentDataException exception thrown if consent violates model constraints
     */
    @Override
    public void createServiceUserConsent(final Consent consent) throws ConflictingResourceException, InvalidConsentDataException {
        final ServiceUserConsentKey key = new ServiceUserConsentKey(consent.getServiceId(), consent.getUserId(), consent.getConsentId());
        if (inMemoryConsentStore.containsKey(key)) {
            throw new ConflictingResourceException(String.format(CONFLICTING_CONSENT_MESSAGE,
                consent.getServiceId(), consent.getUserId(), consent.getConsentId()));
        }
        ConsentValidator.validate(consent);
        inMemoryConsentStore.put(key, consent);
    }

    /**
     * Retrieve consent from in-memory store if exists.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     * @return specific consent for the service-user-consent ID tuple if exists
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    @Override
    public Consent getServiceUserConsent(final String serviceId, final String userId, final String consentId)
            throws ResourceNotFoundException {
        final ServiceUserConsentKey key = new ServiceUserConsentKey(serviceId, userId, consentId);
        final Consent retrievedConsent = inMemoryConsentStore.get(key);

        if (retrievedConsent == null) {
            throw new ResourceNotFoundException(String.format(CONSENT_NOT_FOUND_MESSAGE, serviceId, userId, consentId));
        }
        return retrievedConsent;
    }
}
