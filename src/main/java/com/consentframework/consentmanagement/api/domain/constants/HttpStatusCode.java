package com.consentframework.consentmanagement.api.domain.constants;

/**
 * HTTP status code values.
 */
public enum HttpStatusCode {
    SUCCESS(200),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    CONFLICT(409),
    TOO_MANY_REQUESTS(429),
    INTERNAL_SERVER_ERROR(500);

    private final Integer value;

    private HttpStatusCode(final Integer value) {
        this.value = value;
    }

    /**
     * Return status code integer value.
     *
     * @return status code
     */
    public Integer getValue() {
        return value;
    }
}
