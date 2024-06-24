package com.consentframework.consentmanagement.api.infrastructure.mappers;

import com.consentframework.consentmanagement.api.infrastructure.constants.DynamoDbServiceUserConsentAttributeName;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility methods for mappings between ServiceUserConsent DynamoDB items and Consent objects.
 */
public final class DynamoDbServiceUserConsentMapper {
    private DynamoDbServiceUserConsentMapper() {}

    /**
     * Convert ServiceUserConsent DynamoDB item to Consent object.
     *
     * @return normalized Consent data model
     */
    public static Consent ddbItemToConsent(final Map<String, AttributeValue> ddbConsentItem) {
        if (ddbConsentItem == null) {
            return null;
        }

        return new Consent()
            .serviceId(parseStringAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.SERVICE_ID))
            .userId(parseStringAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.USER_ID))
            .consentId(parseStringAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.CONSENT_ID))
            .consentVersion(parseIntegerAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.CONSENT_VERSION))
            .status(parseConsentStatus(ddbConsentItem))
            .consentData(parseConsentData(ddbConsentItem))
            .expiryTime(parseExpiryTime(ddbConsentItem));
    }

    private static String parseStringAttribute(final Map<String, AttributeValue> ddbConsentItem,
           final DynamoDbServiceUserConsentAttributeName attributeName) {
        return ddbConsentItem.get(attributeName.getValue()).s();
    }

    private static Integer parseIntegerAttribute(final Map<String, AttributeValue> ddbConsentItem,
            final DynamoDbServiceUserConsentAttributeName attributeName) {
        return Integer.parseInt(ddbConsentItem.get(attributeName.getValue()).n());
    }

    private static ConsentStatus parseConsentStatus(final Map<String, AttributeValue> ddbConsentItem) {
        final String statusString = parseStringAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.CONSENT_STATUS);
        return ConsentStatus.fromValue(statusString);
    }

    private static Map<String, String> parseConsentData(final Map<String, AttributeValue> ddbConsentItem) {
        final Map<String, AttributeValue> consentDataMap = ddbConsentItem.get(
            DynamoDbServiceUserConsentAttributeName.CONSENT_DATA.getValue()).m();
        return consentDataMap.entrySet()
            .stream()
            .map(entry -> new AbstractMap.SimpleEntry<String, String>(entry.getKey(), entry.getValue().s()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static OffsetDateTime parseExpiryTime(final Map<String, AttributeValue> ddbConsentItem) {
        final String expiryTimeString = parseStringAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.EXPIRY_TIME);
        return OffsetDateTime.parse(expiryTimeString);
    }
}
