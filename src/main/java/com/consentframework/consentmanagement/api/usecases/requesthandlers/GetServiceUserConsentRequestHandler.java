package com.consentframework.consentmanagement.api.usecases.requesthandlers;

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
public class GetServiceUserConsentRequestHandler extends ApiRequestHandler {
    private static final Logger logger = LogManager.getLogger(GetServiceUserConsentRequestHandler.class);

    static final String MISSING_PATH_PARAMETERS_MESSAGE =
        "Missing required GetServiceUserConsent path parameters, expected serviceId, userId, consentId";

    private GetServiceUserConsentActivity getConsentActivity;

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
        } catch (final BadRequestException badRequestException) {
            logger.warn(MISSING_PATH_PARAMETERS_MESSAGE);
            return buildApiErrorResponse(new BadRequestException(MISSING_PATH_PARAMETERS_MESSAGE));
        }

        logger.info("Retrieving consent for path: " + request.path());
        final GetServiceUserConsentResponseContent responseContent;
        try {
            responseContent = getConsentActivity.handleRequest(requestContent);
        } catch (final ResourceNotFoundException resourceNotFoundException) {
            logger.warn(resourceNotFoundException.getMessage());
            return buildApiErrorResponse(resourceNotFoundException);
        }

        logger.info("Successfully retrieved consent for path: " + request.path());
        return buildApiSuccessResponse(responseContent);
    }
}
