package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consentframework.consentmanagement.api.JSON;
import com.consentframework.consentmanagement.api.domain.constants.ApiHttpResource;
import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.ApiQueryStringParameterName;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ListServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import com.consentframework.consentmanagement.api.usecases.activities.ListServiceUserConsentsActivity;
import com.consentframework.shared.api.domain.constants.HttpMethod;
import com.consentframework.shared.api.domain.constants.HttpStatusCode;
import com.consentframework.shared.api.domain.entities.ApiRequest;
import com.consentframework.shared.api.domain.exceptions.BadRequestException;
import com.consentframework.shared.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.shared.api.domain.exceptions.InternalServiceException;
import com.consentframework.shared.api.domain.parsers.ApiQueryStringParameterParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ListServiceUserConsentsRequestHandlerTest extends RequestHandlerTest {
    private ListServiceUserConsentsRequestHandler handler;
    private ListServiceUserConsentsActivity activity;
    private ServiceUserConsentRepository consentRepository;

    @BeforeEach
    void setup() {
        this.consentRepository = new InMemoryServiceUserConsentRepository();
        this.activity = new ListServiceUserConsentsActivity(consentRepository);
        this.handler = new ListServiceUserConsentsRequestHandler(activity);
    }

    @Test
    protected void testHandleNullRequest() {
        final Map<String, Object> response = handler.handleRequest(null);
        assertMissingConsentsPathParametersResponse(response);
    }

    @Test
    protected void testHandleRequestMissingPathParameters() {
        final Map<String, String> incompletePathParameters = Map.of(
            ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID);
        final ApiRequest request = buildApiRequest(incompletePathParameters, null);

        final Map<String, Object> response = handler.handleRequest(request);
        assertMissingConsentsPathParametersResponse(response);
    }

    @Test
    void testHandleRequestWithWrongTypeQueryParam() {
        final long pageTokenWithWrongType = 1234L;
        final ApiRequest request = buildApiRequest(TestConstants.TEST_CONSENTS_PATH_PARAMS, Map.of(
            ApiQueryStringParameterName.PAGE_TOKEN.getValue(), pageTokenWithWrongType));

        final Map<String, Object> response = handler.handleRequest(request);
        final String expectedErrorMessage = String.format(ApiQueryStringParameterParser.PARSE_FAILURE_MESSAGE,
            ApiQueryStringParameterName.PAGE_TOKEN.getValue());
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, expectedErrorMessage, response);
    }

    @Test
    void testHandleRequestWithInvalidPageToken() {
        final String invalidPageToken = "InvalidPageToken";
        final ApiRequest request = buildApiRequest(TestConstants.TEST_CONSENTS_PATH_PARAMS, Map.of(
            ApiQueryStringParameterName.PAGE_TOKEN.getValue(), invalidPageToken));

        final Map<String, Object> response = handler.handleRequest(request);
        final String expectedErrorMessage = String.format(InMemoryServiceUserConsentRepository.INVALID_PAGE_TOKEN_MESSAGE,
            invalidPageToken);
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, expectedErrorMessage, response);
    }

    @Test
    void testHandleRequestWhenNoConsents() throws JsonProcessingException {
        final ApiRequest request = buildApiRequest(TestConstants.TEST_CONSENTS_PATH_PARAMS,
            TestConstants.TEST_PAGINATION_QUERY_PARAMETERS);
        final Map<String, Object> response = handler.handleRequest(request);
        assertSuccessResponse(response, List.of(), null);
    }

    @Test
    void testHandlePaginatedRequests() throws BadRequestException, ConflictingResourceException, InternalServiceException,
            JsonProcessingException {
        final Consent firstConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        final Consent secondConsent = TestUtils.clone(firstConsent).consentId("SecondConsentId");
        final Consent thirdConsent = TestUtils.clone(firstConsent).consentId("ThirdConsentId");
        final Consent fourthConsent = TestUtils.clone(firstConsent).consentId("FourthConsentId");
        final List<Consent> allConsents = List.of(firstConsent, secondConsent, thirdConsent, fourthConsent);

        for (final Consent consent : allConsents) {
            consentRepository.createServiceUserConsent(consent);
        }

        final ApiRequest firstRequest = buildApiRequest(TestConstants.TEST_CONSENTS_PATH_PARAMS, Map.of(
            ApiQueryStringParameterName.LIMIT.getValue(), 2,
            ApiQueryStringParameterName.PAGE_TOKEN.getValue(), "1"
        ));
        final Map<String, Object> firstResponse = handler.handleRequest(firstRequest);

        final String expectedNextPageToken = "3";
        assertSuccessResponse(firstResponse, List.of(secondConsent, thirdConsent), expectedNextPageToken);

        final ApiRequest secondRequest = buildApiRequest(TestConstants.TEST_CONSENTS_PATH_PARAMS, Map.of(
            ApiQueryStringParameterName.LIMIT.getValue(), 2,
            ApiQueryStringParameterName.PAGE_TOKEN.getValue(), expectedNextPageToken
        ));
        final Map<String, Object> secondResponse = handler.handleRequest(secondRequest);
        assertSuccessResponse(secondResponse, List.of(fourthConsent), null);
    }

    private void assertSuccessResponse(final Map<String, Object> response, final List<Consent> expectedConsents,
            final String expectedNextPageToken) throws JsonProcessingException {
        super.assertSuccessResponse(response);

        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof String);

        final ListServiceUserConsentResponseContent parsedResponse = new JSON().getMapper()
            .readValue((String) responseBody, ListServiceUserConsentResponseContent.class);

        assertEquals(expectedConsents, parsedResponse.getData());
        assertEquals(expectedNextPageToken, parsedResponse.getNextPageToken());
    }

    private ApiRequest buildApiRequest(final Map<String, String> pathParameters, final Map<String, Object> queryStringParameters) {
        return new ApiRequest(HttpMethod.GET.name(), ApiHttpResource.SERVICE_USER_CONSENTS.getValue(), TestConstants.TEST_CONSENTS_PATH,
            pathParameters, queryStringParameters, null, false, null);
    }
}
