package com.consentframework.consentmanagement.api.domain.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.exceptions.IllegalArgumentException;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CreateServiceUserConsentRequestValidatorTest {
    @Test
    void testValidateWhenNullRequestBody() {
        final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
            CreateServiceUserConsentRequestValidator.validate(null));

        assertEquals(CreateServiceUserConsentRequestValidator.MISSING_REQUIRED_FIELDS_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenMissingStatus() {
        final CreateServiceUserConsentRequestContent requestContent = new CreateServiceUserConsentRequestContent()
            .consentData(Map.of());

        final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
            CreateServiceUserConsentRequestValidator.validate(requestContent));

        assertEquals(CreateServiceUserConsentRequestValidator.MISSING_REQUIRED_FIELDS_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testValidateWhenValid() throws IllegalArgumentException {
        final CreateServiceUserConsentRequestContent requestContent = new CreateServiceUserConsentRequestContent()
            .status(ConsentStatus.ACTIVE)
            .consentData(Map.of());
        CreateServiceUserConsentRequestValidator.validate(requestContent);
    }
}
