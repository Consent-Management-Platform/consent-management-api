package com.consentframework.consentmanagement.api.infrastructure.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.entities.ConsentValidator;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InvalidConsentDataException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryServiceUserConsentRepositoryTest {
    private ServiceUserConsentRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryServiceUserConsentRepository();
    }

    @Test
    void testGetServiceUserConsentWhenNotExists() {
        final ResourceNotFoundException thrownException = assertThrows(ResourceNotFoundException.class, () ->
            repository.getServiceUserConsent(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID));

        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertEquals(expectedErrorMessage, thrownException.getMessage());
    }

    @Test
    void testGetServiceUserConsentWhenExists() throws ConflictingResourceException, ResourceNotFoundException, InvalidConsentDataException {
        repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

        final Consent retrievedConsent = repository.getServiceUserConsent(
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertEquals(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS, retrievedConsent);
    }

    @Test
    void testCreateServiceUserConsentWhenAlreadyExists() throws ConflictingResourceException, InvalidConsentDataException {
        repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

        final ConflictingResourceException thrownException = assertThrows(ConflictingResourceException.class, () ->
            repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS));

        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONFLICTING_CONSENT_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertEquals(expectedErrorMessage, thrownException.getMessage());
    }

    @Test
    void testCreateServiceUserConsentWhenMissingRequiredFields() throws ConflictingResourceException, InvalidConsentDataException {
        final Consent consentMissingRequiredFields = new Consent().consentId(TestConstants.TEST_CONSENT_ID);

        final InvalidConsentDataException thrownException = assertThrows(InvalidConsentDataException.class, () ->
            repository.createServiceUserConsent(consentMissingRequiredFields));
        assertEquals(ConsentValidator.SERVICE_ID_BLANK_MESSAGE, thrownException.getMessage());
    }
}
