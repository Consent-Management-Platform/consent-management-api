package com.consentframework.consentmanagement.api.domain.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.constants.ApiQueryStringParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ApiQueryStringParameterParserTest {
    private static final Map<String, Object> INCOMPLETE_QUERY_PARAMETERS = Map.of(
        "TestStringParamKey", "TestStringParamValue",
        "TestIntegerParamKey", 100,
        "TestBooleanParamKey", true
    );

    @Nested
    class ParseStringParameter {
        @Test
        void testWhenRequestNull() throws BadRequestException {
            validateReturnsNull(null, ApiQueryStringParameterName.PAGE_TOKEN);
        }

        @Test
        void testWhenNoQueryParameters() throws BadRequestException {
            final ApiRequest request = buildRequest(null);
            validateReturnsNull(request, ApiQueryStringParameterName.PAGE_TOKEN);
        }

        @Test
        void testWhenMissingQueryParameter() throws BadRequestException {
            final ApiRequest request = buildRequest(INCOMPLETE_QUERY_PARAMETERS);
            validateReturnsNull(request, ApiQueryStringParameterName.PAGE_TOKEN);
        }

        @Test
        void testWhenWrongType() {
            final ApiRequest request = buildRequest(TestConstants.TEST_PAGINATION_QUERY_PARAMETERS);

            final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                ApiQueryStringParameterParser.parseStringQueryStringParameter(request, ApiQueryStringParameterName.LIMIT));

            final String expectedErrorMessage = String.format(ApiQueryStringParameterParser.PARSE_FAILURE_MESSAGE,
                ApiQueryStringParameterName.LIMIT.getValue());
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @Test
        void testWhenQueryParameterPresent() throws BadRequestException {
            final ApiRequest request = buildRequest(TestConstants.TEST_PAGINATION_QUERY_PARAMETERS);
            final String parameterValue = ApiQueryStringParameterParser.parseStringQueryStringParameter(
                request, ApiQueryStringParameterName.PAGE_TOKEN);
            assertEquals(TestConstants.TEST_PAGE_TOKEN, parameterValue);
        }

        private void validateReturnsNull(final ApiRequest request, final ApiQueryStringParameterName parameter)
                throws BadRequestException {
            final String parameterValue = ApiQueryStringParameterParser.parseStringQueryStringParameter(request, parameter);
            assertNull(parameterValue);
        }
    }

    @Nested
    class ParseIntParameter {
        @Test
        void testWhenRequestNull() throws BadRequestException {
            validateReturnsNull(null, ApiQueryStringParameterName.LIMIT);
        }

        @Test
        void testWhenNoQueryParameters() throws BadRequestException {
            final ApiRequest request = buildRequest(null);
            validateReturnsNull(request, ApiQueryStringParameterName.LIMIT);
        }

        @Test
        void testWhenMissingQueryParameter() throws BadRequestException {
            final ApiRequest request = buildRequest(INCOMPLETE_QUERY_PARAMETERS);
            validateReturnsNull(request, ApiQueryStringParameterName.LIMIT);
        }

        @Test
        void testWhenWrongType() {
            final ApiRequest request = buildRequest(TestConstants.TEST_PAGINATION_QUERY_PARAMETERS);

            final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
                ApiQueryStringParameterParser.parseIntQueryStringParameter(request, ApiQueryStringParameterName.PAGE_TOKEN));

            final String expectedErrorMessage = String.format(ApiQueryStringParameterParser.PARSE_FAILURE_MESSAGE,
                ApiQueryStringParameterName.PAGE_TOKEN.getValue());
            assertEquals(expectedErrorMessage, thrownException.getMessage());
        }

        @Test
        void testWhenQueryParameterPresent() throws BadRequestException {
            final ApiRequest request = buildRequest(TestConstants.TEST_PAGINATION_QUERY_PARAMETERS);
            final Integer parameterValue = ApiQueryStringParameterParser.parseIntQueryStringParameter(
                request, ApiQueryStringParameterName.LIMIT);
            assertEquals(TestConstants.TEST_PAGE_LIMIT, parameterValue);
        }

        private void validateReturnsNull(final ApiRequest request, final ApiQueryStringParameterName parameter)
                throws BadRequestException {
            final Integer parameterValue = ApiQueryStringParameterParser.parseIntQueryStringParameter(request, parameter);
            assertNull(parameterValue);
        }
    }

    private ApiRequest buildRequest(final Map<String, Object> queryParameters) {
        return new ApiRequest(HttpMethod.GET.name(), "/", "/", null, queryParameters, null, false, null);
    }
}
