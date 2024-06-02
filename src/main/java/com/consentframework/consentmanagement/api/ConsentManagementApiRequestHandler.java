package com.consentframework.consentmanagement.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Entry point for the Consent Management API.
 *
 * TODO: Replace RequesterHandler type parameters with
 * actual request and response types after integrating with
 * the models package.
 */
public class ConsentManagementApiRequestHandler implements RequestHandler<String, String> {
    @Override
    public String handleRequest(final String request, final Context context) {
        final LambdaLogger logger = context.getLogger();
        logger.log("ConsentManagementApiRequestHandler received request: " + request);

        return String.format("Mock response for request %s", request);
    }
}
