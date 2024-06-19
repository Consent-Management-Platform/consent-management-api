package com.consentframework.consentmanagement.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Entry point for the service, handles requests for an AWS Lambda function.
 */
public class ConsentManagementApiService implements RequestHandler<ApiRequest, Map<String, Object>> {
    private static final Logger logger = LogManager.getLogger(ConsentManagementApiService.class);

    /**
     * Route requests to appropriate request handler and return their response.
     *
     * TODO: Implement routing to specific API operation handlers.
     *
     * @param request API request
     * @return API response
     */
    @Override
    public Map<String, Object> handleRequest(final ApiRequest request, final Context context) {
        logger.info("LambdaRequestHandler received request: " + request.toString());
        throw new UnsupportedOperationException("Unimplemented method 'handleRequest'");
    }
}
