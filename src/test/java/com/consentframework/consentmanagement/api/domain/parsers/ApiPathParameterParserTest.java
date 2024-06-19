package com.consentframework.consentmanagement.api.domain.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ApiPathParameterParserTest {
    @Test
    void testParseParameterWhenRequestNull() {
        validateExceptionThrownForMissingParameters(null);
    }

    @Test
    void testParseParameterWhenNoPathParameters() {
        final ApiRequest request = new ApiRequest(HttpMethod.GET.name(), "/", "/", null, null, null, false, null);
        validateExceptionThrownForMissingParameters(request);
    }

    @Test
    void testParseParameterWhenNotPresent() {
        final Map<String, String> pathParameters = Map.of("parameter1", "value1");
        final ApiRequest request = new ApiRequest(HttpMethod.GET.name(), "/", "/", pathParameters, null, null, false, null);
        validateExceptionThrownForMissingParameters(request);
    }

    @Test
    void testParseParameterWhenPresent() throws BadRequestException {
        final String serviceId = "TestServiceId";
        final Map<String, String> pathParameters = Map.of(
            "parameter1", "value1",
            ApiPathParameterName.SERVICE_ID.getValue(), serviceId
        );
        final ApiRequest request = new ApiRequest(HttpMethod.GET.name(), "/", "/", pathParameters, null, null, false, null);

        final String parsedParameterValue = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.SERVICE_ID);
        assertEquals(serviceId, parsedParameterValue);
    }

    private void validateExceptionThrownForMissingParameters(final ApiRequest request) {
        final BadRequestException thrownException = assertThrows(BadRequestException.class, () ->
            ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.SERVICE_ID));

        final String expectedErrorMessage = String.format(ApiPathParameterParser.PARSE_FAILURE_MESSAGE,
            ApiPathParameterName.SERVICE_ID.getValue());
        assertEquals(expectedErrorMessage, thrownException.getMessage());
    }
}
