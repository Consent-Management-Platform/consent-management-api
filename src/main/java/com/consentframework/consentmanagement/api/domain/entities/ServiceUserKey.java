package com.consentframework.consentmanagement.api.domain.entities;

/**
 * Service-user pair, used to query for consents a user has with a service.
 */
public record ServiceUserKey(String serviceId, String userId) {}
