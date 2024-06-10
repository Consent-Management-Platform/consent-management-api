package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.ApiResponseParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiExceptionResponseContent;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract class for an API request handler.
 */
abstract class ApiRequestHandler {
    static final String MISSING_PATH_PARAMETERS_MESSAGE = "Missing required path parameters, expected %s";
    static final String REQUEST_PARSE_FAILURE_MESSAGE = "Unable to parse request";

    private static final Logger logger = LogManager.getLogger(ApiRequestHandler.class);

    public final List<ApiPathParameterName> requiredPathParameters;

    /**
     * Construct ApiRequestHandler with properties shared across subclasses.
     *
     * @param requiredPathParameters required path parameters
     */
    public ApiRequestHandler(final List<ApiPathParameterName> requiredPathParameters) {
        this.requiredPathParameters = requiredPathParameters;
    }

    /**
     * Handle API request.
     *
     * @param request API request object
     * @return API response
     */
    abstract Map<String, Object> handleRequest(final ApiRequest request);

    /**
     * Handle missing path parameter exception and return API error response.
     *
     * @param exception original bad request exception
     * @return 400 Bad Request API error response including list of required parameters
     */
    protected Map<String, Object> handleMissingPathParamsAndBuildErrorResponse(final BadRequestException exception) {
        final List<String> requiredPathParameterNames = requiredPathParameters.stream()
            .map(pathParam -> pathParam.getValue())
            .collect(Collectors.toList());
        final String errorMessage = String.format(MISSING_PATH_PARAMETERS_MESSAGE,
            String.join(", ", requiredPathParameterNames));

        logger.warn(errorMessage, exception);
        return buildApiErrorResponse(new BadRequestException(errorMessage, exception));
    }

    /**
     * Handle invalid request exception and return API error response.
     *
     * @param exception exception thrown while parsing request
     * @return 400 Bad Request API error response
     */
    protected Map<String, Object> handleInvalidRequestAndBuildErrorResponse(final Exception exception) {
        logger.warn(String.format("%s received unexpected %s parsing request: %s",
            this.getClass().getName(), exception.getClass(), exception.getMessage()));
        // Print stack trace for non-standard invalid request exception cases
        if (!(exception instanceof BadRequestException)) {
            exception.printStackTrace();
        }

        final BadRequestException externalException = buildExternalBadRequestException(exception);
        return buildApiErrorResponse(externalException);
    }

    /**
     * Handle conflicting resource exception and return API error response.
     *
     * @param conflictException exception thrown saving consent
     * @return 409 Conflict API error response
     */
    protected Map<String, Object> handleConflictAndBuildErrorResponse(final ConflictingResourceException conflictException) {
        logger.warn(conflictException.getMessage());
        return buildApiErrorResponse(conflictException);
    }

    /**
     * Build API success response.
     *
     * @param responseBody API response body
     * @return 200 Success API response
     */
    protected Map<String, Object> buildApiSuccessResponse(final Object responseBody) {
        return buildApiResponse(HttpStatusCode.SUCCESS, responseBody);
    }

    /**
     * Build API error response with appropriate status code and message body.
     *
     * @param exception original thrown exception
     * @return API error response
     */
    protected Map<String, Object> buildApiErrorResponse(final Exception exception) {
        final HttpStatusCode statusCode = determineStatusCode(exception);
        final ApiExceptionResponseContent exceptionContent = new ApiExceptionResponseContent(exception.getMessage());

        return buildApiResponse(statusCode, exceptionContent);
    }

    /**
     * Build API response with given status code and response body.
     *
     * @param statusCode HTTP status code to return
     * @param body response body
     * @return map of response parameter names and values
     */
    private Map<String, Object> buildApiResponse(final HttpStatusCode statusCode, final Object body) {
        return Map.of(
            ApiResponseParameterName.STATUS_CODE.getValue(), statusCode.getValue(),
            ApiResponseParameterName.BODY.getValue(), body
        );
    }

    private BadRequestException buildExternalBadRequestException(final Exception exception) {
        if (exception instanceof BadRequestException) {
            return (BadRequestException) exception;
        }
        return new BadRequestException(REQUEST_PARSE_FAILURE_MESSAGE);
    }

    private HttpStatusCode determineStatusCode(final Exception exception) {
        if (exception instanceof BadRequestException) {
            return HttpStatusCode.BAD_REQUEST;
        }
        if (exception instanceof ConflictingResourceException) {
            return HttpStatusCode.CONFLICT;
        }
        if (exception instanceof ResourceNotFoundException) {
            return HttpStatusCode.NOT_FOUND;
        }
        return HttpStatusCode.INTERNAL_SERVER_ERROR;
    }
}
