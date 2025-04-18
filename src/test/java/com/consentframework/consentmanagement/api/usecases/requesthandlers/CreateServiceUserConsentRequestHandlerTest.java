package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.consentframework.consentmanagement.api.JSON;
import com.consentframework.consentmanagement.api.domain.constants.ApiHttpResource;
import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import com.consentframework.consentmanagement.api.usecases.activities.CreateServiceUserConsentActivity;
import com.consentframework.shared.api.domain.constants.HttpMethod;
import com.consentframework.shared.api.domain.constants.HttpStatusCode;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.shared.api.domain.exceptions.InternalServiceException;
import com.consentframework.shared.api.domain.requesthandlers.ApiRequestHandler;
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
    void testHandleRequestWhenConsentAlreadyExists() throws BadRequestException, ConflictingResourceException, InternalServiceException,
            JsonProcessingException {
        final Consent existingConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        final CreateServiceUserConsentRequestContent requestContent = TestUtils.toCreateRequestContent(existingConsent);

        final String testExceptionMessage = "TestConflictException";
        when(activity.handleRequest(existingConsent.getServiceId(), existingConsent.getUserId(), requestContent))
            .thenThrow(new ConflictingResourceException(testExceptionMessage));

        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMETERS, TestUtils.toString(requestContent));

        final Map<String, Object> response = handler.handleRequest(request);
        assertExceptionResponse(HttpStatusCode.CONFLICT, testExceptionMessage, response);
    }

    @Test
    void testHandleValidRequest() throws BadRequestException, JsonProcessingException {
        final String requestContentString = TestUtils.toCreateRequestContentString(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);
        final ApiRequest request = buildApiRequest(VALID_PATH_PARAMETERS, requestContentString);

        final Map<String, Object> response = handler.handleRequest(request);
        assertSuccessResponse(response);

        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof String);

        final CreateServiceUserConsentResponseContent parsedResponse = new JSON().getMapper()
            .readValue((String) responseBody, CreateServiceUserConsentResponseContent.class);
        assertNotNull(parsedResponse.getConsentId());
    }

    @Test
    protected void testHandleNullRequest() {
        final Map<String, Object> response = handler.handleRequest(null);
        assertMissingConsentsPathParametersResponse(response);
    }

    @Test
    protected void testHandleRequestMissingPathParameters() throws JsonProcessingException {
        final Map<String, String> incompletePathParameters = Map.of(
            ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID
        );
        final String requestContentString = TestUtils.toCreateRequestContentString(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);
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
        return new ApiRequest(HttpMethod.POST.name(), ApiHttpResource.SERVICE_USER_CONSENTS.getValue(), TestConstants.TEST_CONSENTS_PATH,
            pathParameters, null, null, false, body);
    }
}
