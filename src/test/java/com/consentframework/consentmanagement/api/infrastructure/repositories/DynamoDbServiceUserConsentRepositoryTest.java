package com.consentframework.consentmanagement.api.infrastructure.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

class DynamoDbServiceUserConsentRepositoryTest {
    private DynamoDbTable<DynamoDbServiceUserConsent> consentTable;
    private DynamoDbServiceUserConsentRepository repository;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        consentTable = (DynamoDbTable<DynamoDbServiceUserConsent>) mock(DynamoDbTable.class);
        repository = new DynamoDbServiceUserConsentRepository(consentTable);
    }

    @Nested
    class GetServiceUserConsentTest {
        @Test
        void testGetConsentWhenDynamoDbThrowsException() {
            final String originalErrorMessage = "TestDynamoDBError";
            final AwsServiceException testException = DynamoDbException.builder().message(originalErrorMessage).build();
            doThrow(testException).when(consentTable).getItem(any(GetItemEnhancedRequest.class));

            final InternalServiceException thrownException = assertThrows(InternalServiceException.class, () ->
                repository.getServiceUserConsent(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID));

            final String expectedErrorMessage = String.format(
                "Received DynamoDbException retrieving consent with serviceId: '%s', userId: '%s', consentId: '%s': %s",
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID, originalErrorMessage);
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @Test
        void testGetConsentWhenDoesNotExist() {
            when(consentTable.getItem(any(GetItemEnhancedRequest.class))).thenReturn(null);

            final ResourceNotFoundException thrownException = assertThrows(ResourceNotFoundException.class, () ->
                repository.getServiceUserConsent(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID));

            final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @Test
        void testGetConsentWhenExists() throws InternalServiceException, ResourceNotFoundException {
            when(consentTable.getItem(any(GetItemEnhancedRequest.class))).thenReturn(TestConstants.TEST_DDB_CONSENT_WITH_ALL_FIELDS);

            final Consent returnedConsent = repository.getServiceUserConsent(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
                TestConstants.TEST_CONSENT_ID);
            assertEquals(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS, returnedConsent);
        }
    }

    @Nested
    class CreateServiceUserConsentTest {
        @Test
        void testCreateConsent() {
            assertThrows(UnsupportedOperationException.class, () -> repository.createServiceUserConsent(null));
        }
    }

    @Nested
    class UpdateServiceUserConsentTest {
        @Test
        void testUpdateConsent() {
            assertThrows(UnsupportedOperationException.class, () -> repository.updateServiceUserConsent(null));
        }
    }

    @Nested
    class ListServiceUserConsentsTest {
        @Test
        void testUpdateConsent() {
            assertThrows(UnsupportedOperationException.class, () ->
                repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, null, null));
        }
    }
}
