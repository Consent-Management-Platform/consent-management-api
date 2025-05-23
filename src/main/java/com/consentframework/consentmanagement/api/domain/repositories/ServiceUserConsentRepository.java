package com.consentframework.consentmanagement.api.domain.repositories;

import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.shared.api.domain.exceptions.InternalServiceException;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.shared.api.domain.pagination.ListPage;

/**
 * Interface specifying supported integrations with service user consent data.
 */
public interface ServiceUserConsentRepository {
    public static final String CONSENT_ALREADY_EXISTS_MESSAGE = "Consent already exists with serviceId: %s, userId: %s, consentId: %s";
    public static final String CONSENT_NOT_FOUND_MESSAGE = "No consent found with serviceId: %s, userId: %s, consentId: %s";

    /**
     * Save new consent to repository if does not yet exist.
     *
     * @param consent Consent object to save to the repository
     * @throws BadRequestException exception thrown if consent violates model constraints
     * @throws ConflictingResourceException exception thrown if consent already exists with same key
     * @throws InternalServiceException exception thrown if unexpected server error creating consent
     */
    void createServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException,
        InternalServiceException;

    /**
     * Retrieve consent from repository if exists.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     * @return specific consent for the service-user-consent ID tuple if exists
     * @throws InternalServiceException exception thrown if unexpected error querying repository
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    Consent getServiceUserConsent(final String serviceId, final String userId, final String consentId)
        throws InternalServiceException, ResourceNotFoundException;

    /**
     * Update existing consent with new data.
     *
     * @param consent Consent object to save to the repository
     * @throws BadRequestException exception thrown if consent violates model constraints
     * @throws ConflictingResourceException exception thrown if stored consent has conflicting data
     * @throws InternalServiceException exception thrown if unexpected server-side error updating consent
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    void updateServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException,
        ResourceNotFoundException, InternalServiceException;

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
