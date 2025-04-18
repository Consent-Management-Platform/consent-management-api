package com.consentframework.consentmanagement.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.consentframework.consentmanagement.api.domain.constants.ApiHttpResource;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.DynamoDbServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.usecases.activities.CreateServiceUserConsentActivity;
import com.consentframework.consentmanagement.api.usecases.activities.GetServiceUserConsentActivity;
import com.consentframework.consentmanagement.api.usecases.activities.ListServiceUserConsentsActivity;
import com.consentframework.consentmanagement.api.usecases.activities.UpdateServiceUserConsentActivity;
import com.consentframework.consentmanagement.api.usecases.requesthandlers.CreateServiceUserConsentRequestHandler;
import com.consentframework.consentmanagement.api.usecases.requesthandlers.GetServiceUserConsentRequestHandler;
import com.consentframework.consentmanagement.api.usecases.requesthandlers.ListServiceUserConsentsRequestHandler;
import com.consentframework.consentmanagement.api.usecases.requesthandlers.UpdateServiceUserConsentRequestHandler;
import com.consentframework.shared.api.domain.constants.ApiResponseParameterName;
import com.consentframework.shared.api.domain.constants.HttpMethod;
import com.consentframework.shared.api.domain.constants.HttpStatusCode;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.requesthandlers.ApiRequestHandler;
import com.consentframework.shared.api.infrastructure.entities.DynamoDbServiceUserConsent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * Entry point for the service, handles requests for an AWS Lambda function.
 */
public class ConsentManagementApiService implements RequestHandler<ApiRequest, Map<String, Object>> {
    private static final Logger logger = LogManager.getLogger(ConsentManagementApiService.class);

    static final String UNSUPPORTED_OPERATION_MESSAGE = "Unsupported resource operation, received resource '%s' and operation '%s'";

    private ServiceUserConsentRepository consentRepository;

    /**
     * Instantiate API service.
     */
    public ConsentManagementApiService() {
        final DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.create();
        consentRepository = ConsentManagementApiService.constructDynamoDbConsentRepository(dynamoDbEnhancedClient);
    }

    /**
     * Instantiate API service with consent repository.
     *
     * @param consentRepository consent repository
     */
    public ConsentManagementApiService(final ServiceUserConsentRepository consentRepository) {
        this.consentRepository = consentRepository;
    }

    /**
     * Route requests to appropriate request handler and return their response.
     *
     * @param request API request
     * @return API response
     */
    @Override
    public Map<String, Object> handleRequest(final ApiRequest request, final Context context) {
        if (request == null) {
            return buildUnsupportedOperationResponse(request);
        }

        logger.info("LambdaRequestHandler received request: " + request.toString());

        if (ApiHttpResource.SERVICE_USER_CONSENTS.getValue().equals(request.resource())) {
            if (HttpMethod.GET.name().equals(request.httpMethod())) {
                final ListServiceUserConsentsActivity activity = new ListServiceUserConsentsActivity(consentRepository);
                return new ListServiceUserConsentsRequestHandler(activity).handleRequest(request);
            }
            if (HttpMethod.POST.name().equals(request.httpMethod())) {
                final CreateServiceUserConsentActivity activity = new CreateServiceUserConsentActivity(consentRepository);
                return new CreateServiceUserConsentRequestHandler(activity).handleRequest(request);
            }
        } else if (ApiHttpResource.SERVICE_USER_CONSENT.getValue().equals(request.resource())) {
            if (HttpMethod.GET.name().equals(request.httpMethod())) {
                final GetServiceUserConsentActivity activity = new GetServiceUserConsentActivity(consentRepository);
                return new GetServiceUserConsentRequestHandler(activity).handleRequest(request);
            }
            if (HttpMethod.POST.name().equals(request.httpMethod())) {
                final UpdateServiceUserConsentActivity activity = new UpdateServiceUserConsentActivity(consentRepository);
                return new UpdateServiceUserConsentRequestHandler(activity).handleRequest(request);
            }
        }

        return buildUnsupportedOperationResponse(request);
    }

    private Map<String, Object> buildUnsupportedOperationResponse(final ApiRequest request) {
        final String requestResource = request == null ? null : request.resource();
        final String requestHttpMethod = request == null ? null : request.httpMethod();
        final String errorMessage = String.format(UNSUPPORTED_OPERATION_MESSAGE, requestResource, requestHttpMethod);
        logger.warn(errorMessage);

        final Map<String, Object> apiErrorResponse = new HashMap<String, Object>();
        apiErrorResponse.put(ApiResponseParameterName.STATUS_CODE.getValue(), HttpStatusCode.BAD_REQUEST.getValue());
        apiErrorResponse.put(ApiResponseParameterName.BODY.getValue(), String.format(ApiRequestHandler.ERROR_RESPONSE_BODY, errorMessage));
        return apiErrorResponse;
    }

    /**
     * Construct a DynamoDbServiceUserConsentRepository instance.
     *
     * @param dynamoDbEnhancedClient DynamoDB enhanced client
     * @return DynamoDB ServiceUserConsent repository
     */
    static DynamoDbServiceUserConsentRepository constructDynamoDbConsentRepository(final DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        final DynamoDbTable<DynamoDbServiceUserConsent> dynamoDbTable = dynamoDbEnhancedClient.table(
            DynamoDbServiceUserConsent.TABLE_NAME, TableSchema.fromImmutableClass(DynamoDbServiceUserConsent.class));
        return new DynamoDbServiceUserConsentRepository(dynamoDbTable);
    }
}
