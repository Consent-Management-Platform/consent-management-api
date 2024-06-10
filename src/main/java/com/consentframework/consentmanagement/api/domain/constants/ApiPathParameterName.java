package com.consentframework.consentmanagement.api.domain.constants;

import java.util.List;

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

    // Required path parameters for /consents APIs, eg. List and Create consent APIs
    public static final List<ApiPathParameterName> CONSENTS_PATH_PARAMETERS = List.of(SERVICE_ID, USER_ID);

    // Required path parameters for /consents/{consentId} APIs, eg. Get and Update consent APIs
    public static final List<ApiPathParameterName> CONSENT_PATH_PARAMETERS = List.of(SERVICE_ID, USER_ID, CONSENT_ID);
}
