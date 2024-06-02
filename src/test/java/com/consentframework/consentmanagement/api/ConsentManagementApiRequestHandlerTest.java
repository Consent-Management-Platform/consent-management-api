package com.consentframework.consentmanagement.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsentManagementApiRequestHandlerTest {
    private ConsentManagementApiRequestHandler requestHandler;

    private Context mockContext;
    private LambdaLogger mockLogger;

    @BeforeEach
    void setup() {
        mockContext = mock(Context.class);
        mockLogger = mock(LambdaLogger.class);
        when(mockContext.getLogger()).thenReturn(mockLogger);

        requestHandler = new ConsentManagementApiRequestHandler();
    }

    @Test
    void testHandleRequest() {
        final String request = "TestRequest";
        final String expectedResponse = String.format("Mock response for request %s", request);

        final String response = requestHandler.handleRequest(request, mockContext);
        assertEquals(expectedResponse, response);
    }
}
