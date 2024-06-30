package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import com.consentframework.consentmanagement.api.JSON;
import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.consentmanagement.api.models.UpdateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.usecases.activities.UpdateServiceUserConsentActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * UpdateServiceUserConsent API request handler.
 */
public class UpdateServiceUserConsentRequestHandler extends ApiRequestHandler {
    private static final Logger logger = LogManager.getLogger(UpdateServiceUserConsentRequestHandler.class);

    private UpdateServiceUserConsentActivity activity;

    /**
     * Construct UpdateServiceUserConsent API request handler.
     *
     * @param activity update consent activity
     */
    public UpdateServiceUserConsentRequestHandler(final UpdateServiceUserConsentActivity activity) {
        super(ApiPathParameterName.CONSENT_PATH_PARAMETERS);
        this.activity = activity;
    }

    /**
     * Handle UpdateServiceUserConsent API request.
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

        try {
            final UpdateServiceUserConsentRequestContent updatedContent = new JSON().getMapper()
                .readValue(request.body(), UpdateServiceUserConsentRequestContent.class);
            logger.info("Updating consent for path: " + request.path());
            activity.handleRequest(serviceId, userId, consentId, updatedContent);
        } catch (final JsonProcessingException jsonProcessingException) {
            return logAndBuildJsonProcessingErrorResponse(jsonProcessingException);
        } catch (final BadRequestException | ConflictingResourceException | InternalServiceException
                | ResourceNotFoundException conflictException) {
            return logAndBuildErrorResponse(conflictException);
        }

        logger.info("Successfully updated consent for path: " + request.path());
        return buildApiSuccessResponse(null);
    }
}
