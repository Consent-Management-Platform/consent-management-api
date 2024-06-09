package com.consentframework.consentmanagement.api.infrastructure.requesthandlers.apigatewaylambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.consentframework.consentmanagement.api.activities.GetServiceUserConsentActivity;
import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.ApiResponseParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.adapters.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.GetServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class GetServiceUserConsentLambdaRequestHandlerTest {
    private GetServiceUserConsentLambdaRequestHandler handler;
    private GetServiceUserConsentActivity activity;
    private ServiceUserConsentRepository consentRepository;
    private Context context;
    private LambdaLogger logger;

    @BeforeEach
    void setup() {
        this.consentRepository = new InMemoryServiceUserConsentRepository();
        this.activity = new GetServiceUserConsentActivity(this.consentRepository);
        this.handler = new GetServiceUserConsentLambdaRequestHandler(this.activity);
        this.context = mock(Context.class);
        this.logger = mock(LambdaLogger.class);
        when(this.context.getLogger()).thenReturn(this.logger);
        doNothing().when(this.logger).log(anyString());
    }

    @Test
    void testHandleRequestWhenConsentExists() throws BadRequestException, ConflictingResourceException {
        final Consent existingConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        consentRepository.createServiceUserConsent(existingConsent);

        final ApiRequest request = buildValidApiRequest();
        final Map<String, Object> response = handler.handleRequest(request, context);
        assertNotNull(response);
        assertStatusCodeEquals(HttpStatusCode.SUCCESS, response);

        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof GetServiceUserConsentResponseContent);
        assertEquals(existingConsent, ((GetServiceUserConsentResponseContent) responseBody).getData());
    }

    @Test
    void testHandleRequestForNonExistingConsent() throws BadRequestException {
        final ApiRequest request = buildValidApiRequest();

        final Map<String, Object> response = handler.handleRequest(request, context);
        assertNotNull(response);
        assertStatusCodeEquals(HttpStatusCode.NOT_FOUND, response);

        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertEquals(expectedErrorMessage, getResponseBody(response));
    }

    @Test
    void testHandleNullRequest() {
        final Map<String, Object> response = handler.handleRequest(null, context);
        assertEqualsMissingPathParametersResponse(response);
    }

    @Test
    void testHandleRequestWithoutPathParameters() {
        assertReturnsMissingPathParametersResponse(null);
    }

    @Test
    void testHandleResponseMissingServiceId() {
        final Map<String, String> pathParameters = Map.of(
            ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID,
            ApiPathParameterName.CONSENT_ID.getValue(), TestConstants.TEST_CONSENT_ID
        );
        assertReturnsMissingPathParametersResponse(pathParameters);
    }

    @Test
    void testHandleResponseMissingUserId() {
        final Map<String, String> pathParameters = Map.of(
            ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
            ApiPathParameterName.CONSENT_ID.getValue(), TestConstants.TEST_CONSENT_ID
        );
        assertReturnsMissingPathParametersResponse(pathParameters);
    }

    @Test
    void testHandleResponseMissingConsentId() {
        final Map<String, String> pathParameters = Map.of(
            ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
            ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID
        );
        assertReturnsMissingPathParametersResponse(pathParameters);
    }

    private void assertReturnsMissingPathParametersResponse(final Map<String, String> pathParameters) {
        final ApiRequest request = buildApiRequest(pathParameters);

        final Map<String, Object> response = handler.handleRequest(request, context);
        assertEqualsMissingPathParametersResponse(response);
    }

    private void assertEqualsMissingPathParametersResponse(final Map<String, Object> response) {
        assertNotNull(response);
        assertStatusCodeEquals(HttpStatusCode.BAD_REQUEST, response);
        assertEquals(GetServiceUserConsentLambdaRequestHandler.MISSING_PATH_PARAMETERS_MESSAGE,
            getResponseBody(response));
    }

    private void assertStatusCodeEquals(final HttpStatusCode statusCode, final Map<String, Object> response) {
        assertEquals(statusCode.getValue(), response.get(ApiResponseParameterName.STATUS_CODE.getValue()));
    }

    private ApiRequest buildValidApiRequest() {
        final Map<String, String> pathParameters = Map.of(
            ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
            ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID,
            ApiPathParameterName.CONSENT_ID.getValue(), TestConstants.TEST_CONSENT_ID
        );
        return buildApiRequest(pathParameters);
    }

    private ApiRequest buildApiRequest(final Map<String, String> pathParameters) {
        return new ApiRequest(HttpMethod.GET.name(), TestConstants.TEST_GET_CONSENT_PATH, pathParameters, null, null, false, null);
    }

    private Object getResponseBody(final Map<String, Object> response) {
        return response.get(ApiResponseParameterName.BODY.getValue());
    }
}