package com.consentframework.consentmanagement.api.testcommon.constants;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.ApiQueryStringParameterName;
import com.consentframework.consentmanagement.api.infrastructure.constants.DynamoDbServiceUserConsentAttributeName;
import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Utility class defining common test constants.
 */
public final class TestConstants {
    public static final String TEST_CONSENT_ID = "TestConsentId";
    public static final String TEST_SERVICE_ID = "TestServiceId";
    public static final String TEST_USER_ID = "TestUserId";
    public static final String TEST_PARTITION_KEY = String.format("%s|%s|%s", TEST_SERVICE_ID, TEST_USER_ID, TEST_CONSENT_ID);
    public static final Integer TEST_CONSENT_VERSION = 1;
    public static final ConsentStatus TEST_CONSENT_STATUS = ConsentStatus.ACTIVE;
    public static final String TEST_CONSENT_TYPE = "TestConsentType";
    public static final Integer TEST_PAGE_LIMIT = 2;
    public static final String TEST_PAGE_TOKEN = "1";

    public static final String TEST_CONSENTS_PATH = String.format(
        "/v1/consent-management/services/%s/users/%s/consents",
        TEST_SERVICE_ID, TEST_USER_ID);

    public static final String TEST_CONSENT_PATH = String.format("%s/%s", TEST_CONSENTS_PATH, TEST_CONSENT_ID);

    public static final Map<String, Object> TEST_PAGINATION_QUERY_PARAMETERS = Map.of(
        ApiQueryStringParameterName.LIMIT.getValue(), TEST_PAGE_LIMIT,
        ApiQueryStringParameterName.PAGE_TOKEN.getValue(), TEST_PAGE_TOKEN
    );

    public static final OffsetDateTime TEST_EXPIRY_TIME = OffsetDateTime.ofInstant(
        Instant.now().plus(30, ChronoUnit.DAYS),
        ZoneId.systemDefault());

    public static final Map<String, String> TEST_CONSENT_DATA_MAP = Map.of(
        "TestKey1", "TestValue1",
        "TestKey2", "TestValue2"
    );
    public static final Map<String, AttributeValue> TEST_CONSENT_DATA_ATTRIBUTE_MAP = Map.of(
        "TestKey1", AttributeValue.fromS("TestValue1"),
        "TestKey2", AttributeValue.fromS("TestValue2")
    );

    public static final Consent TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS = new Consent()
        .serviceId(TEST_SERVICE_ID)
        .userId(TEST_USER_ID)
        .consentId(TEST_CONSENT_ID)
        .consentVersion(TEST_CONSENT_VERSION)
        .status(TEST_CONSENT_STATUS);

    public static final Consent TEST_CONSENT_WITH_ALL_FIELDS = new Consent()
        .serviceId(TEST_SERVICE_ID)
        .userId(TEST_USER_ID)
        .consentId(TEST_CONSENT_ID)
        .consentVersion(TEST_CONSENT_VERSION)
        .status(TEST_CONSENT_STATUS)
        .consentData(TEST_CONSENT_DATA_MAP)
        .expiryTime(TEST_EXPIRY_TIME);

    private static final DynamoDbServiceUserConsent.Builder TEST_DDB_CONSENT_BUILDER_WITH_REQUIRED_FIELDS =
        DynamoDbServiceUserConsent.builder()
            .id(TEST_PARTITION_KEY)
            .serviceId(TEST_SERVICE_ID)
            .userId(TEST_USER_ID)
            .consentId(TEST_CONSENT_ID)
            .consentVersion(TEST_CONSENT_VERSION)
            .consentStatus(TEST_CONSENT_STATUS);

    public static final DynamoDbServiceUserConsent TEST_DDB_CONSENT_WITH_ONLY_REQUIRED_FIELDS =
        TEST_DDB_CONSENT_BUILDER_WITH_REQUIRED_FIELDS.build();

    public static final DynamoDbServiceUserConsent TEST_DDB_CONSENT_WITH_ALL_FIELDS = TEST_DDB_CONSENT_BUILDER_WITH_REQUIRED_FIELDS
        .consentData(TEST_CONSENT_DATA_MAP)
        .consentType(TEST_CONSENT_TYPE)
        .expiryTime(TEST_EXPIRY_TIME.toString())
        .build();

    public static final Map<String, AttributeValue> TEST_CONSENT_DDB_ATTRIBUTES = Map.of(
        DynamoDbServiceUserConsentAttributeName.ID.getValue(), AttributeValue.fromS(TEST_PARTITION_KEY),
        DynamoDbServiceUserConsentAttributeName.SERVICE_ID.getValue(), AttributeValue.fromS(TEST_SERVICE_ID),
        DynamoDbServiceUserConsentAttributeName.USER_ID.getValue(), AttributeValue.fromS(TEST_USER_ID),
        DynamoDbServiceUserConsentAttributeName.CONSENT_ID.getValue(), AttributeValue.fromS(TEST_CONSENT_ID),
        DynamoDbServiceUserConsentAttributeName.CONSENT_VERSION.getValue(), AttributeValue.fromN(TEST_CONSENT_VERSION.toString()),
        DynamoDbServiceUserConsentAttributeName.CONSENT_STATUS.getValue(), AttributeValue.fromS(TEST_CONSENT_STATUS.getValue()),
        DynamoDbServiceUserConsentAttributeName.CONSENT_DATA.getValue(), AttributeValue.fromM(TEST_CONSENT_DATA_ATTRIBUTE_MAP),
        DynamoDbServiceUserConsentAttributeName.EXPIRY_TIME.getValue(), AttributeValue.fromS(TEST_EXPIRY_TIME.toString())
    );

    public static final Map<String, String> TEST_CONSENTS_PATH_PARAMS = Map.of(
        ApiPathParameterName.SERVICE_ID.getValue(), TEST_SERVICE_ID,
        ApiPathParameterName.USER_ID.getValue(), TEST_USER_ID
    );

    public static final Map<String, String> TEST_CONSENT_PATH_PARAMS = Map.of(
        ApiPathParameterName.SERVICE_ID.getValue(), TEST_SERVICE_ID,
        ApiPathParameterName.USER_ID.getValue(), TEST_USER_ID,
        ApiPathParameterName.CONSENT_ID.getValue(), TEST_CONSENT_ID
    );

    public static final String CONSENTS_PATH_MISSING_PATH_PARAMS_MESSAGE = "Missing required path parameters, expected serviceId, userId";
    public static final String CONSENT_PATH_MISSING_PATH_PARAMS_MESSAGE =
        "Missing required path parameters, expected serviceId, userId, consentId";
}
