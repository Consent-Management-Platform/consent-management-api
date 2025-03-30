package com.consentframework.consentmanagement.api.infrastructure.repositories;

import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.domain.validators.ConsentValidator;
import com.consentframework.consentmanagement.api.infrastructure.entities.InMemoryServiceUserConsentKey;
import com.consentframework.consentmanagement.api.infrastructure.entities.InMemoryServiceUserKey;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.shared.api.domain.pagination.ListPage;
import com.consentframework.shared.api.domain.pagination.ListPaginator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of ServiceUserConsentRepository.
 */
public class InMemoryServiceUserConsentRepository implements ServiceUserConsentRepository {
    public static final String INVALID_PAGE_TOKEN_MESSAGE = "Received invalid pagination token %s, expected an integer value";

    private Map<InMemoryServiceUserConsentKey, Consent> inMemoryConsentStore = new HashMap<InMemoryServiceUserConsentKey, Consent>();
    private Map<InMemoryServiceUserKey, List<Consent>> inMemoryConsentsByServiceUserIndex =
        new HashMap<InMemoryServiceUserKey, List<Consent>>();

    /**
     * Add consent to in-memory store if does not yet exist.
     *
     * @param consent Consent object to save to the repository
     * @throws BadRequestException exception thrown if consent violates model constraints
     * @throws ConflictingResourceException exception thrown if consent already exists with same key
     */
    @Override
    public void createServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException {
        final InMemoryServiceUserConsentKey key = new InMemoryServiceUserConsentKey(
            consent.getServiceId(), consent.getUserId(), consent.getConsentId());
        if (inMemoryConsentStore.containsKey(key)) {
            throw new ConflictingResourceException(String.format(CONSENT_ALREADY_EXISTS_MESSAGE,
                consent.getServiceId(), consent.getUserId(), consent.getConsentId()));
        }
        ConsentValidator.validate(consent);
        storeValidatedConsent(key, consent);
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
        final InMemoryServiceUserConsentKey key = new InMemoryServiceUserConsentKey(serviceId, userId, consentId);
        final Consent retrievedConsent = inMemoryConsentStore.get(key);

        if (retrievedConsent == null) {
            throw new ResourceNotFoundException(String.format(CONSENT_NOT_FOUND_MESSAGE, serviceId, userId, consentId));
        }
        return retrievedConsent;
    }

    /**
     * Update existing consent with new data.
     *
     * @param consent Consent object to save to the repository
     * @throws BadRequestException exception thrown if consent violates model constraints
     * @throws ConflictingResourceException exception thrown if stored consent has conflicting data
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    @Override
    public void updateServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException,
            ResourceNotFoundException {
        ConsentValidator.validate(consent);

        final Consent existingConsent = getServiceUserConsent(consent.getServiceId(), consent.getUserId(), consent.getConsentId());
        ConsentValidator.validateNextConsentVersion(existingConsent, consent);

        final InMemoryServiceUserConsentKey key = new InMemoryServiceUserConsentKey(
            consent.getServiceId(), consent.getUserId(), consent.getConsentId());
        storeValidatedConsent(key, consent);
    }

    /**
     * List user's consents for a given service.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param limit maximum number of consents to retrieve
     * @param pageToken pagination token for backend consents query
     * @return page of matching consents stored for the service/user pair
     * @throws BadRequestException exception thrown when receive invalid input
     */
    @Override
    public ListPage<Consent> listServiceUserConsents(final String serviceId, final String userId,
            final Integer limit, final String pageToken) throws BadRequestException {
        final List<Consent> allMatchingConsents = inMemoryConsentsByServiceUserIndex.get(new InMemoryServiceUserKey(serviceId, userId));

        final Integer parsedPageToken = parsePageToken(pageToken);
        return new ListPaginator<Consent>().getSinglePage(allMatchingConsents, limit, parsedPageToken);
    }

    private void storeValidatedConsent(final InMemoryServiceUserConsentKey key, final Consent consent) {
        inMemoryConsentStore.put(key, consent);

        final InMemoryServiceUserKey serviceUserIndexKey = new InMemoryServiceUserKey(consent.getServiceId(), consent.getUserId());
        final List<Consent> serviceUserConsents = inMemoryConsentsByServiceUserIndex.getOrDefault(
            serviceUserIndexKey, new ArrayList<Consent>());
        serviceUserConsents.add(consent);
        inMemoryConsentsByServiceUserIndex.put(serviceUserIndexKey, serviceUserConsents);
    }

    private Integer parsePageToken(final String pageToken) throws BadRequestException {
        if (pageToken == null) {
            return null;
        }

        try {
            return Integer.parseInt(pageToken);
        } catch (final NumberFormatException e) {
            throw new BadRequestException(String.format(INVALID_PAGE_TOKEN_MESSAGE, pageToken));
        }
    }
}
