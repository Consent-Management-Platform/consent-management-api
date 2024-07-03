package com.consentframework.consentmanagement.api.infrastructure.constants;

/**
 * Attribute names for the ServiceUserConsent DynamoDB table.
 */
public enum DynamoDbServiceUserConsentAttributeName {
    ID("id"),
    SERVICE_ID("serviceId"),
    USER_ID("userId"),
    CONSENT_ID("consentId"),
    CONSENT_VERSION("consentVersion"),
    CONSENT_STATUS("consentStatus"),
    CONSENT_TYPE("consentType"),
    CONSENT_DATA("consentData"),
    EXPIRY_TIME("expiryTime");

    private final String value;

    private DynamoDbServiceUserConsentAttributeName(final String value) {
        this.value = value;
    }

    /**
     * Return attribute name.
     *
     * @return attribute name
     */
    public String getValue() {
        return value;
    }
}
