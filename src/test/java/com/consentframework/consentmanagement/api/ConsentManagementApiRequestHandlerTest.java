package com.consentframework.consentmanagement.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConsentManagementApiRequestHandlerTest {
    @Test
    void testConstructor() {
        final ConsentManagementApiRequestHandler requestHandler = new ConsentManagementApiRequestHandler();
        assertNotNull(requestHandler, "Should be able to successfully initialize request handler");
    }
}
