package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.GetServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.usecases.activities.GetServiceUserConsentActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class GetServiceUserConsentRequestHandlerTest extends RequestHandlerTest {
    private GetServiceUserConsentRequestHandler handler;
    private GetServiceUserConsentActivity activity;
    private ServiceUserConsentRepository consentRepository;

    @BeforeEach
    void setup() {
        this.consentRepository = new InMemoryServiceUserConsentRepository();
        this.activity = new GetServiceUserConsentActivity(this.consentRepository);
        this.handler = new GetServiceUserConsentRequestHandler(this.activity);
    }

    @Test
    void testHandleRequestWhenConsentExists() throws BadRequestException, ConflictingResourceException {
        final Consent existingConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        consentRepository.createServiceUserConsent(existingConsent);

        final ApiRequest request = buildValidApiRequest();
        final Map<String, Object> response = handler.handleRequest(request);
        assertSuccessResponse(response);

        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof GetServiceUserConsentResponseContent);
        assertEquals(existingConsent, ((GetServiceUserConsentResponseContent) responseBody).getData());
    }

    @Test
    void testHandleRequestForNonExistingConsent() throws BadRequestException {
        final ApiRequest request = buildValidApiRequest();

        final Map<String, Object> response = handler.handleRequest(request);

        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertExceptionResponse(HttpStatusCode.NOT_FOUND, expectedErrorMessage, response);
    }

    @Test
    void testHandleNullRequest() {
        final Map<String, Object> response = handler.handleRequest(null);
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, GetServiceUserConsentRequestHandler.MISSING_PATH_PARAMETERS_MESSAGE, response);
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

        final Map<String, Object> response = handler.handleRequest(request);
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, GetServiceUserConsentRequestHandler.MISSING_PATH_PARAMETERS_MESSAGE, response);
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
        return new ApiRequest(HttpMethod.GET.name(), TestConstants.TEST_CONSENT_PATH, pathParameters, null, null, false, null);
    }
}
