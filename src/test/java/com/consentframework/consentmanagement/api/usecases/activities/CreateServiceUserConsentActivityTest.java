package com.consentframework.consentmanagement.api.usecases.activities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.domain.validators.CreateServiceUserConsentRequestValidator;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.matchers.CreatedConsentMatcher;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.exceptions.ConflictingResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateServiceUserConsentActivityTest {
    private CreateServiceUserConsentActivity activity;
    private ServiceUserConsentRepository consentRepository;

    @BeforeEach
    void setup() {
        this.consentRepository = spy(new InMemoryServiceUserConsentRepository());
        this.activity = new CreateServiceUserConsentActivity(consentRepository);
    }

    @Test
    void testHandleInvalidRequest() throws BadRequestException, ConflictingResourceException, InternalServiceException {
        final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
            activity.handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                new CreateServiceUserConsentRequestContent()));

        assertEquals(CreateServiceUserConsentRequestValidator.MISSING_REQUIRED_FIELDS_MESSAGE, thrownException.getMessage());

        verify(this.consentRepository, never()).createServiceUserConsent(any(Consent.class));
    }

    @Test
    void testHandleValidRequest() throws BadRequestException, ConflictingResourceException, InternalServiceException {
        final CreateServiceUserConsentRequestContent requestContent = new CreateServiceUserConsentRequestContent()
            .status(ConsentStatus.ACTIVE)
            .consentType(TestConstants.TEST_CONSENT_TYPE)
            .consentData(TestConstants.TEST_CONSENT_DATA_MAP)
            .expiryTime(TestConstants.TEST_EXPIRY_TIME);

        final CreateServiceUserConsentResponseContent response = activity.handleRequest(TestConstants.TEST_SERVICE_ID,
            TestConstants.TEST_USER_ID, requestContent);

        assertNotNull(response.getConsentId());

        final Consent expectedConsent = new Consent()
            .serviceId(TestConstants.TEST_SERVICE_ID)
            .userId(TestConstants.TEST_USER_ID)
            .consentVersion(1)
            .consentType(TestConstants.TEST_CONSENT_TYPE)
            .consentData(requestContent.getConsentData())
            .expiryTime(requestContent.getExpiryTime())
            .status(requestContent.getStatus());

        verify(this.consentRepository).createServiceUserConsent(argThat(new CreatedConsentMatcher(expectedConsent)));
    }
}
