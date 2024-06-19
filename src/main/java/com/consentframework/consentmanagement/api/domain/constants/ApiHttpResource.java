package com.consentframework.consentmanagement.api.domain.constants;

/**
 * API HTTP resources, which represent REST resource paths with path parameter placeholders.
 */
public enum ApiHttpResource {
    SERVICE_USER_CONSENT("/v1/consent-management/services/{serviceId}/users/{userId}/consents/{consentId}"),
    SERVICE_USER_CONSENTS("/v1/consent-management/services/{serviceId}/users/{userId}/consents");

    private final String value;

    private ApiHttpResource(final String value) {
        this.value = value;
    }

    /**
     * Return resource path.
     *
     * @return resource path
     */
    public String getValue() {
        return value;
    }
}
