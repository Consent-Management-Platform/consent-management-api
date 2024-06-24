package com.consentframework.consentmanagement.api.infrastructure.repositories;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.pagination.ListPage;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.consentmanagement.api.infrastructure.mappers.DynamoDbServiceUserConsentMapper;
import com.consentframework.consentmanagement.api.models.Consent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.Map;

/**
 * DynamoDB implementation of ServiceUserConsentRepository.
 */
public class DynamoDbServiceUserConsentRepository implements ServiceUserConsentRepository {
    private static final Logger logger = LogManager.getLogger(DynamoDbServiceUserConsentRepository.class);

    private final DynamoDbTable<DynamoDbServiceUserConsent> consentTable;

    /**
     * Construct DynamoDB consent repository.
     */
    public DynamoDbServiceUserConsentRepository(final DynamoDbTable<DynamoDbServiceUserConsent> consentTable) {
        this.consentTable = consentTable;
    }

    /**
     * Add consent to DynamoDB table if does not yet exist.
     *
     * @param consent Consent object to save to the repository
     * @throws BadRequestException exception thrown if consent violates model constraints
     * @throws ConflictingResourceException exception thrown if consent already exists with same key
     */
    @Override
    public void createServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException {
        // TODO: implement create operation
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Retrieve consent from DynamoDB if exists, otherwise throw a ResourceNotFoundException.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param consentId consent ID, specific to the service-user pair
     * @return specific consent for the service-user-consent ID tuple if exists
     * @throws InternalServiceException exception thrown if receive error from DynamoDB when retrieving consent
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    @Override
    public Consent getServiceUserConsent(final String serviceId, final String userId, final String consentId)
            throws InternalServiceException, ResourceNotFoundException {
        final String consentContext = String.format("consent with serviceId: '%s', userId: '%s', consentId: '%s'",
            serviceId, userId, consentId);
        logger.info(String.format("Submitting DynamoDB GetItem request for %s", consentContext));
        final GetItemEnhancedRequest getItemRequest = buildGetItemRequest(serviceId, userId, consentId);
        final DynamoDbServiceUserConsent consentItem = getServiceUserConsent(getItemRequest, serviceId, userId, consentId);

        logger.info(String.format("Successfully retrieved %s, converting to Consent data model", consentContext));
        return DynamoDbServiceUserConsentMapper.dynamoDbItemToConsent(consentItem);
    }

    private DynamoDbServiceUserConsent getServiceUserConsent(final GetItemEnhancedRequest getItemRequest, final String serviceId,
            final String userId, final String consentId) throws InternalServiceException, ResourceNotFoundException {
        final DynamoDbServiceUserConsent consentItem;
        try {
            consentItem = consentTable.getItem(getItemRequest);
        } catch (final DynamoDbException ddbException) {
            final String exceptionContext = String.format("retrieving consent with serviceId: '%s', userId: '%s', consentId: '%s'",
                serviceId, userId, consentId);
            throw logAndGetNormalizedServiceError(ddbException, exceptionContext);
        }
        if (consentItem == null) {
            throw new ResourceNotFoundException(String.format(CONSENT_NOT_FOUND_MESSAGE, serviceId, userId, consentId));
        }
        return consentItem;
    }

    /**
     * Update existing consent with new data.
     *
     * @param consent Consent object to save to the repository
     * @throws BadRequestException exception thrown if consent violates model constraints
     * @throws ConflictingResourceException exception thrown if stored consent has conflicting data
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    @Override
    public void updateServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException,
            ResourceNotFoundException {
        // TODO: implement update operation
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * List user's consents for a given service.
     *
     * @param serviceId service identifier
     * @param userId user identifier
     * @param limit maximum number of consents to retrieve
     * @param pageToken pagination token for backend consents query
     * @return page of matching consents stored for the service/user pair
     * @throws BadRequestException exception thrown when receive invalid input
     */
    @Override
    public ListPage<Consent> listServiceUserConsents(final String serviceId, final String userId,
            final Integer limit, final String pageToken) throws BadRequestException {
        // TODO: implement list operation
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private GetItemEnhancedRequest buildGetItemRequest(final String serviceId, final String userId, final String consentId) {
        final Key partitionKey = DynamoDbServiceUserConsentMapper.toServiceUserConsentPartitionKey(serviceId, userId, consentId);
        return GetItemEnhancedRequest.builder()
            .key(partitionKey)
            .consistentRead(true)
            .build();
    }

    private InternalServiceException logAndGetNormalizedServiceError(final DynamoDbException ddbException, final String exceptionContext) {
        final String errorMessage = String.format("Received DynamoDbException %s: %s", exceptionContext, ddbException.getMessage());
        logger.error(errorMessage);
        ddbException.printStackTrace();
        return new InternalServiceException(errorMessage, ddbException);
    }
}
