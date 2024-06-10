package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.consentmanagement.api.models.UpdateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.usecases.activities.UpdateServiceUserConsentActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
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
            return handleMissingPathParamsAndBuildErrorResponse(badRequestException);
        }

        // TODO: extract from request
        final UpdateServiceUserConsentRequestContent updatedContent = null;

        logger.info("Updating consent for path: " + request.path());
        try {
            activity.handleRequest(serviceId, userId, consentId, updatedContent);
        } catch (final BadRequestException | ConflictingResourceException | ResourceNotFoundException exception) {
            logger.warn(exception.getMessage());
            return buildApiErrorResponse(exception);
        }

        logger.info("Successfully updated consent for path: " + request.path());
        return buildApiSuccessResponse(null);
    }

}
