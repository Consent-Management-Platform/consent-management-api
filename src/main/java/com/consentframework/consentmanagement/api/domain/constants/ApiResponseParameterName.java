package com.consentframework.consentmanagement.api.domain.constants;

/**
 * API response body parameter names.
 */
public enum ApiResponseParameterName {
    BODY("body"),
    HEADERS("headers"),
    STATUS_CODE("statusCode");

    private final String value;

    private ApiResponseParameterName(final String value) {
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
