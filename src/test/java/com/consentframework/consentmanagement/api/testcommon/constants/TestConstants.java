package com.consentframework.consentmanagement.api.testcommon.constants;

import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;

/**
 * Utility class defining common test constants.
 */
public final class TestConstants {
    public static final String TEST_CONSENT_ID = "TestConsentId";
    public static final String TEST_SERVICE_ID = "TestServiceId";
    public static final String TEST_USER_ID = "TestUserId";

    public static final Consent TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS = new Consent()
        .serviceId(TEST_SERVICE_ID)
        .userId(TEST_USER_ID)
        .consentId(TEST_CONSENT_ID)
        .consentVersion(1)
        .status(ConsentStatus.ACTIVE);
}
