package com.consentframework.consentmanagement.api.infrastructure.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent.Builder;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.Test;

class DynamoDbServiceUserConsentTest {
    @Test
    void testBuilderWithOnlyRequiredFields() {
        final Consent consent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        final DynamoDbServiceUserConsent ddbConsent = dynamoDbConsentBuilderWithRequiredFields(consent).build();
        assertRequiredFieldsEqual(consent, ddbConsent);
    }

    @Test
    void testBuilderWithAllFields() {
        final Consent consent = TestConstants.TEST_CONSENT_WITH_ALL_FIELDS;
        final DynamoDbServiceUserConsent ddbConsent = dynamoDbConsentBuilderWithRequiredFields(consent)
            .consentData(consent.getConsentData())
            // TODO: Add consentType to API model
            .consentType("TestConsentType")
            .expiryTime(consent.getExpiryTime().toString())
            .build();

        assertRequiredFieldsEqual(consent, ddbConsent);
        assertEquals(consent.getConsentData(), ddbConsent.consentData());
        assertEquals(consent.getExpiryTime().toString(), ddbConsent.expiryTime());
    }

    private Builder dynamoDbConsentBuilderWithRequiredFields(final Consent consent) {
        return DynamoDbServiceUserConsent.builder()
            .id(String.format("%s|%s|%s", consent.getServiceId(), consent.getUserId(), consent.getConsentId()))
            .serviceId(consent.getServiceId())
            .userId(consent.getUserId())
            .consentId(consent.getConsentId())
            .consentVersion(consent.getConsentVersion())
            .consentStatus(consent.getStatus());
    }

    private void assertRequiredFieldsEqual(final Consent consent, final DynamoDbServiceUserConsent ddbConsent) {
        assertEquals(consent.getServiceId(), ddbConsent.serviceId());
        assertEquals(consent.getUserId(), ddbConsent.userId());
        assertEquals(consent.getConsentId(), ddbConsent.consentId());
        assertEquals(consent.getConsentVersion(), ddbConsent.consentVersion());
        assertEquals(consent.getStatus(), ddbConsent.consentStatus());
    }
}
