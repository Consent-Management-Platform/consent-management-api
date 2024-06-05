package com.consentframework.consentmanagement.api.domain.entities;

/**
 * Composite key of a consent entity, which is specific to a service/user pair.
 */
public record ServiceUserConsentKey(String serviceId, String userId, String consentId) {}
