package com.consentframework.consentmanagement.api.infrastructure.requesthandlers.apigatewaylambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.consentframework.consentmanagement.api.domain.constants.ApiResponseParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.requesthandlers.ApiRequestHandler;

import java.util.Map;

/**
 * Request handler for an AWS API Gateway backed Lambda function.
 */
abstract class LambdaRequestHandler implements ApiRequestHandler<Context>, RequestHandler<ApiRequest, Map<String, Object>> {
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
