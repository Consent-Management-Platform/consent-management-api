package com.consentframework.consentmanagement.api.infrastructure.entities;

/**
 * Service-user pair, used to query for consents a user has with a service.
 */
public record InMemoryServiceUserKey(String serviceId, String userId) {}
