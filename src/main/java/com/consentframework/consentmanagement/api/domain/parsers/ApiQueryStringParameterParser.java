package com.consentframework.consentmanagement.api.domain.parsers;

import com.consentframework.consentmanagement.api.domain.constants.ApiQueryStringParameterName;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * API query string parameter parser.
 */
public final class ApiQueryStringParameterParser {
    public static final String PARSE_FAILURE_MESSAGE = "Unable to parse %s query parameter from request";

    private static final String WRONG_TYPE_LOG_MESSAGE = "Expected %s query parameter to be a %s but was a %s: %s";

    private static final Logger logger = LogManager.getLogger(ApiQueryStringParameterParser.class);

    private ApiQueryStringParameterParser() {}

    /**
     * Parse string query string parameter value from request.
     *
     * @param request API request
     * @return query string parameter value, or null if not found
     * @throws BadRequestException exception thrown when query string parameter value is not a string
     */
    public static String parseStringQueryStringParameter(final ApiRequest request, final ApiQueryStringParameterName parameter)
            throws BadRequestException {
        final Object pathParameterValue = parseQueryStringParameterObject(request, parameter);
        if (pathParameterValue == null || pathParameterValue instanceof String) {
            return (String) pathParameterValue;
        }
        logger.warn(String.format(WRONG_TYPE_LOG_MESSAGE, parameter.getValue(), String.class, pathParameterValue.getClass(),
            pathParameterValue.toString()));
        throw buildBadRequestException(parameter);
    }

    /**
     * Parse integer query string parameter value from request.
     *
     * @param request API request
     * @return query string parameter value, or null if not found
     * @throws BadRequestException exception thrown when query string parameter value is not an integer
     */
    public static Integer parseIntQueryStringParameter(final ApiRequest request, final ApiQueryStringParameterName parameter)
            throws BadRequestException {
        final Object pathParameterValue = parseQueryStringParameterObject(request, parameter);
        if (pathParameterValue == null || pathParameterValue instanceof Integer) {
            return (Integer) pathParameterValue;
        }
        if (pathParameterValue instanceof String) {
            try {
                return Integer.parseInt((String) pathParameterValue);
            } catch (final NumberFormatException e) {
                logger.warn(String.format(PARSE_FAILURE_MESSAGE, parameter.getValue())
                    + String.format(", value '%s' is not parseable as Integer", pathParameterValue.toString()));
                throw buildBadRequestException(parameter);
            }
        }
        logger.warn(String.format(WRONG_TYPE_LOG_MESSAGE, parameter.getValue(), Integer.class, pathParameterValue.getClass(),
            pathParameterValue.toString()));
        throw buildBadRequestException(parameter);
    }

    /**
     * Parse query string parameter value from request.
     *
     * @param request API request
     * @return query string parameter value, or null if not found
     */
    private static Object parseQueryStringParameterObject(final ApiRequest request, final ApiQueryStringParameterName parameter) {
        if (!hasQueryStringParameter(request, parameter)) {
            return null;
        }
        return request.queryStringParameters().get(parameter.getValue());
    }

    private static boolean hasQueryStringParameter(final ApiRequest request, final ApiQueryStringParameterName parameter) {
        return request != null && request.queryStringParameters() != null
            && request.queryStringParameters().containsKey(parameter.getValue());
    }

    private static BadRequestException buildBadRequestException(final ApiQueryStringParameterName parameter) {
        return new BadRequestException(String.format(PARSE_FAILURE_MESSAGE, parameter.getValue()));
    }
}
