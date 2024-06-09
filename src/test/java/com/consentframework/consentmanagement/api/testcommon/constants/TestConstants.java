package com.consentframework.consentmanagement.api.testcommon.constants;

import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;

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

    public static final String TEST_GET_CONSENT_PATH = String.format(
        "/v1/consent-management/services/%s/users/%s/consents/%s",
        TEST_SERVICE_ID, TEST_USER_ID, TEST_CONSENT_ID);

    public static final OffsetDateTime TEST_EXPIRY_TIME = OffsetDateTime.ofInstant(
        Instant.now().plus(30, ChronoUnit.DAYS),
        ZoneId.systemDefault());

    public static final Map<String, String> TEST_CONSENT_DATA_MAP = Map.of(
        "TestKey1", "TestValue1",
        "TestKey2", "TestValue2"
    );

    public static final Consent TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS = new Consent()
        .serviceId(TEST_SERVICE_ID)
        .userId(TEST_USER_ID)
        .consentId(TEST_CONSENT_ID)
        .consentVersion(1)
        .status(ConsentStatus.ACTIVE);

    public static final Consent TEST_CONSENT_WITH_ALL_FIELDS = new Consent()
        .serviceId(TEST_SERVICE_ID)
        .userId(TEST_USER_ID)
        .consentId(TEST_CONSENT_ID)
        .consentVersion(1)
        .status(ConsentStatus.ACTIVE)
        .consentData(TEST_CONSENT_DATA_MAP)
        .expiryTime(TEST_EXPIRY_TIME);
}
