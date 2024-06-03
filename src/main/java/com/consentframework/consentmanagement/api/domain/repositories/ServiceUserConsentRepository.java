package com.consentframework.consentmanagement.api.domain.repositories;

import com.consentframework.consentmanagement.api.models.Consent;

/**
 * Interface specifying supported integrations with
 * service user consent data.
 */
public interface ServiceUserConsentRepository {
    Consent getServiceUserConsent(final String serviceId, final String userId, final String consentId);
}
