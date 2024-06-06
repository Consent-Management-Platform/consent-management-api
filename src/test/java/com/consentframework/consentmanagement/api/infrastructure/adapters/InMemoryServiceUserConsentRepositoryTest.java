package com.consentframework.consentmanagement.api.infrastructure.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consentframework.consentmanagement.api.domain.entities.ConsentValidator;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.IllegalArgumentException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import com.consentframework.consentmanagement.api.utils.pagination.ListPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        void testGetConsentWhenExists() throws ConflictingResourceException, ResourceNotFoundException, IllegalArgumentException {
            repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

            final Consent retrievedConsent = repository.getServiceUserConsent(
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS, retrievedConsent);
        }
    }

    @Nested
    class CreateServiceUserConsent {
        @Test
        void testCreateConsentWhenAlreadyExists() throws ConflictingResourceException, IllegalArgumentException {
            repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

            final ConflictingResourceException thrownException = assertThrows(ConflictingResourceException.class, () ->
                repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS));

            final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONFLICTING_CONSENT_MESSAGE,
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @Test
        void testCreateConsentWhenMissingRequiredFields() throws ConflictingResourceException, IllegalArgumentException {
            final Consent consentMissingRequiredFields = new Consent().consentId(TestConstants.TEST_CONSENT_ID);

            final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
                repository.createServiceUserConsent(consentMissingRequiredFields));
            assertEquals(ConsentValidator.SERVICE_ID_BLANK_MESSAGE, thrownException.getMessage());
        }
    }

    @Nested
    class ListServiceUserConsent {
        @Test
        void testListConsentWhenEmpty() throws IllegalArgumentException {
            final ListPage<Consent> results = repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                null, null);
            assertTrue(results.resultsOnPage().isEmpty());
            assertTrue(results.nextPageToken().isEmpty());
        }

        @Test
        void testListConsentWithLimitAndPageToken() throws ConflictingResourceException, IllegalArgumentException {
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
                final IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
                    repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, null, invalidPageToken));
                assertEquals(String.format(InMemoryServiceUserConsentRepository.INVALID_PAGE_TOKEN_MESSAGE, invalidPageToken),
                    thrownException.getMessage());
            }
        }
    }
}
