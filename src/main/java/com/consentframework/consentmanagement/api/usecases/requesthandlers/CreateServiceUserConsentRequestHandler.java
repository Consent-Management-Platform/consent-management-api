package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import com.consentframework.consentmanagement.api.JSON;
import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.usecases.activities.CreateServiceUserConsentActivity;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.shared.api.domain.exceptions.InternalServiceException;
import com.consentframework.shared.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.shared.api.domain.requesthandlers.ApiRequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * CreateServiceUserConsent API request handler.
 */
public class CreateServiceUserConsentRequestHandler extends ApiRequestHandler {
    static final String INVALID_CONSENT_REQUEST_CONTENT_MESSAGE = "Unable to parse CreateServiceUserConsent request content";

    private static final Logger logger = LogManager.getLogger(CreateServiceUserConsentRequestHandler.class);
    private static final ObjectMapper objectMapper = new JSON().getMapper();

    private CreateServiceUserConsentActivity createConsentActivity;

    /**
     * Construct CreateServiceUserConsent API request handler.
     *
     * @param createConsentActivity CreateServiceUserConsent API activity
     */
    public CreateServiceUserConsentRequestHandler(final CreateServiceUserConsentActivity createConsentActivity) {
        super(ApiPathParameterName.CONSENTS_PATH_PARAMETERS);
        this.createConsentActivity = createConsentActivity;
    }

    /**
     * Handle CreateServiceUserConsent API requests.
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

        final String responseContentString;
        try {
            final CreateServiceUserConsentRequestContent requestContent = new JSON().getMapper()
                .readValue(request.body(), CreateServiceUserConsentRequestContent.class);

            logger.info(String.format("Creating consent for serviceId: %s, userId: %s", serviceId, userId));
            final CreateServiceUserConsentResponseContent responseContent = createConsentActivity.handleRequest(
                serviceId, userId, requestContent);
            responseContentString = toJsonString(objectMapper, responseContent);
        } catch (final JsonProcessingException jsonProcessingException) {
            return logAndBuildJsonProcessingErrorResponse(jsonProcessingException);
        } catch (final BadRequestException | ConflictingResourceException | InternalServiceException exception) {
            return logAndBuildErrorResponse(exception);
        }

        logger.info(String.format("Successfully created consent for serviceId: %s, userId: %s", serviceId, userId));
        return buildApiSuccessResponse(responseContentString);
    }
}
