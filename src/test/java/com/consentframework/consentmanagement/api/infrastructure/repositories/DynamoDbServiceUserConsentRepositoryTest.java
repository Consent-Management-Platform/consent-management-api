package com.consentframework.consentmanagement.api.infrastructure.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.domain.validators.ConsentValidator;
import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
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
        void testCreateNullConsent() {
            final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                repository.createServiceUserConsent(null));
            assertEquals(ConsentValidator.CONSENT_NULL_MESSAGE, thrownException.getMessage());
        }

        @Test
        void testCreateInvalidConsent() {
            final Consent incompleteConsent = new Consent()
                .serviceId(TestConstants.TEST_SERVICE_ID)
                .userId(TestConstants.TEST_USER_ID)
                .consentId(TestConstants.TEST_CONSENT_ID)
                .consentVersion(1);

            final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                repository.createServiceUserConsent(incompleteConsent));
            assertEquals(ConsentValidator.STATUS_NULL_MESSAGE, thrownException.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void testCreateWhenConditionalCheckFailedException() {
            final String originalExceptionMessage = "TestConditionalCheckFailedException";
            final ConditionalCheckFailedException conditionFailedException = ConditionalCheckFailedException.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorMessage(originalExceptionMessage).build())
                .build();
            doThrow(conditionFailedException).when(consentTable).putItem(any(PutItemEnhancedRequest.class));

            final ConflictingResourceException thrownException = assertThrows(ConflictingResourceException.class, () ->
                repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS));

            final String expectedExceptionMessage = String.format(
                "Failed to create consent, consent with serviceId: '%s', userId: '%s', consentId: '%s' already exists",
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(expectedExceptionMessage, thrownException.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void testCreateWhenUnexpectedDynamoDbException() {
            final String originalErrorMessage = "TestDynamoDBError";
            final AwsServiceException testException = DynamoDbException.builder().message(originalErrorMessage).build();
            doThrow(testException).when(consentTable).putItem(any(PutItemEnhancedRequest.class));

            final InternalServiceException thrownException = assertThrows(InternalServiceException.class, () ->
                repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS));

            final String expectedErrorMessage = String.format(
                "Received DynamoDbException creating consent with serviceId: '%s', userId: '%s', consentId: '%s': %s",
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID, originalErrorMessage);
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void testCreateConsentWhenDynamoDbPutItemSucceeds() throws BadRequestException, ConflictingResourceException,
                InternalServiceException {
            doNothing().when(consentTable).putItem(any(PutItemEnhancedRequest.class));
            repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS);
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
