package com.consentframework.consentmanagement.api.domain.repositories;

import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.IllegalArgumentException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.utils.pagination.ListPage;

/**
 * Interface specifying supported integrations with service user consent data.
 */
public interface ServiceUserConsentRepository {
    public static final String CONFLICTING_CONSENT_MESSAGE = "Consent already exists with serviceId %s, userId %s, consentId %s";
    public static final String CONSENT_NOT_FOUND_MESSAGE = "No consent found with serviceId %s, userId %s, consentId %s";

    /**
     * Save new consent to repository if does not yet exist.
     *
     * @param consent Consent object to save to the repository
     * @throws ConflictingResourceException exception thrown if consent already exists with same key
     * @throws IllegalArgumentException exception thrown if consent violates model constraints
     */
    void createServiceUserConsent(final Consent consent) throws ConflictingResourceException, IllegalArgumentException;

    /**
     * Retrieve consent from repository if exists.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     * @return specific consent for the service-user-consent ID tuple if exists
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    Consent getServiceUserConsent(final String serviceId, final String userId, final String consentId) throws ResourceNotFoundException;

    /**
     * List user's consents for a given service.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param limit maximum number of consents to retrieve
     * @param pageToken pagination token for backend consents query
     * @return page of matching Consents with next page token if applicable
     * @throws IllegalArgumentException exception thrown when receive invalid input
     */
    ListPage<Consent> listServiceUserConsents(final String serviceId, final String userId,
        final Integer limit, final String pageToken) throws IllegalArgumentException;
}
