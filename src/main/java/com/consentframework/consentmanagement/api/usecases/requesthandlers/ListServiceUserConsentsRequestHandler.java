package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.ApiQueryStringParameterName;
import com.consentframework.consentmanagement.api.models.ListServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.usecases.activities.ListServiceUserConsentsActivity;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.shared.api.domain.parsers.ApiQueryStringParameterParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * ListServiceUserConsents API request handler.
 */
public class ListServiceUserConsentsRequestHandler extends ApiRequestHandler {
    private static final Logger logger = LogManager.getLogger(ListServiceUserConsentsRequestHandler.class);

    private ListServiceUserConsentsActivity listConsentsActivity;

    /**
     * Construct ListServiceUserConsents API request handler.
     *
     * @param listConsentsActivity ListServiceUserConsents API activity
     */
    public ListServiceUserConsentsRequestHandler(final ListServiceUserConsentsActivity listConsentsActivity) {
        super(ApiPathParameterName.CONSENTS_PATH_PARAMETERS);
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
        try {
            serviceId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.SERVICE_ID.getValue());
            userId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.USER_ID.getValue());
        } catch (final BadRequestException badRequestException) {
            return logAndBuildMissingPathParamResponse(badRequestException);
        }

        final Integer limit;
        final String pageToken;
        final ListServiceUserConsentResponseContent responseContent;
        final String responseBodyString;
        try {
            limit = ApiQueryStringParameterParser.parseIntQueryStringParameter(request, ApiQueryStringParameterName.LIMIT.getValue());
            pageToken = ApiQueryStringParameterParser.parseStringQueryStringParameter(request,
                ApiQueryStringParameterName.PAGE_TOKEN.getValue());

            logger.info("Retrieving consents for path: " + request.path());
            responseContent = listConsentsActivity.handleRequest(
                serviceId, userId, limit, pageToken);
            responseBodyString = toJsonString(responseContent);
        } catch (final BadRequestException | JsonProcessingException exception) {
            return logAndBuildErrorResponse(exception);
        }

        logger.info(String.format("Successfully retrieved %d consents for path: %s, limit: %d, pageToken: %s",
            responseContent.getData().size(), request.path(), limit, pageToken));
        return buildApiSuccessResponse(responseBodyString);
    }
}
