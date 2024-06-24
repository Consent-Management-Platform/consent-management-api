package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.consentmanagement.api.models.GetServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.usecases.activities.GetServiceUserConsentActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * GetServiceUserConsent API request handler.
 */
public class GetServiceUserConsentRequestHandler extends ApiRequestHandler {
    private static final Logger logger = LogManager.getLogger(GetServiceUserConsentRequestHandler.class);

    private GetServiceUserConsentActivity getConsentActivity;

    /**
     * Construct GetServiceUserConsent API request handler.
     *
     * @param getConsentActivity GetServiceUserConsent API activity
     */
    public GetServiceUserConsentRequestHandler(final GetServiceUserConsentActivity getConsentActivity) {
        super(ApiPathParameterName.CONSENT_PATH_PARAMETERS);
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
        final String serviceId;
        final String userId;
        final String consentId;
        try {
            serviceId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.SERVICE_ID);
            userId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.USER_ID);
            consentId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.CONSENT_ID);
        } catch (final BadRequestException badRequestException) {
            return logAndBuildMissingPathParamResponse(badRequestException);
        }

        logger.info("Retrieving consent for path: " + request.path());
        final String responseBodyString;
        try {
            final GetServiceUserConsentResponseContent responseContent = getConsentActivity.handleRequest(serviceId, userId, consentId);
            responseBodyString = toJsonString(responseContent);
        } catch (final InternalServiceException | JsonProcessingException | ResourceNotFoundException exception) {
            return logAndBuildErrorResponse(exception);
        }

        logger.info("Successfully retrieved consent for path: " + request.path());
        return buildApiSuccessResponse(responseBodyString);
    }
}
