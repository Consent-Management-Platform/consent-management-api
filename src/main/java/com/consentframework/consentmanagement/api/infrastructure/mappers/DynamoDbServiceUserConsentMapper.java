package com.consentframework.consentmanagement.api.infrastructure.mappers;

import com.consentframework.consentmanagement.api.infrastructure.constants.DynamoDbServiceUserConsentAttributeName;
import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import software.amazon.awssdk.enhanced.dynamodb.Key;
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
     * @param ddbConsentItem DynamoDB consent data model
     * @return normalized Consent data model
     */
    public static Consent dynamoDbItemToConsent(final DynamoDbServiceUserConsent ddbConsentItem) {
        if (ddbConsentItem == null) {
            return null;
        }

        final Consent consent = new Consent()
            .serviceId(ddbConsentItem.serviceId())
            .userId(ddbConsentItem.userId())
            .consentId(ddbConsentItem.consentId())
            .consentVersion(ddbConsentItem.consentVersion())
            .status(ddbConsentItem.consentStatus())
            .consentType(ddbConsentItem.consentType())
            .consentData(ddbConsentItem.consentData());

        if (ddbConsentItem.expiryTime() != null) {
            consent.expiryTime(OffsetDateTime.parse(ddbConsentItem.expiryTime()));
        }

        return consent;
    }

    /**
     * Convert Consent object to DynamoDbServiceUserConsent.
     *
     * @param consent normalized consent model
     * @return DynamoDB consent data model
     */
    public static DynamoDbServiceUserConsent toDynamoDbServiceUserConsent(final Consent consent) {
        if (consent == null) {
            return null;
        }

        final DynamoDbServiceUserConsent.Builder dbConsentBuilder = DynamoDbServiceUserConsent.builder()
            .id(toDynamoDbId(consent.getServiceId(), consent.getUserId(), consent.getConsentId()))
            .serviceId(consent.getServiceId())
            .userId(consent.getUserId())
            .consentId(consent.getConsentId())
            .consentVersion(consent.getConsentVersion())
            .consentStatus(consent.getStatus())
            .consentType(consent.getConsentType())
            .consentData(consent.getConsentData());

        if (consent.getExpiryTime() != null) {
            dbConsentBuilder.expiryTime(consent.getExpiryTime().toString());
        }

        return dbConsentBuilder.build();
    }

    /**
     * Convert ServiceUserConsent DynamoDB attribute value map to Consent object.
     *
     * @return normalized Consent data model
     */
    public static Consent dynamoDbAttributeMapToConsent(final Map<String, AttributeValue> ddbConsentItem) {
        if (ddbConsentItem == null) {
            return null;
        }

        return new Consent()
            .serviceId(parseStringAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.SERVICE_ID))
            .userId(parseStringAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.USER_ID))
            .consentId(parseStringAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.CONSENT_ID))
            .consentVersion(parseIntegerAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.CONSENT_VERSION))
            .status(parseConsentStatus(ddbConsentItem))
            .consentType(parseStringAttribute(ddbConsentItem, DynamoDbServiceUserConsentAttributeName.CONSENT_TYPE))
            .consentData(parseConsentData(ddbConsentItem))
            .expiryTime(parseExpiryTime(ddbConsentItem));
    }

    /**
     * Build ServiceUserConsent DynamoDB table partition key value.
     *
     * @param serviceId service ID
     * @param userId user ID
     * @param consentId consent ID
     * @return consent partition key value
     */
    public static Key toServiceUserConsentPartitionKey(final String serviceId, final String userId, final String consentId) {
        return Key.builder()
            .partitionValue(toDynamoDbId(serviceId, userId, consentId))
            .build();
    }

    /**
     * Build ServiceUserConsent DynamoDB id value.
     *
     * @param serviceId service ID
     * @param userId user ID
     * @param consentId consent ID
     * @return combined DynamoDB item id
     */
    public static String toDynamoDbId(final String serviceId, final String userId, final String consentId) {
        return String.format("%s|%s|%s", serviceId, userId, consentId);
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
