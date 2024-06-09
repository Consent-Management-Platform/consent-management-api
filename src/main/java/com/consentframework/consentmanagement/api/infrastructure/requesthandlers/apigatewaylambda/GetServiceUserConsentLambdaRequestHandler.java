package com.consentframework.consentmanagement.api.infrastructure.requesthandlers.apigatewaylambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.consentframework.consentmanagement.api.activities.GetServiceUserConsentActivity;
import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.consentmanagement.api.models.GetServiceUserConsentResponseContent;

import java.util.Map;

/**
 * GetServiceUserConsent API request handler for an AWS API Gateway backed Lambda.
 */
public class GetServiceUserConsentLambdaRequestHandler extends LambdaRequestHandler {
    static final String MISSING_PATH_PARAMETERS_MESSAGE =
        "Missing required GetServiceUserConsent path parameters, expected serviceId, userId, consentId";

    private GetServiceUserConsentActivity getConsentActivity;

    public GetServiceUserConsentLambdaRequestHandler(final GetServiceUserConsentActivity getConsentActivity) {
        this.getConsentActivity = getConsentActivity;
    }

    /**
     * Handle GetServiceUserConsent API requests.
     *
     * @param request API request
     * @param context Lambda function context
     */
    @Override
    public Map<String, Object> handleRequest(final ApiRequest request, final Context context) {
        final LambdaLogger logger = context.getLogger();

        final String serviceId;
        final String userId;
        final String consentId;
        try {
            serviceId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.SERVICE_ID);
            userId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.USER_ID);
            consentId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.CONSENT_ID);
        } catch (final BadRequestException e) {
            logger.log(MISSING_PATH_PARAMETERS_MESSAGE, LogLevel.WARN);
            return buildApiResponse(HttpStatusCode.BAD_REQUEST, MISSING_PATH_PARAMETERS_MESSAGE);
        }

        logger.log("Retrieving consent for path: " + request.path());
        final GetServiceUserConsentResponseContent responseContent;
        try {
            responseContent = getConsentActivity.handleRequest(serviceId, userId, consentId);
        } catch (final ResourceNotFoundException resourceNotFoundException) {
            logger.log(resourceNotFoundException.getMessage(), LogLevel.WARN);
            return buildApiResponse(HttpStatusCode.NOT_FOUND, resourceNotFoundException.getMessage());
        }

        logger.log("Successfully retrieved consent for path: " + request.path());
        return buildApiResponse(HttpStatusCode.SUCCESS, responseContent);
    }
}
