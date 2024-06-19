package com.consentframework.consentmanagement.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsentManagementApiServiceTest {
    private ConsentManagementApiService service;

    @BeforeEach
    void setup() {
        service = new ConsentManagementApiService();
    }

    @Test
    void testHandleRequest() {
        final ApiRequest request = new ApiRequest(
            HttpMethod.GET.name(),
            TestConstants.TEST_CONSENT_PATH,
            TestConstants.TEST_CONSENT_PATH_PARAMS,
            null,
            null,
            false,
            null
        );
        assertThrows(UnsupportedOperationException.class, () ->
            service.handleRequest(request, mock(Context.class)));
    }
}
