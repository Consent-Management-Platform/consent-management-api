package com.consentframework.consentmanagement.api.domain.repositories;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.pagination.ListPage;
import com.consentframework.consentmanagement.api.models.Consent;

/**
 * Interface specifying supported integrations with service user consent data.
 */
public interface ServiceUserConsentRepository {
    public static final String CONSENT_ALREADY_EXISTS_MESSAGE = "Consent already exists with serviceId %s, userId %s, consentId %s";
    public static final String CONSENT_NOT_FOUND_MESSAGE = "No consent found with serviceId %s, userId %s, consentId %s";

    /**
     * Save new consent to repository if does not yet exist.
     *
     * @param consent Consent object to save to the repository
     * @throws BadRequestException exception thrown if consent violates model constraints
     * @throws ConflictingResourceException exception thrown if consent already exists with same key
     */
    void createServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException;

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
     * Update existing consent with new data.
     *
     * @param consent Consent object to save to the repository
     * @throws BadRequestException exception thrown if consent violates model constraints
     * @throws ConflictingResourceException exception thrown if stored consent has conflicting data
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    void updateServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException,
        ResourceNotFoundException;

    /**
     * List user's consents for a given service.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param limit maximum number of consents to retrieve
     * @param pageToken pagination token for backend consents query
     * @return page of matching Consents with next page token if applicable
     * @throws BadRequestException exception thrown when receive invalid input
     */
    ListPage<Consent> listServiceUserConsents(final String serviceId, final String userId,
        final Integer limit, final String pageToken) throws BadRequestException;
}
