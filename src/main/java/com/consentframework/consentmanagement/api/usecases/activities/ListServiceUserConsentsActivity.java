package com.consentframework.consentmanagement.api.usecases.activities;

import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ListServiceUserConsentResponseContent;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.pagination.ListPage;

/**
 * ListServiceUserConsents API activity.
 */
public class ListServiceUserConsentsActivity {
    private final ServiceUserConsentRepository consentRepository;

    /**
     * Constructor for list consent activity.
     *
     * @param consentRepository consent data store
     */
    public ListServiceUserConsentsActivity(final ServiceUserConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    /**
     * Handle request to list ServiceUserConsents for a given service and user pair.
     *
     * @param serviceId service obtaining consent
     * @param userId user providing consent
     * @param limit maximum number of consents to retrieve
     * @param pageToken pagination token for backend consents query
     * @return page of matching Consents with next page token if applicable
     * @throws BadRequestException exception thrown when receive invalid input
     */
    public ListServiceUserConsentResponseContent handleRequest(final String serviceId, final String userId,
            final Integer limit, final String pageToken) throws BadRequestException {
        final ListPage<Consent> paginatedConsents = this.consentRepository.listServiceUserConsents(serviceId, userId, limit, pageToken);

        final String nextPageToken = parseNextPageToken(paginatedConsents);

        return new ListServiceUserConsentResponseContent()
            .data(paginatedConsents.resultsOnPage())
            .nextPageToken(nextPageToken);
    }

    private String parseNextPageToken(final ListPage<Consent> paginatedConsents) {
        if (paginatedConsents.nextPageToken().isEmpty()) {
            return null;
        }
        return paginatedConsents.nextPageToken().get();
    }
}
