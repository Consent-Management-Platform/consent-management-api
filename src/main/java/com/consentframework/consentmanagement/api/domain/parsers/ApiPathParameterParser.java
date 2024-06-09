package com.consentframework.consentmanagement.api.domain.parsers;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;

/**
 * Utility class for parsing API path parameters.
 */
public final class ApiPathParameterParser {
    static final String PATH_PARAMETER_NOT_FOUND_MESSAGE = "Unable to parse %s path parameter from request";

    private ApiPathParameterParser() {}

    /**
     * Parse path parameter value from request.
     *
     * @param request API request
     * @return path parameter value
     * @throws BadRequestException exception thrown when path parameter not found
     */
    public static String parsePathParameter(final ApiRequest request, final ApiPathParameterName parameter) throws BadRequestException {
        if (request == null || request.pathParameters() == null || !request.pathParameters().containsKey(parameter.getValue())) {
            throw new BadRequestException(String.format(PATH_PARAMETER_NOT_FOUND_MESSAGE, parameter.getValue()));
        }
        return request.pathParameters().get(parameter.getValue());
    }
}
