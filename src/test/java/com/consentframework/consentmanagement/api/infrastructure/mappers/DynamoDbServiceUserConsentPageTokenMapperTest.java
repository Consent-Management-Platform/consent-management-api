package com.consentframework.consentmanagement.api.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

class DynamoDbServiceUserConsentPageTokenMapperTest {
    private static final Map<String, AttributeValue> ATTRIBUTE_VALUE_MAP_TOKEN = Map.of(
        DynamoDbServiceUserConsent.PARTITION_KEY,
        AttributeValue.fromS(TestConstants.TEST_PARTITION_KEY));

    @Nested
    class ToDynamoDbPageTokenTest {
        @Test
        void testMapNullToken() throws BadRequestException {
            final Map<String, AttributeValue> parsedToken = DynamoDbServiceUserConsentPageTokenMapper.toDynamoDbPageToken(null);
            assertNull(parsedToken);
        }

        @Test
        void testMapInvalidToken() throws BadRequestException {
            final String invalidToken = "NotAJsonString";
            final BadRequestException throwException = assertThrows(BadRequestException.class, () ->
                DynamoDbServiceUserConsentPageTokenMapper.toDynamoDbPageToken(invalidToken));
            final String expectedErrorMessage = String.format(DynamoDbServiceUserConsentPageTokenMapper.INVALID_PAGE_TOKEN_MESSAGE,
                invalidToken);
            assertEquals(expectedErrorMessage, throwException.getMessage());
        }

        @Test
        void testMapValidToken() throws BadRequestException {
            final Map<String, AttributeValue> parsedToken = DynamoDbServiceUserConsentPageTokenMapper.toDynamoDbPageToken(
                TestConstants.TEST_DDB_PAGE_TOKEN);
            assertEquals(ATTRIBUTE_VALUE_MAP_TOKEN, parsedToken);
        }
    }

    @Nested
    class ToJsonStringPageTokenTest {
        @Test
        void testMapNullToken() throws BadRequestException {
            final String parsedToken = DynamoDbServiceUserConsentPageTokenMapper.toJsonStringPageToken(null);
            assertNull(parsedToken);
        }

        @Test
        void testParseInvalidToken() throws BadRequestException {
            final Map<String, AttributeValue> invalidToken = new HashMap<String, AttributeValue>();
            invalidToken.put("SomeKey", null);
            final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                DynamoDbServiceUserConsentPageTokenMapper.toJsonStringPageToken(invalidToken));
            final String expectedMessage = String.format(DynamoDbServiceUserConsentPageTokenMapper.INVALID_PAGE_TOKEN_MESSAGE,
                invalidToken.toString());
            assertEquals(expectedMessage, thrownException.getMessage());
        }

        @Test
        void testMapValidToken() throws BadRequestException {
            final String parsedToken = DynamoDbServiceUserConsentPageTokenMapper.toJsonStringPageToken(ATTRIBUTE_VALUE_MAP_TOKEN);
            assertEquals(TestConstants.TEST_DDB_PAGE_TOKEN, parsedToken);
        }
    }
}
