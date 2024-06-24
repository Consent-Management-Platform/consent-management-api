package com.consentframework.consentmanagement.api.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.Test;

class DynamoDbServiceUserConsentMapperTest {
    @Test
    void testDdbItemToConsentWhenNull() {
        final Consent parsedConsent = DynamoDbServiceUserConsentMapper.ddbItemToConsent(null);
        assertNull(parsedConsent);
    }

    @Test
    void testDdbItemToConsentWhenAllFieldsPresent() {
        final Consent consent = TestConstants.TEST_CONSENT_WITH_ALL_FIELDS;
        final Consent parsedConsent = DynamoDbServiceUserConsentMapper.ddbItemToConsent(TestConstants.TEST_CONSENT_DDB_ATTRIBUTES);
        assertEquals(consent, parsedConsent);
    }
}
