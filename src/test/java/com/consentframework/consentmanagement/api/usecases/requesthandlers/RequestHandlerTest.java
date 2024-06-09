package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consentframework.consentmanagement.api.domain.constants.ApiResponseParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiExceptionResponseContent;

import java.util.Map;

abstract class RequestHandlerTest {
    protected void assertSuccessResponse(final Map<String, Object> response) {
        assertNotNull(response);
        assertStatusCodeEquals(HttpStatusCode.SUCCESS, response);
    }

    protected void assertExceptionResponse(final HttpStatusCode expectedStatusCode, final String expectedMessage,
            final Map<String, Object> response) {
        assertNotNull(response);
        assertStatusCodeEquals(expectedStatusCode, response);
        assertExceptionResponseEquals(expectedMessage, response);
    }

    protected Object getResponseBody(final Map<String, Object> response) {
        return response.get(ApiResponseParameterName.BODY.getValue());
    }

    private void assertStatusCodeEquals(final HttpStatusCode statusCode, final Map<String, Object> response) {
        assertEquals(statusCode.getValue(), response.get(ApiResponseParameterName.STATUS_CODE.getValue()));
    }

    private void assertExceptionResponseEquals(final String expectedMessage, final Map<String, Object> response) {
        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof ApiExceptionResponseContent,
            String.format("Expected response body to be ApiExceptionResponseContent but was %s", responseBody.getClass().getName()));
        assertEquals(expectedMessage,
            ((ApiExceptionResponseContent) responseBody).message());
    }
}
