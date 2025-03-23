package com.consentframework.consentmanagement.api.infrastructure.mappers;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Utility methods for mapping between ServiceUserConsent DynamoDB page tokens and JSON string page tokens.
 *
 * The DynamoDB Java client represents pagination tokens as a map from String to AttributeValue objects,
 * and for the Consent Management REST API to provide paginated API methods, we need to convert to JSON strings
 * that can be passed over HTTP.
 */
public final class DynamoDbServiceUserConsentPageTokenMapper {
    private static final Logger logger = LogManager.getLogger(DynamoDbServiceUserConsentPageTokenMapper.class);

    static final String INVALID_PAGE_TOKEN_MESSAGE = "Unable to parse page token %s";

    private DynamoDbServiceUserConsentPageTokenMapper() {}

    /**
     * Convert JSON string page token to an attribute value map that can be passed to DynamoDB.
     *
     * @param pageToken JSON string pagination token
     * @return DynamoDB attribute value map representation of the page token, or null if input is null
     * @throws BadRequestException exception thrown if unable to parse page token
     */
    public static Map<String, AttributeValue> toDynamoDbPageToken(final String pageToken) throws BadRequestException {
        if (pageToken == null) {
            return null;
        }

        try {
            return EnhancedDocument.fromJson(pageToken).toMap();
        } catch (final IllegalArgumentException | UncheckedIOException e) {
            logger.error("Unable to parse String pageToken, expected JSON string but was {}, error: {}", pageToken, e.getMessage(), e);
            throw logAndGetInvalidPageTokenException(pageToken);
        }
    }

    /**
     * Convert DynamoDB attribute value map page token to a JSON string.
     *
     * @param pageToken DynamoDB attribute value map representation of page token
     * @return JSON string representation of page token, or null if input is null
     * @throws BadRequestException exception thrown if unable to parse page token
     */
    public static String toJsonStringPageToken(final Map<String, AttributeValue> pageToken) throws BadRequestException {
        if (pageToken == null) {
            return null;
        }

        try {
            return EnhancedDocument.fromAttributeValueMap(pageToken).toJson();
        } catch (final IllegalArgumentException | NullPointerException | UncheckedIOException e) {
            logger.error("Unable to parse Map<String, AttributeValue> pageToken {}, error: {}", pageToken.toString(), e.getMessage(), e);
            throw logAndGetInvalidPageTokenException(pageToken.toString());
        }
    }

    private static BadRequestException logAndGetInvalidPageTokenException(final String pageToken) {
        final String errorMessage = String.format(INVALID_PAGE_TOKEN_MESSAGE, pageToken);
        logger.error(errorMessage);
        return new BadRequestException(errorMessage);
    }
}
