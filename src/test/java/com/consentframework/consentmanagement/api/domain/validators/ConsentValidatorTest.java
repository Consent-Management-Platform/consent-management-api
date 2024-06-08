package com.consentframework.consentmanagement.api.domain.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.exceptions.IllegalArgumentException;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import org.junit.jupiter.api.Test;

class ConsentValidatorTest {
    @Test
    void testValidateValidConsent() throws IllegalArgumentException {
        ConsentValidator.validate(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);
    }

    @Test
    void testValidateWhenMissingServiceId() throws IllegalArgumentException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).serviceId(null);

        final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.SERVICE_ID_BLANK_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingUserId() throws IllegalArgumentException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).userId(null);

        final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.USER_ID_BLANK_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingConsentId() throws IllegalArgumentException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).consentId(null);

        final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.CONSENT_ID_BLANK_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingConsentVersion() throws IllegalArgumentException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).consentVersion(null);

        final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.CONSENT_VERSION_NULL_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingStatus() throws IllegalArgumentException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).status(null);

        final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.STATUS_NULL_MESSAGE, thrownException.getMessage());
    }
}
