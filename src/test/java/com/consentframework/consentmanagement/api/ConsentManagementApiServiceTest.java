package com.consentframework.consentmanagement.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.consentframework.consentmanagement.api.domain.constants.ApiHttpResource;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import com.consentframework.consentmanagement.api.usecases.requesthandlers.RequestHandlerTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ConsentManagementApiServiceTest extends RequestHandlerTest {
    private ServiceUserConsentRepository consentRepository;
    private ConsentManagementApiService service;

    @BeforeEach
    void setup() {
        consentRepository = spy(new InMemoryServiceUserConsentRepository());
        service = new ConsentManagementApiService(consentRepository);
    }

    @Test
    void testHandleGetRequest() throws BadRequestException, ConflictingResourceException, ResourceNotFoundException {
        final Consent existingConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        consentRepository.createServiceUserConsent(existingConsent);

        final ApiRequest request = new ApiRequest(
            HttpMethod.GET.name(),
            ApiHttpResource.SERVICE_USER_CONSENT.getValue(),
            TestConstants.TEST_CONSENT_PATH,
            TestConstants.TEST_CONSENT_PATH_PARAMS,
            null,
            null,
            false,
            null
        );
        final Map<String, Object> response = service.handleRequest(request, null);
        assertSuccessResponse(response);

        verify(consentRepository).getServiceUserConsent(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
            TestConstants.TEST_CONSENT_ID);
    }

    @Test
    void testHandleListRequest() throws BadRequestException {
        final ApiRequest request = new ApiRequest(
            HttpMethod.GET.name(),
            ApiHttpResource.SERVICE_USER_CONSENTS.getValue(),
            TestConstants.TEST_CONSENTS_PATH,
            TestConstants.TEST_CONSENTS_PATH_PARAMS,
            null,
            null,
            false,
            null
        );
        final Map<String, Object> response = service.handleRequest(request, null);
        assertSuccessResponse(response);

        verify(consentRepository).listServiceUserConsents(TestConstants.TEST_SERVICE_ID, TestConstants.TEST_USER_ID,
            null, null);
    }

    @Test
    void testHandleCreateRequest() throws BadRequestException, ConflictingResourceException, JsonProcessingException {
        final String requestContentString = TestUtils.toCreateRequestContentString(TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS);
        final ApiRequest request = new ApiRequest(
            HttpMethod.POST.name(),
            ApiHttpResource.SERVICE_USER_CONSENTS.getValue(),
            TestConstants.TEST_CONSENTS_PATH,
            TestConstants.TEST_CONSENTS_PATH_PARAMS,
            null,
            null,
            false,
            requestContentString
        );
        final Map<String, Object> response = service.handleRequest(request, null);
        assertSuccessResponse(response);

        verify(consentRepository).createServiceUserConsent(any(Consent.class));
    }

    @Test
    void testHandleUpdateRequest() throws BadRequestException, ConflictingResourceException, JsonProcessingException,
            ResourceNotFoundException {
        final Consent existingConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        consentRepository.createServiceUserConsent(existingConsent);

        final Consent updatedConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS).consentVersion(2);
        final String updateRequestBody = TestUtils.toUpdateRequestContentString(updatedConsent);

        final ApiRequest request = new ApiRequest(
            HttpMethod.POST.name(),
            ApiHttpResource.SERVICE_USER_CONSENT.getValue(),
            TestConstants.TEST_CONSENT_PATH,
            TestConstants.TEST_CONSENT_PATH_PARAMS,
            null,
            null,
            false,
            updateRequestBody
        );

        final Map<String, Object> response = service.handleRequest(request, null);
        assertSuccessResponse(response);

        verify(consentRepository).updateServiceUserConsent(any(Consent.class));
    }

    @Test
    void testHandleUnsupportedResourceOperation() {
        final String unsupportedHttpOperation = "DELETE";
        final ApiRequest request = new ApiRequest(
            unsupportedHttpOperation,
            ApiHttpResource.SERVICE_USER_CONSENT.getValue(),
            TestConstants.TEST_CONSENT_PATH,
            TestConstants.TEST_CONSENT_PATH_PARAMS,
            null,
            null,
            false,
            null
        );

        final Map<String, Object> response = service.handleRequest(request, null);
        final String expectedErrorMessage = String.format(ConsentManagementApiService.UNSUPPORTED_OPERATION_MESSAGE,
            ApiHttpResource.SERVICE_USER_CONSENT.getValue(), unsupportedHttpOperation);
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, expectedErrorMessage, response);
    }

    @Test
    protected void testHandleNullRequest() throws Exception {
        final Map<String, Object> response = service.handleRequest(null, null);
        final String expectedErrorMessage = String.format(ConsentManagementApiService.UNSUPPORTED_OPERATION_MESSAGE, null, null);
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, expectedErrorMessage, response);
    }

    @Test
    protected void testHandleRequestMissingPathParameters() throws Exception {
        final ApiRequest request = new ApiRequest(HttpMethod.GET.name(), ApiHttpResource.SERVICE_USER_CONSENT.getValue(),
            TestConstants.TEST_CONSENT_PATH, null, null, null, false, null);

        final Map<String, Object> response = service.handleRequest(request, null);
        assertMissingConsentPathParametersResponse(response);
    }
}
