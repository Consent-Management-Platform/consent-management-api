package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.consentframework.consentmanagement.api.JSON;
import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.usecases.activities.CreateServiceUserConsentActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CreateServiceUserConsentRequestHandlerTest extends RequestHandlerTest {
    private static final Map<String, String> VALID_PATH_PARAMETERS = Map.of(
        ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
        ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID
    );

    private CreateServiceUserConsentRequestHandler handler;
    private CreateServiceUserConsentActivity activity;
    private ServiceUserConsentRepository consentRepository;

    @BeforeEach
    void setup() {
        this.consentRepository = new InMemoryServiceUserConsentRepository();
        this.activity = spy(new CreateServiceUserConsentActivity(this.consentRepository));
        this.handler = new CreateServiceUserConsentRequestHandler(this.activity);
    }

    @Test
    void testHandleRequestWhenConsentAlreadyExists() throws BadRequestException, ConflictingResourceException, JsonProcessingException {
        final Consent existingConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        final CreateServiceUserConsentRequestContent requestContent = toRequestContent(existingConsent);

        final String testExceptionMessage = "TestConflictException";
        when(activity.handleRequest(existingConsent.getServiceId(), existingConsent.getUserId(), requestContent))
            .thenThrow(new ConflictingResourceException(testExceptionMessage));

        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMETERS, toString(requestContent));

        final Map<String, Object> response = handler.handleRequest(request);
        assertExceptionResponse(HttpStatusCode.CONFLICT, testExceptionMessage, response);
    }

    @Test
    void testHandleValidRequest() throws BadRequestException, JsonProcessingException {
        final String requestContentString = toRequestContentString(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);
        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMETERS, requestContentString);

        final Map<String, Object> response = handler.handleRequest(request);
        assertSuccessResponse(response);

        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof CreateServiceUserConsentResponseContent);
        assertNotNull(((CreateServiceUserConsentResponseContent) responseBody).getConsentId());
    }

    @Test
    void testHandleNullRequest() {
        final Map<String, Object> response = handler.handleRequest(null);
        assertMissingConsentsPathParametersResponse(response);
    }

    @Test
    void testHandleRequestMissingPathParameters() throws JsonProcessingException {
        final Map<String, String> incompletePathParameters = Map.of(
            ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID
        );
        final String requestContentString = toRequestContentString(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);
        final ApiRequest request = buildApiRequest(incompletePathParameters, requestContentString);

        final Map<String, Object> response = handler.handleRequest(request);
        assertMissingConsentsPathParametersResponse(response);
    }

    @Test
    void testHandleRequestWithInvalidBody() {
        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMETERS, "Invalid request body");

        final Map<String, Object> response = handler.handleRequest(request);
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, ApiRequestHandler.REQUEST_PARSE_FAILURE_MESSAGE, response);
    }

    private ApiRequest buildApiRequest(final Map<String, String> pathParameters, final String body) {
        return new ApiRequest(HttpMethod.POST.name(), TestConstants.TEST_CONSENTS_PATH, pathParameters, null, null, false, body);
    }

    private String toRequestContentString(final Consent consent) throws JsonProcessingException {
        final CreateServiceUserConsentRequestContent requestContent = toRequestContent(consent);
        return toString(requestContent);
    }

    private CreateServiceUserConsentRequestContent toRequestContent(final Consent consent) {
        return new CreateServiceUserConsentRequestContent()
            .consentData(consent.getConsentData())
            .status(consent.getStatus())
            .expiryTime(consent.getExpiryTime());
    }

    private String toString(final CreateServiceUserConsentRequestContent requestContent) throws JsonProcessingException {
        return new JSON().getMapper().writeValueAsString(requestContent);
    }
}
