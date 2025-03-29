package com.consentframework.consentmanagement.api.usecases.activities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.GetServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetServiceUserConsentActivityTest {
    private GetServiceUserConsentActivity activity;
    private ServiceUserConsentRepository consentRepository;

    @BeforeEach
    void setup() {
        this.consentRepository = new InMemoryServiceUserConsentRepository();
        this.activity = new GetServiceUserConsentActivity(this.consentRepository);
    }

    @Test
    void testRetrieveNonExistingConsent() {
        final ResourceNotFoundException thrownException = assertThrows(ResourceNotFoundException.class, () ->
            activity.handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID));

        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertEquals(expectedErrorMessage, thrownException.getMessage());
    }

    @Test
    void testRetrieveExistingConsent() throws BadRequestException, ConflictingResourceException,
            InternalServiceException, ResourceNotFoundException {
        final Consent createdConsent = TestConstants.TEST_CONSENT_WITH_ALL_FIELDS;
        consentRepository.createServiceUserConsent(createdConsent);

        final GetServiceUserConsentResponseContent response = activity.handleRequest(TestConstants.TEST_SERVICE_ID,
            TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertEquals(createdConsent, response.getData());
    }
}
