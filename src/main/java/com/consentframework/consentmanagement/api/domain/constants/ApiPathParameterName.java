package com.consentframework.consentmanagement.api.domain.constants;

/**
 * API path parameter names.
 */
public enum ApiPathParameterName {
    SERVICE_ID("serviceId"),
    USER_ID("userId"),
    CONSENT_ID("consentId");

    private final String value;

    private ApiPathParameterName(final String value) {
        this.value = value;
    }

    /**
     * Return parameter name.
     *
     * @return parameter name
     */
    public String getValue() {
        return value;
    }
}
