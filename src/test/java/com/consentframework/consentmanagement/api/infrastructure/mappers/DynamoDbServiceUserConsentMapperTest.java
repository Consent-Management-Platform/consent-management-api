package com.consentframework.consentmanagement.api.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

class DynamoDbServiceUserConsentMapperTest {
    @Nested
    class DynamoDbItemToConsentTest {
        @Test
        void testMapToConsentWhenNull() {
            final DynamoDbServiceUserConsent ddbItem = null;
            final Consent parsedConsent = DynamoDbServiceUserConsentMapper.dynamoDbItemToConsent(ddbItem);
            assertNull(parsedConsent);
        }

        @Test
        void testMapToConsentWhenOnlyRequiredFieldsPresent() {
            final Consent parsedConsent = DynamoDbServiceUserConsentMapper.dynamoDbItemToConsent(
                TestConstants.TEST_DDB_CONSENT_WITH_ONLY_REQUIRED_FIELDS);
            assertEquals(TestConstants.TEST_SERVICE_ID, parsedConsent.getServiceId());
            assertEquals(TestConstants.TEST_USER_ID, parsedConsent.getUserId());
            assertEquals(TestConstants.TEST_CONSENT_ID, parsedConsent.getConsentId());
            assertEquals(TestConstants.TEST_CONSENT_VERSION, parsedConsent.getConsentVersion());
            assertNull(parsedConsent.getConsentData());
            assertNull(parsedConsent.getExpiryTime());
        }

        @Test
        void testMapToConsentWhenAllFieldsPresent() {
            final Consent parsedConsent = DynamoDbServiceUserConsentMapper.dynamoDbItemToConsent(
                TestConstants.TEST_DDB_CONSENT_WITH_ALL_FIELDS);
            assertEquals(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS, parsedConsent);
        }
    }

    @Nested
    class DynamoDbAttributeMapToConsentTest {
        @Test
        void testMapToConsentWhenNull() {
            final Map<String, AttributeValue> ddbItemMap = null;
            final Consent parsedConsent = DynamoDbServiceUserConsentMapper.dynamoDbAttributeMapToConsent(ddbItemMap);
            assertNull(parsedConsent);
        }

        @Test
        void testMapToConsentWhenAllFieldsPresent() {
            final Consent parsedConsent = DynamoDbServiceUserConsentMapper.dynamoDbAttributeMapToConsent(
                TestConstants.TEST_CONSENT_DDB_ATTRIBUTES);
            assertEquals(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS, parsedConsent);
        }
    }
}
