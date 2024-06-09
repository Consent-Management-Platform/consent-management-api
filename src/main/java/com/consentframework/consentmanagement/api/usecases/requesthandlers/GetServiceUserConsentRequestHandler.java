package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import com.consentframework.consentmanagement.api.domain.constants.ApiResponseParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.entities.GetServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.models.GetServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.usecases.activities.GetServiceUserConsentActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * GetServiceUserConsent API request handler.
 */
public class GetServiceUserConsentRequestHandler implements ApiRequestHandler {
    private static final Logger logger = LogManager.getLogger(GetServiceUserConsentRequestHandler.class);

    static final String MISSING_PATH_PARAMETERS_MESSAGE =
        "Missing required GetServiceUserConsent path parameters, expected serviceId, userId, consentId";

    protected GetServiceUserConsentActivity getConsentActivity;

    /**
     * Construct GetServiceUserConsent API request handler.
     *
     * @param getConsentActivity GetServiceUserConsent API activity
     */
    public GetServiceUserConsentRequestHandler(final GetServiceUserConsentActivity getConsentActivity) {
        this.getConsentActivity = getConsentActivity;
    }

    /**
     * Handle GetServiceUserConsent API requests.
     *
     * @param request API request
     * @return API response
     */
    @Override
    public Map<String, Object> handleRequest(final ApiRequest request) {
        final GetServiceUserConsentRequestContent requestContent;
        try {
            requestContent = GetServiceUserConsentRequestContent.parseFromRequest(request);
        } catch (final BadRequestException e) {
            logger.warn(MISSING_PATH_PARAMETERS_MESSAGE);
            return buildApiResponse(HttpStatusCode.BAD_REQUEST, MISSING_PATH_PARAMETERS_MESSAGE);
        }

        logger.info("Retrieving consent for path: " + request.path());
        final GetServiceUserConsentResponseContent responseContent;
        try {
            responseContent = getConsentActivity.getConsent(requestContent);
        } catch (final ResourceNotFoundException resourceNotFoundException) {
            logger.warn(resourceNotFoundException.getMessage());
            return buildApiResponse(HttpStatusCode.NOT_FOUND, resourceNotFoundException.getMessage());
        }

        logger.info("Successfully retrieved consent for path: " + request.path());
        return buildApiResponse(HttpStatusCode.SUCCESS, responseContent);
    }

    /**
     * Build API response to pass from the Lambda function to API Gateway.
     *
     * @param statusCode HTTP status code to return
     * @param body response body
     * @return map of response parameter names and values
     */
    protected Map<String, Object> buildApiResponse(final HttpStatusCode statusCode, final Object body) {
        return Map.of(
            ApiResponseParameterName.STATUS_CODE.getValue(), statusCode.getValue(),
            ApiResponseParameterName.BODY.getValue(), body
        );
    }
}
