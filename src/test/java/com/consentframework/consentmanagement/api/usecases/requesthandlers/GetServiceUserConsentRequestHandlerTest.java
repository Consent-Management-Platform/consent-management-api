package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consentframework.consentmanagement.api.JSON;
import com.consentframework.consentmanagement.api.domain.constants.ApiHttpResource;
import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.GetServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.usecases.activities.GetServiceUserConsentActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
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
    void testHandleRequestWhenConsentExists() throws BadRequestException, ConflictingResourceException, InternalServiceException,
            JsonProcessingException {
        final Consent existingConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        consentRepository.createServiceUserConsent(existingConsent);

        final ApiRequest request = buildApiRequest(TestConstants.TEST_CONSENT_PATH_PARAMS);
        final Map<String, Object> response = handler.handleRequest(request);
        assertSuccessResponse(response);

        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof String);

        final GetServiceUserConsentResponseContent parsedResponse = new JSON().getMapper()
            .readValue((String) responseBody, GetServiceUserConsentResponseContent.class);
        assertEquals(existingConsent, parsedResponse.getData());
    }

    @Test
    void testHandleRequestForNonExistingConsent() throws BadRequestException {
        final ApiRequest request = buildApiRequest(TestConstants.TEST_CONSENT_PATH_PARAMS);

        final Map<String, Object> response = handler.handleRequest(request);

        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertExceptionResponse(HttpStatusCode.NOT_FOUND, expectedErrorMessage, response);
    }

    @Test
    protected void testHandleNullRequest() {
        final Map<String, Object> response = handler.handleRequest(null);
        assertMissingConsentPathParametersResponse(response);
    }

    @Test
    void testHandleRequestWithoutPathParameters() {
        final ApiRequest request = buildApiRequest(null);
        final Map<String, Object> response = handler.handleRequest(request);
        assertMissingConsentPathParametersResponse(response);
    }

    @Test
    protected void testHandleRequestMissingPathParameters() {
        final List<Map<String, String>> incompletePathParamConfigs = List.of(
            Map.of(
                ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID,
                ApiPathParameterName.CONSENT_ID.getValue(), TestConstants.TEST_CONSENT_ID
            ),
            Map.of(
                ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
                ApiPathParameterName.CONSENT_ID.getValue(), TestConstants.TEST_CONSENT_ID
            ),
            Map.of(
                ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
                ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID
            )
        );

        for (final Map<String, String> pathParameters : incompletePathParamConfigs) {
            final ApiRequest request = buildApiRequest(pathParameters);

            final Map<String, Object> response = handler.handleRequest(request);
            assertMissingConsentPathParametersResponse(response);
        }
    }

    private ApiRequest buildApiRequest(final Map<String, String> pathParameters) {
        return new ApiRequest(HttpMethod.GET.name(), ApiHttpResource.SERVICE_USER_CONSENT.getValue(), TestConstants.TEST_CONSENT_PATH,
            pathParameters, null, null, false, null);
    }
}
