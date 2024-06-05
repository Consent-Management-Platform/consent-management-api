package com.consentframework.consentmanagement.api.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.exceptions.InvalidConsentDataException;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import org.junit.jupiter.api.Test;

class ConsentValidatorTest {
    @Test
    void testValidateValidConsent() throws InvalidConsentDataException {
        ConsentValidator.validate(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);
    }

    @Test
    void testValidateWhenMissingServiceId() throws InvalidConsentDataException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).serviceId(null);

        final InvalidConsentDataException thrownException = assertThrows(InvalidConsentDataException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.SERVICE_ID_BLANK_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingUserId() throws InvalidConsentDataException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).userId(null);

        final InvalidConsentDataException thrownException = assertThrows(InvalidConsentDataException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.USER_ID_BLANK_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingConsentId() throws InvalidConsentDataException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).consentId(null);

        final InvalidConsentDataException thrownException = assertThrows(InvalidConsentDataException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.CONSENT_ID_BLANK_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingConsentVersion() throws InvalidConsentDataException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).consentVersion(null);

        final InvalidConsentDataException thrownException = assertThrows(InvalidConsentDataException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.CONSENT_VERSION_NULL_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingStatus() throws InvalidConsentDataException {
        final Consent testConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS).status(null);

        final InvalidConsentDataException thrownException = assertThrows(InvalidConsentDataException.class, () ->
            ConsentValidator.validate(testConsent));
        assertEquals(ConsentValidator.STATUS_NULL_MESSAGE, thrownException.getMessage());
    }
}
