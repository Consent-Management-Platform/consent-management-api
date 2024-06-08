package com.consentframework.consentmanagement.api.activities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.IllegalArgumentException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.domain.validators.CreateServiceUserConsentRequestValidator;
import com.consentframework.consentmanagement.api.infrastructure.adapters.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.matchers.CreatedConsentMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;

class CreateServiceUserConsentActivityTest {
    private CreateServiceUserConsentActivity activity;
    private ServiceUserConsentRepository consentRepository;

    @BeforeEach
    void setup() {
        this.consentRepository = spy(new InMemoryServiceUserConsentRepository());
        this.activity = new CreateServiceUserConsentActivity(consentRepository);
    }

    @Test
    void testHandleInvalidRequest() throws ConflictingResourceException, IllegalArgumentException {
        final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
            activity.handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                new CreateServiceUserConsentRequestContent()));

        assertEquals(CreateServiceUserConsentRequestValidator.MISSING_REQUIRED_FIELDS_MESSAGE, thrownException.getMessage());

        verify(this.consentRepository, never()).createServiceUserConsent(any(Consent.class));
    }

    @Test
    void testHandleValidRequest() throws ConflictingResourceException, IllegalArgumentException {
        final CreateServiceUserConsentRequestContent requestContent = new CreateServiceUserConsentRequestContent()
            .status(ConsentStatus.ACTIVE)
            .consentData(Map.of("TestKey", "TestValue"))
            .expiryTime(OffsetDateTime.now());

        activity.handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, requestContent);

        final Consent expectedConsent = new Consent()
            .serviceId(TestConstants.TEST_SERVICE_ID)
            .userId(TestConstants.TEST_USER_ID)
            .consentVersion(1)
            .consentData(requestContent.getConsentData())
            .expiryTime(requestContent.getExpiryTime())
            .status(requestContent.getStatus());

        verify(this.consentRepository).createServiceUserConsent(argThat(new CreatedConsentMatcher(expectedConsent)));
    }
}
