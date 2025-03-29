package com.consentframework.consentmanagement.api.domain.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.models.ConsentStatus;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

class CreateServiceUserConsentRequestValidatorTest {
    @Test
    void testValidateWhenNullRequestBody() {
        final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
            CreateServiceUserConsentRequestValidator.validate(null));

        assertEquals(CreateServiceUserConsentRequestValidator.MISSING_REQUIRED_FIELDS_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingStatus() {
        final CreateServiceUserConsentRequestContent requestContent = new CreateServiceUserConsentRequestContent()
            .consentData(TestConstants.TEST_CONSENT_DATA_MAP);

        final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
            CreateServiceUserConsentRequestValidator.validate(requestContent));

        assertEquals(CreateServiceUserConsentRequestValidator.MISSING_REQUIRED_FIELDS_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenValid() throws BadRequestException {
        final CreateServiceUserConsentRequestContent requestContent = new CreateServiceUserConsentRequestContent()
            .status(ConsentStatus.ACTIVE)
            .consentData(TestConstants.TEST_CONSENT_DATA_MAP);
        CreateServiceUserConsentRequestValidator.validate(requestContent);
    }
}
