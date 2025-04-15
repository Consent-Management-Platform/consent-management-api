package com.consentframework.consentmanagement.api.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import com.consentframework.shared.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.shared.api.infrastructure.mappers.DynamoDbConsentExpiryTimeConverter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.ZoneOffset;
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
            assertNull(parsedConsent.getConsentType());
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
    class ConsentToDynamoDbConsentTest {
        @Test
        void testMapToDynamoDbConsentWhenNull() {
            final Consent consent = null;
            final DynamoDbServiceUserConsent ddbItem = DynamoDbServiceUserConsentMapper.toDynamoDbServiceUserConsent(consent);
            assertNull(ddbItem);
        }

        @Test
        void testMapToDynamoDbConsentWhenOnlyRequiredFieldsPresent() {
            final Consent consent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
            final DynamoDbServiceUserConsent parsedDdbItem = DynamoDbServiceUserConsentMapper.toDynamoDbServiceUserConsent(consent);
            assertRequiredFieldsEqual(consent, parsedDdbItem);
            assertNull(parsedDdbItem.consentType());
            assertNull(parsedDdbItem.expiryTime());
            assertNull(parsedDdbItem.expiryTimeId());
            assertNull(parsedDdbItem.expiryHour());
        }

        @Test
        void testMapToDynamoDbConsentWhenAllFieldsPresent() {
            final Consent consent = TestConstants.TEST_CONSENT_WITH_ALL_FIELDS;
            final DynamoDbServiceUserConsent parsedDdbItem = DynamoDbServiceUserConsentMapper.toDynamoDbServiceUserConsent(consent);
            assertRequiredFieldsEqual(consent, parsedDdbItem);
            assertEquals(consent.getConsentType(), parsedDdbItem.consentType());
            assertEquals(consent.getConsentData(), parsedDdbItem.consentData());
            final String expectedExpiryTimeString = DynamoDbConsentExpiryTimeConverter.EXPIRY_TIME_FORMATTER
                .format(consent.getExpiryTime().withOffsetSameInstant(ZoneOffset.UTC));
            assertEquals(expectedExpiryTimeString, parsedDdbItem.expiryTime());
            assertEquals(String.format("%s|%s", expectedExpiryTimeString, TestConstants.TEST_PARTITION_KEY), parsedDdbItem.expiryTimeId());
            final String expectedExpiryHour = DynamoDbConsentExpiryTimeConverter.EXPIRY_HOUR_FORMATTER
                .format(consent.getExpiryTime().withOffsetSameInstant(ZoneOffset.UTC));
            assertEquals(expectedExpiryHour, parsedDdbItem.expiryHour());
        }

        @Test
        void testClearsAutoExpireGsiAttributesWhenStatusNotActive() {
            final Consent consent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS);
            consent.setStatus(ConsentStatus.REVOKED);
            final DynamoDbServiceUserConsent parsedDdbItem = DynamoDbServiceUserConsentMapper.toDynamoDbServiceUserConsent(consent);
            assertRequiredFieldsEqual(consent, parsedDdbItem);
            assertEquals(consent.getConsentType(), parsedDdbItem.consentType());
            assertEquals(consent.getConsentData(), parsedDdbItem.consentData());
            final String expectedExpiryTimeString = DynamoDbConsentExpiryTimeConverter.EXPIRY_TIME_FORMATTER
                .format(consent.getExpiryTime().withOffsetSameInstant(ZoneOffset.UTC));
            assertEquals(expectedExpiryTimeString, parsedDdbItem.expiryTime());
            assertNull(parsedDdbItem.expiryTimeId());
            assertNull(parsedDdbItem.expiryHour());
        }

        private void assertRequiredFieldsEqual(final Consent originalConsent, final DynamoDbServiceUserConsent parsedDynamoDbItem) {
            assertEquals(originalConsent.getServiceId(), parsedDynamoDbItem.serviceId());
            assertEquals(originalConsent.getUserId(), parsedDynamoDbItem.userId());
            assertEquals(originalConsent.getConsentId(), parsedDynamoDbItem.consentId());
            assertEquals(originalConsent.getConsentVersion(), parsedDynamoDbItem.consentVersion());
            assertEquals(originalConsent.getStatus().getValue(), parsedDynamoDbItem.consentStatus());
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
