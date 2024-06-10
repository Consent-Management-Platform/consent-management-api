package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.ApiQueryStringParameterName;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.consentmanagement.api.domain.parsers.ApiQueryStringParameterParser;
import com.consentframework.consentmanagement.api.models.ListServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.usecases.activities.ListServiceUserConsentsActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * ListServiceUserConsents API request handler.
 */
public class ListServiceUserConsentsRequestHandler extends ApiRequestHandler {
    private static final Logger logger = LogManager.getLogger(ListServiceUserConsentsRequestHandler.class);

    static final String MISSING_PATH_PARAMETERS_MESSAGE =
        "Missing required ListServiceUserConsents path parameters, expected serviceId, userId";

    private ListServiceUserConsentsActivity listConsentsActivity;

    /**
     * Construct ListServiceUserConsents API request handler.
     *
     * @param listConsentsActivity ListServiceUserConsents API activity
     */
    public ListServiceUserConsentsRequestHandler(final ListServiceUserConsentsActivity listConsentsActivity) {
        this.listConsentsActivity = listConsentsActivity;
    }

    /**
     * Handle ListServiceUserConsents API requests.
     *
     * @param request API request
     * @return API response
     */
    @Override
    public Map<String, Object> handleRequest(final ApiRequest request) {
        final String serviceId;
        final String userId;
        final Integer limit;
        final String pageToken;
        try {
            serviceId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.SERVICE_ID);
            userId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.USER_ID);
            limit = ApiQueryStringParameterParser.parseIntQueryStringParameter(request, ApiQueryStringParameterName.LIMIT);
            pageToken = ApiQueryStringParameterParser.parseStringQueryStringParameter(request, ApiQueryStringParameterName.PAGE_TOKEN);
        } catch (final BadRequestException badRequestException) {
            return handleInvalidRequestAndBuildErrorResponse(badRequestException);
        }

        logger.info("Retrieving consents for path: " + request.path());
        final ListServiceUserConsentResponseContent responseContent;
        try {
            responseContent = listConsentsActivity.handleRequest(serviceId, userId, limit, pageToken);
        } catch (final BadRequestException badRequestException) {
            return handleInvalidRequestAndBuildErrorResponse(badRequestException);
        }

        logger.info(String.format("Successfully retrieved %d consents for path: %s, limit: %d, pageToken: %s",
            responseContent.getData().size(), request.path(), limit, pageToken));
        return buildApiSuccessResponse(responseContent);
    }
}
