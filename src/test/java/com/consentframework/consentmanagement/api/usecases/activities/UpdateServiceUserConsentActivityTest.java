package com.consentframework.consentmanagement.api.usecases.activities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.domain.validators.ConsentValidator;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import com.consentframework.consentmanagement.api.models.UpdateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateServiceUserConsentActivityTest {
    private UpdateServiceUserConsentActivity activity;
    private ServiceUserConsentRepository consentRepository;

    @BeforeEach
    void setup() {
        this.consentRepository = new InMemoryServiceUserConsentRepository();
        activity = new UpdateServiceUserConsentActivity(this.consentRepository);
    }

    @Test
    void testHandleNullConsentData() {
        final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
            activity.handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID, null));
        assertEquals(UpdateServiceUserConsentActivity.MISSING_CONSENT_DATA_MESSAGE, thrownException.getMessage());
    }

    @Test
    void testHandleRequestForNonExistingConsent() throws BadRequestException, ConflictingResourceException, ResourceNotFoundException {
        final UpdateServiceUserConsentRequestContent requestContent = new UpdateServiceUserConsentRequestContent()
            .consentVersion(1)
            .status(ConsentStatus.REVOKED);

        final ResourceNotFoundException thrownException = assertThrows(ResourceNotFoundException.class, () ->
            activity.handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                TestConstants.TEST_CONSENT_ID, requestContent));

        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertEquals(expectedErrorMessage, thrownException.getMessage());
    }

    @Test
    void testHandleRequestWithConflictingVersion() throws BadRequestException, ConflictingResourceException, InternalServiceException {
        this.consentRepository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS);

        final UpdateServiceUserConsentRequestContent requestContent = new UpdateServiceUserConsentRequestContent()
            .consentVersion(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS.getConsentVersion())
            .status(ConsentStatus.REVOKED);

        final ConflictingResourceException thrownException = assertThrows(ConflictingResourceException.class, () ->
            activity.handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                TestConstants.TEST_CONSENT_ID, requestContent));
        final String expectedMessage = String.format(ConsentValidator.VERSION_CONFLICT_MESSAGE,
            TestConstants.TEST_CONSENT_WITH_ALL_FIELDS.getConsentVersion() + 1, requestContent.getConsentVersion());
        assertEquals(expectedMessage, thrownException.getMessage());
    }

    @Test
    void testHandleValidRequest() throws BadRequestException, ConflictingResourceException,
            InternalServiceException, ResourceNotFoundException {
        this.consentRepository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

        final Integer updatedConsentVersion = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS.getConsentVersion() + 1;
        final UpdateServiceUserConsentRequestContent requestContent = new UpdateServiceUserConsentRequestContent()
            .consentVersion(updatedConsentVersion)
            .consentData(TestConstants.TEST_CONSENT_DATA_MAP)
            .expiryTime(TestConstants.TEST_EXPIRY_TIME)
            .status(ConsentStatus.REVOKED);

        activity.handleRequest(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID, requestContent);

        final Consent storedConsent = this.consentRepository.getServiceUserConsent(TestConstants.TEST_SERVICE_ID,
            TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);

        assertEquals(requestContent.getConsentVersion(), storedConsent.getConsentVersion());
        assertEquals(requestContent.getConsentData(), storedConsent.getConsentData());
        assertEquals(requestContent.getExpiryTime(), storedConsent.getExpiryTime());
        assertEquals(requestContent.getStatus(), storedConsent.getStatus());
    }
}
