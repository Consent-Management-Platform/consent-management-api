package com.consentframework.consentmanagement.api.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.consentframework.consentmanagement.api.infrastructure.constants.DynamoDbServiceUserConsentAttributeName;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

class DynamoDbServiceUserConsentMapperTest {
    @Test
    void testDdbItemToConsentWhenNull() {
        final Consent parsedConsent = DynamoDbServiceUserConsentMapper.ddbItemToConsent(null);
        assertNull(parsedConsent);
    }

    @Test
    void testDdbItemToConsentWhenAllFieldsPresent() {
        final Consent consent = TestConstants.TEST_CONSENT_WITH_ALL_FIELDS;

        final String partitionKey = String.format("%s|%s|%s", consent.getServiceId(), consent.getUserId(), consent.getConsentId());

        final Map<String, AttributeValue> ddbConsentData = new HashMap<String, AttributeValue>();
        for (final Map.Entry<String, String> entry : consent.getConsentData().entrySet()) {
            ddbConsentData.put(entry.getKey(), AttributeValue.fromS(entry.getValue()));
        }

        final Map<String, AttributeValue> ddbConsentAttributes = Map.of(
            DynamoDbServiceUserConsentAttributeName.ID.getValue(), AttributeValue.fromS(partitionKey),
            DynamoDbServiceUserConsentAttributeName.SERVICE_ID.getValue(), AttributeValue.fromS(consent.getServiceId()),
            DynamoDbServiceUserConsentAttributeName.USER_ID.getValue(), AttributeValue.fromS(consent.getUserId()),
            DynamoDbServiceUserConsentAttributeName.CONSENT_ID.getValue(), AttributeValue.fromS(consent.getConsentId()),
            DynamoDbServiceUserConsentAttributeName.CONSENT_VERSION.getValue(), AttributeValue.fromN(
                consent.getConsentVersion().toString()),
            DynamoDbServiceUserConsentAttributeName.CONSENT_STATUS.getValue(), AttributeValue.fromS(consent.getStatus().getValue()),
            DynamoDbServiceUserConsentAttributeName.CONSENT_DATA.getValue(), AttributeValue.fromM(ddbConsentData),
            DynamoDbServiceUserConsentAttributeName.EXPIRY_TIME.getValue(), AttributeValue.fromS(consent.getExpiryTime().toString())
        );

        final Consent parsedConsent = DynamoDbServiceUserConsentMapper.ddbItemToConsent(ddbConsentAttributes);
        assertEquals(consent, parsedConsent);
    }
}
