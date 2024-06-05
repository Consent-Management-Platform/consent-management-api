package com.consentframework.consentmanagement.api.infrastructure.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class InMemoryServiceUserConsentRepositoryTest {
    private ServiceUserConsentRepository repository;

    private static final String TEST_CONSENT_ID = "TestConsentId";
    private static final String TEST_SERVICE_ID = "TestServiceId";
    private static final String TEST_USER_ID = "TestUserId";

    private static final Consent TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS = new Consent()
        .serviceId(TEST_SERVICE_ID)
        .userId(TEST_USER_ID)
        .consentId(TEST_CONSENT_ID)
        .consentVersion(new BigDecimal(1))
        .status(ConsentStatus.ACTIVE);

    @BeforeEach
    void setup() {
        repository = new InMemoryServiceUserConsentRepository();
    }

    @Test
    void testGetServiceUserConsentWhenNotExists() {
        final ResourceNotFoundException thrownException = assertThrows(ResourceNotFoundException.class, () ->
            repository.getServiceUserConsent(TEST_SERVICE_ID, TEST_USER_ID, TEST_CONSENT_ID));

        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
            TEST_SERVICE_ID, TEST_USER_ID, TEST_CONSENT_ID);
        assertEquals(expectedErrorMessage, thrownException.getMessage());
    }

    @Test
    void testGetServiceUserConsentWhenExists() throws ConflictingResourceException, ResourceNotFoundException {
        repository.createServiceUserConsent(TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

        final Consent retrievedConsent = repository.getServiceUserConsent(TEST_SERVICE_ID, TEST_USER_ID, TEST_CONSENT_ID);
        assertEquals(TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS, retrievedConsent);
    }

    @Test
    void testCreateServiceUserConsentWhenAlreadyExists() throws ConflictingResourceException {
        repository.createServiceUserConsent(TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

        final ConflictingResourceException thrownException = assertThrows(ConflictingResourceException.class, () ->
            repository.createServiceUserConsent(TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS));

        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONFLICTING_CONSENT_MESSAGE,
            TEST_SERVICE_ID, TEST_USER_ID, TEST_CONSENT_ID);
        assertEquals(expectedErrorMessage, thrownException.getMessage());
    }
}
