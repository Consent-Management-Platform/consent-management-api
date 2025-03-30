package com.consentframework.consentmanagement.api.infrastructure.entities;

/**
 * Composite key of a consent entity, which is specific to a service/user pair.
 */
public record InMemoryServiceUserConsentKey(String serviceId, String userId, String consentId) {}
