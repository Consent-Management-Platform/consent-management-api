package com.consentframework.consentmanagement.api.infrastructure.repositories;

import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.domain.validators.ConsentValidator;
import com.consentframework.consentmanagement.api.infrastructure.entities.DynamoDbServiceUserConsent;
import com.consentframework.consentmanagement.api.infrastructure.mappers.DynamoDbServiceUserConsentMapper;
import com.consentframework.consentmanagement.api.infrastructure.mappers.DynamoDbServiceUserConsentPageTokenMapper;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.shared.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.shared.api.domain.pagination.ListPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DynamoDB implementation of ServiceUserConsentRepository.
 */
public class DynamoDbServiceUserConsentRepository implements ServiceUserConsentRepository {
    private static final Logger logger = LogManager.getLogger(DynamoDbServiceUserConsentRepository.class);

    static final String CONSENT_EXISTS_CONDITION = "attribute_exists(id)";
    static final String CONSENT_NOT_EXISTS_CONDITION = "attribute_not_exists(id)";
    static final ListPage<Consent> EMPTY_CONSENTS_PAGE = new ListPage<Consent>(List.of(), Optional.empty());

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
     * @throws InternalServiceException exception thrown if unexpected server error creating consent
     */
    @Override
    public void createServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException,
            InternalServiceException {
        ConsentValidator.validate(consent);

        final String consentContext = String.format("creating consent with serviceId: '%s', userId: '%s', consentId: '%s'",
            consent.getServiceId(), consent.getUserId(), consent.getConsentId());
        logger.info(String.format("Submitting CreateServiceUserConsent request for %s", consentContext));
        putConsent(consent, CONSENT_NOT_EXISTS_CONDITION, consentContext);
        logger.info(String.format("Successfully created %s", consentContext));
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
        logger.info(String.format("Submitting GetServiceUserConsent request for %s", consentContext));
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
     * @throws InternalServiceException exception thrown if receive unexpected server-side exception
     * @throws ResourceNotFoundException exception thrown if no such consent exists
     */
    @Override
    public void updateServiceUserConsent(final Consent consent) throws BadRequestException, ConflictingResourceException,
            InternalServiceException, ResourceNotFoundException {
        ConsentValidator.validate(consent);

        final String consentContext = String.format("updating consent with serviceId: '%s', userId: '%s', consentId: '%s'",
            consent.getServiceId(), consent.getUserId(), consent.getConsentId());
        logger.info(String.format("Submitting UpdateServiceUserConsent request for %s", consentContext));
        putConsent(consent, CONSENT_EXISTS_CONDITION, consentContext);
        logger.info(String.format("Successfully updated %s", consentContext));
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
        final QueryEnhancedRequest queryRequest = buildListServiceUserConsentsQueryRequest(serviceId, userId, limit, pageToken);

        final SdkIterable<Page<DynamoDbServiceUserConsent>> queryResults = consentTable
            .index(DynamoDbServiceUserConsent.CONSENT_BY_SERVICE_USER_GSI_NAME)
            .query(queryRequest);

        if (queryResults == null) {
            return EMPTY_CONSENTS_PAGE;
        }
        final Optional<Page<DynamoDbServiceUserConsent>> firstPageResults = queryResults
            .stream()
            .findFirst();
        if (!firstPageResults.isPresent()) {
            return EMPTY_CONSENTS_PAGE;
        }

        final List<Consent> consents = firstPageResults.get()
            .items()
            .stream()
            .map(ddbConsent -> DynamoDbServiceUserConsentMapper.dynamoDbItemToConsent(ddbConsent))
            .collect(Collectors.toList());
        final Map<String, AttributeValue> lastEvaluatedKey = firstPageResults.get().lastEvaluatedKey();
        final String lastEvaluatedKeyString = DynamoDbServiceUserConsentPageTokenMapper.toJsonStringPageToken(lastEvaluatedKey);
        return new ListPage<Consent>(consents, Optional.ofNullable(lastEvaluatedKeyString));
    }

    private void putConsent(final Consent consent, final String conditionExpression, final String consentContext)
            throws ConflictingResourceException, InternalServiceException {
        final PutItemEnhancedRequest<DynamoDbServiceUserConsent> putRequest = buildPutRequest(consent, conditionExpression);
        try {
            consentTable.putItem(putRequest);
        } catch (final ConditionalCheckFailedException conditionFailedException) {
            final String errorMessage = String.format("Error %s, consent already exists", consentContext);
            logger.warn(errorMessage, conditionFailedException);
            throw new ConflictingResourceException(errorMessage);
        } catch (final DynamoDbException ddbException) {
            throw logAndGetNormalizedServiceError(ddbException, consentContext);
        }
    }

    private PutItemEnhancedRequest<DynamoDbServiceUserConsent> buildPutRequest(final Consent consent, final String conditionExpression) {
        final DynamoDbServiceUserConsent ddbConsent = DynamoDbServiceUserConsentMapper.toDynamoDbServiceUserConsent(consent);
        return PutItemEnhancedRequest.builder(DynamoDbServiceUserConsent.class)
            .item(ddbConsent)
            .conditionExpression(Expression.builder().expression(conditionExpression).build())
            .build();
    }

    private GetItemEnhancedRequest buildGetItemRequest(final String serviceId, final String userId, final String consentId) {
        final Key partitionKey = DynamoDbServiceUserConsentMapper.toServiceUserConsentPartitionKey(serviceId, userId, consentId);
        return GetItemEnhancedRequest.builder()
            .key(partitionKey)
            .consistentRead(true)
            .build();
    }

    private QueryEnhancedRequest buildListServiceUserConsentsQueryRequest(final String serviceId, final String userId, final Integer limit,
            final String pageToken) throws BadRequestException {
        final Key queryKey = Key.builder()
            .partitionValue(userId)
            .sortValue(serviceId)
            .build();

        final Map<String, AttributeValue> exclusiveStartKey = DynamoDbServiceUserConsentPageTokenMapper.toDynamoDbPageToken(pageToken);

        return QueryEnhancedRequest.builder()
            .queryConditional(QueryConditional.keyEqualTo(queryKey))
            .exclusiveStartKey(exclusiveStartKey)
            .limit(limit)
            .build();
    }

    private InternalServiceException logAndGetNormalizedServiceError(final DynamoDbException ddbException, final String exceptionContext) {
        final String errorMessage = String.format("Received DynamoDbException %s: %s", exceptionContext, ddbException.getMessage());
        logger.error(errorMessage, ddbException);
        return new InternalServiceException(errorMessage, ddbException);
    }
}
