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
import com.consentframework.consentmanagement.api.domain.pagination.ListPage;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.domain.validators.ConsentValidator;
import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class DynamoDbServiceUserConsentRepositoryTest {
    private DynamoDbTable<DynamoDbServiceUserConsent> consentTable;
    private DynamoDbServiceUserConsentRepository repository;

    private static final String TEST_CONDITIONAL_CHECK_FAILED_EXCEPTION_MESSAGE = "TestConditionalCheckFailedException";
    private static final ConditionalCheckFailedException CONDITION_FAILED_EXCEPTION = ConditionalCheckFailedException.builder()
        .awsErrorDetails(AwsErrorDetails.builder().errorMessage(TEST_CONDITIONAL_CHECK_FAILED_EXCEPTION_MESSAGE).build())
        .build();

    private static final String TEST_DYNAMODB_EXCEPTION_MESSAGE = "TestDynamoDBError";
    private static final AwsServiceException DYNAMODB_EXCEPTION = DynamoDbException.builder()
        .message(TEST_DYNAMODB_EXCEPTION_MESSAGE)
        .build();

    @Mock
    private PageIterable<DynamoDbServiceUserConsent> mockQueryResults;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() {
        consentTable = (DynamoDbTable<DynamoDbServiceUserConsent>) mock(DynamoDbTable.class);
        repository = new DynamoDbServiceUserConsentRepository(consentTable);
        MockitoAnnotations.openMocks(this);
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
            final Consent incompleteConsent = buildIncompleteConsent();
            final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                repository.createServiceUserConsent(incompleteConsent));
            assertEquals(ConsentValidator.STATUS_NULL_MESSAGE, thrownException.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void testCreateWhenConditionalCheckFailedException() {
            doThrow(CONDITION_FAILED_EXCEPTION).when(consentTable).putItem(any(PutItemEnhancedRequest.class));
            final ConflictingResourceException thrownException = assertThrows(ConflictingResourceException.class, () ->
                repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS));

            final String expectedExceptionMessage = String.format(
                "Error creating consent with serviceId: '%s', userId: '%s', consentId: '%s', consent already exists",
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(expectedExceptionMessage, thrownException.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void testCreateWhenUnexpectedDynamoDbException() {
            doThrow(DYNAMODB_EXCEPTION).when(consentTable).putItem(any(PutItemEnhancedRequest.class));
            final InternalServiceException thrownException = assertThrows(InternalServiceException.class, () ->
                repository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS));

            final String expectedErrorMessage = String.format(
                "Received DynamoDbException creating consent with serviceId: '%s', userId: '%s', consentId: '%s': %s",
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID, TEST_DYNAMODB_EXCEPTION_MESSAGE);
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
        void testUpdateWithNullConsent() {
            final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                repository.updateServiceUserConsent(null));
            assertEquals(ConsentValidator.CONSENT_NULL_MESSAGE, thrownException.getMessage());
        }

        @Test
        void testUpdateWithInvalidConsent() {
            final Consent incompleteConsent = buildIncompleteConsent();
            final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                repository.updateServiceUserConsent(incompleteConsent));
            assertEquals(ConsentValidator.STATUS_NULL_MESSAGE, thrownException.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void testUpdateWhenConditionalCheckFailedException() {
            doThrow(CONDITION_FAILED_EXCEPTION).when(consentTable).putItem(any(PutItemEnhancedRequest.class));
            final ConflictingResourceException thrownException = assertThrows(ConflictingResourceException.class, () ->
                repository.updateServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS));

            final String expectedExceptionMessage = String.format(
                "Error updating consent with serviceId: '%s', userId: '%s', consentId: '%s', consent already exists",
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
            assertEquals(expectedExceptionMessage, thrownException.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void testUpdateWhenUnexpectedDynamoDbException() {
            doThrow(DYNAMODB_EXCEPTION).when(consentTable).putItem(any(PutItemEnhancedRequest.class));
            final InternalServiceException thrownException = assertThrows(InternalServiceException.class, () ->
                repository.updateServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS));

            final String expectedErrorMessage = String.format(
                "Received DynamoDbException updating consent with serviceId: '%s', userId: '%s', consentId: '%s': %s",
                TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID, TEST_DYNAMODB_EXCEPTION_MESSAGE);
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void testUpdateConsentWhenDynamoDbPutItemSucceeds() throws BadRequestException, ConflictingResourceException,
                InternalServiceException, ResourceNotFoundException {
            doNothing().when(consentTable).putItem(any(PutItemEnhancedRequest.class));
            repository.updateServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS);
        }
    }

    @Nested
    class ListServiceUserConsentsTest {
        @Test
        void testListConsentWithoutOptionalParametersWhenNullResults() throws BadRequestException {
            final ListPage<Consent> queryResults = repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID,
                TestConstants.TEST_USER_ID, null, null);
            assertEquals(DynamoDbServiceUserConsentRepository.EMPTY_CONSENTS_PAGE, queryResults);
        }

        @Test
        void testListConsentWithOptionalParametersWhenNullResults() throws BadRequestException {
            final ListPage<Consent> queryResults = repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID,
                TestConstants.TEST_USER_ID, 10, TestConstants.TEST_DDB_PAGE_TOKEN);
            assertEquals(DynamoDbServiceUserConsentRepository.EMPTY_CONSENTS_PAGE, queryResults);
        }

        @Test
        void testListConsentWhenEmptyResults() throws BadRequestException {
            when(mockQueryResults.stream()).thenReturn(Stream.empty());
            when(consentTable.query(any(QueryEnhancedRequest.class))).thenReturn(mockQueryResults);

            final ListPage<Consent> queryResults = repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID,
                TestConstants.TEST_USER_ID, 10, TestConstants.TEST_DDB_PAGE_TOKEN);
            assertEquals(DynamoDbServiceUserConsentRepository.EMPTY_CONSENTS_PAGE, queryResults);
        }

        @Test
        void testListConsentWhenMultiplePages() throws BadRequestException {
            final List<DynamoDbServiceUserConsent> mockConsents = List.of(TestConstants.TEST_DDB_CONSENT_WITH_ALL_FIELDS);
            final Page<DynamoDbServiceUserConsent> mockPageConsents = Page.builder(DynamoDbServiceUserConsent.class)
                .items(mockConsents)
                .lastEvaluatedKey(TestConstants.TEST_DDB_PAGE_TOKEN_ATTRIBUTE_MAP)
                .build();
            when(mockQueryResults.stream()).thenReturn(List.of(mockPageConsents).stream());
            when(consentTable.query(any(QueryEnhancedRequest.class))).thenReturn(mockQueryResults);

            final ListPage<Consent> queryResults = repository.listServiceUserConsents(TestConstants.TEST_SERVICE_ID,
                TestConstants.TEST_USER_ID, 10, TestConstants.TEST_DDB_PAGE_TOKEN);
            assertEquals(List.of(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS), queryResults.resultsOnPage());
            assertEquals(Optional.of(TestConstants.TEST_DDB_PAGE_TOKEN), queryResults.nextPageToken());
        }
    }

    private Consent buildIncompleteConsent() {
        return new Consent()
            .serviceId(TestConstants.TEST_SERVICE_ID)
            .userId(TestConstants.TEST_USER_ID)
            .consentId(TestConstants.TEST_CONSENT_ID)
            .consentVersion(1);
    }
}
