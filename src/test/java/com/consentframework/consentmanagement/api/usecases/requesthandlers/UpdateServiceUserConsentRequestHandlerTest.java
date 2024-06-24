package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.consentframework.consentmanagement.api.domain.constants.ApiHttpResource;
import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.InternalServiceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import com.consentframework.consentmanagement.api.usecases.activities.UpdateServiceUserConsentActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class UpdateServiceUserConsentRequestHandlerTest extends RequestHandlerTest {
    private UpdateServiceUserConsentRequestHandler handler;
    private UpdateServiceUserConsentActivity activity;
    private ServiceUserConsentRepository consentRepository;

    @BeforeEach
    void setup() {
        this.consentRepository = new InMemoryServiceUserConsentRepository();
        this.activity = new UpdateServiceUserConsentActivity(consentRepository);
        this.handler = new UpdateServiceUserConsentRequestHandler(activity);
    }

    @Test
    protected void testHandleNullRequest() {
        final Map<String, Object> response = handler.handleRequest(null);
        assertMissingConsentPathParametersResponse(response);
    }

    @Test
    protected void testHandleRequestMissingPathParameters() {
        final Map<String, String> incompletePathParameters = Map.of(
            ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
            ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID);
        final ApiRequest request = buildApiRequest(incompletePathParameters, null);

        final Map<String, Object> response = handler.handleRequest(request);
        assertMissingConsentPathParametersResponse(response);
    }

    @Test
    void testHandleRequestWithInvalidData() {
        final String invalidData = "TestInvalidConsentData";
        final ApiRequest request = buildApiRequest(TestConstants.TEST_CONSENT_PATH_PARAMS, invalidData);
        final Map<String, Object> response = handler.handleRequest(request);
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, ApiRequestHandler.REQUEST_PARSE_FAILURE_MESSAGE, response);
    }

    @Test
    void testHandleRequestForNonExistingConsent() throws JsonProcessingException {
        final ApiRequest request = buildApiRequest(TestConstants.TEST_CONSENT_PATH_PARAMS,
            TestUtils.toUpdateRequestContentString(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS));
        final Map<String, Object> response = handler.handleRequest(request);
        final String expectedErrorMessage = String.format(ServiceUserConsentRepository.CONSENT_NOT_FOUND_MESSAGE,
            TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertExceptionResponse(HttpStatusCode.NOT_FOUND, expectedErrorMessage, response);
    }

    @Test
    void testHandleValidRequest() throws BadRequestException, ConflictingResourceException, InternalServiceException,
            JsonProcessingException, ResourceNotFoundException {
        consentRepository.createServiceUserConsent(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);

        final Consent updatedConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS).consentVersion(2);
        final ApiRequest request = buildApiRequest(TestConstants.TEST_CONSENT_PATH_PARAMS,
            TestUtils.toUpdateRequestContentString(updatedConsent));

        final Map<String, Object> response = handler.handleRequest(request);
        assertSuccessResponse(response);

        final Consent storedConsent = consentRepository.getServiceUserConsent(TestConstants.TEST_SERVICE_ID,
            TestConstants.TEST_USER_ID, TestConstants.TEST_CONSENT_ID);
        assertEquals(updatedConsent.getConsentVersion(), storedConsent.getConsentVersion());
        assertEquals(updatedConsent.getConsentData(), storedConsent.getConsentData());
        assertEquals(updatedConsent.getExpiryTime().toEpochSecond(), storedConsent.getExpiryTime().toEpochSecond());
    }

    private ApiRequest buildApiRequest(final Map<String, String> pathParameters, final String body) {
        return new ApiRequest(HttpMethod.POST.name(), ApiHttpResource.SERVICE_USER_CONSENT.getValue(), TestConstants.TEST_CONSENT_PATH,
            pathParameters, null, null, false, body);
    }
}
