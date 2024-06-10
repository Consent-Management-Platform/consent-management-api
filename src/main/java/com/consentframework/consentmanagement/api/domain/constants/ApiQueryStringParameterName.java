package com.consentframework.consentmanagement.api.domain.constants;

/**
 * API query string parameter names.
 */
public enum ApiQueryStringParameterName {
    LIMIT("limit"),
    PAGE_TOKEN("pageToken");

    private final String value;

    private ApiQueryStringParameterName(final String value) {
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
