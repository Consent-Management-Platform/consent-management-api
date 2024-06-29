package com.consentframework.consentmanagement.api.infrastructure.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.pagination.ListPage;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.domain.validators.ConsentValidator;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class InMemoryServiceUserConsentRepositoryTest {
    private ServiceUserConsentRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryServiceUserConsentRepository();
    }

    @Nested
    class GetServiceUserConsent {
        @Test
        void testGetConsentWhenNotExists() {
            final ResourceNotFoundException thrownException = assertThrows(ResourceNotFoundException.class, () ->
                repository.getServiceUserConsent(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID));

            final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @Test
        void testGetConsentWhenExists() throws BadRequestException, ConflictingResourceException,
                InternalServiceException, ResourceNotFoundException {
            repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

            final Consent retrievedConsent = repository.getServiceUserConsent(
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS, retrievedConsent);
        }
    }

    @Nested
    class CreateServiceUserConsent {
        @Test
        void testCreateConsentWhenAlreadyExists() throws BadRequestException, ConflictingResourceException, InternalServiceException {
            repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

            final ConflictingResourceException thrownException = assertThrows(ConflictingResourceException.class, () ->
                repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS));

            final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_ALREADY_EXISTS_MESSAGE,
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @Test
        void testCreateConsentWhenMissingRequiredFields() throws BadRequestException, ConflictingResourceException {
            final Consent consentMissingRequiredFields = new Consent().consentId(TestConstants.TEST_CONSENT_ID);

            final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                repository.createServiceUserConsent(consentMissingRequiredFields));
            assertEquals(ConsentValidator.SERVICE_ID_BLANK_MESSAGE, thrownException.getMessage());
        }
    }

    @Nested
    class UpdateServiceUserConsent {
        @Test
        void testUpdateWhenDoesNotExist() {
            final ResourceNotFoundException thrownException = assertThrows(ResourceNotFoundException.class, () ->
                repository.updateServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS));

            final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @Test
        void testUpdateWhenVersionConflict() throws BadRequestException, ConflictingResourceException, InternalServiceException {
            repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

            final ConflictingResourceException thrownException = assertThrows(ConflictingResourceException.class, () ->
                repository.updateServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS));
            assertEquals(String.format(ConsentValidator.VERSION_CONFLICT_MESSAGE, 2, 1),
                thrownException.getMessage());
        }

        @Test
        void testUpdateWhenValidNewData() throws BadRequestException, ConflictingResourceException,
                InternalServiceException, ResourceNotFoundException {
            repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

            final Consent inputConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS)
                .consentVersion(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS.getConsentVersion() + 1)
                .consentData(Map.of("TestAttribute", "TestValue"))
                .status(ConsentStatus.REVOKED);

            repository.updateServiceUserConsent(inputConsent);

            final Consent retrievedConsent = repository.getServiceUserConsent(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                TestConstants.TEST_CONSENT_ID);
            assertEquals(inputConsent, retrievedConsent);
        }
    }

    @Nested
    class ListServiceUserConsent {
        @Test
        void testListConsentWhenEmpty() throws BadRequestException {
            final ListPage<Consent> results = repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                null, null);
            assertTrue(results.resultsOnPage().isEmpty());
            assertTrue(results.nextPageToken().isEmpty());
        }

        @Test
        void testListConsentWithLimitAndPageToken() throws BadRequestException, ConflictingResourceException, InternalServiceException {
            final Consent firstConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
            final Consent secondConsent = TestUtils.clone(firstConsent).consentId("TestConsentId2");
            final Consent thirdConsent = TestUtils.clone(firstConsent).consentId("TestConsentId3");
            final Consent fourthConsent = TestUtils.clone(firstConsent).consentId("TestConsentId4");
            final List<Consent> allConsents = List.of(firstConsent, secondConsent, thirdConsent, fourthConsent);

            for (final Consent consent : allConsents) {
                repository.createServiceUserConsent(consent);
            }

            final Integer limit = 2;
            final String pageToken = "1";
            final ListPage<Consent> results = repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                limit, pageToken);
            assertEquals(List.of(secondConsent, thirdConsent), results.resultsOnPage());
            assertEquals(3, results.nextPageToken().getAsInt());
        }

        @Test
        void testListConsentWithInvalidPageToken() {
            final List<String> invalidPageTokens = List.of("", "  ", "InvalidPageToken");

            for (final String invalidPageToken : invalidPageTokens) {
                final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                    repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, null, invalidPageToken));
                assertEquals(String.format(InMemoryServiceUserConsentRepository.INVALID_PAGE_TOKEN_MESSAGE, invalidPageToken),
                    thrownException.getMessage());
            }
        }
    }
}
