package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.constants.ApiQueryStringParameterName;
import com.consentframework.consentmanagement.api.domain.constants.HttpMethod;
import com.consentframework.consentmanagement.api.domain.constants.HttpStatusCode;
import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.parsers.ApiPathParameterParser;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.repositories.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ListServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import com.consentframework.consentmanagement.api.usecases.activities.ListServiceUserConsentsActivity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class ListServiceUserConsentsRequestHandlerTest extends RequestHandlerTest {
    private static final Map<String, String> COMPLETE_PATH_PARAMETERS = Map.of(
        ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID,
        ApiPathParameterName.USER_ID.getValue(), TestConstants.TEST_USER_ID
    );

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
    void testHandleNullRequest() {
        final Map<String, Object> response = handler.handleRequest(null);
        final String expectedErrorMessage = String.format(ApiPathParameterParser.PARSE_FAILURE_MESSAGE,
            ApiPathParameterName.SERVICE_ID.getValue());
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, expectedErrorMessage, response);
    }

    @Test
    void testHandleRequestMissingPathParameters() {
        final Map<String, String> incompletePathParameters = Map.of(
            ApiPathParameterName.SERVICE_ID.getValue(), TestConstants.TEST_SERVICE_ID);
        final ApiRequest request = buildApiRequest(incompletePathParameters, null);
        final Map<String, Object> response = handler.handleRequest(request);

        final String expectedErrorMessage = String.format(ApiPathParameterParser.PARSE_FAILURE_MESSAGE,
            ApiPathParameterName.USER_ID.getValue());
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, expectedErrorMessage, response);
    }

    @Test
    void testHandleRequestWithInvalidPageToken() {
        final String invalidPageToken = "InvalidPageToken";
        final ApiRequest request = buildApiRequest(COMPLETE_PATH_PARAMETERS, Map.of(
            ApiQueryStringParameterName.PAGE_TOKEN.getValue(), invalidPageToken));
        final Map<String, Object> response = handler.handleRequest(request);
        final String expectedErrorMessage = String.format(InMemoryServiceUserConsentRepository.INVALID_PAGE_TOKEN_MESSAGE,
            invalidPageToken);
        assertExceptionResponse(HttpStatusCode.BAD_REQUEST, expectedErrorMessage, response);
    }

    @Test
    void testHandleRequestWhenNoConsents() {
        final ApiRequest request = buildApiRequest(COMPLETE_PATH_PARAMETERS, TestConstants.TEST_PAGINATION_QUERY_PARAMETERS);
        final Map<String, Object> response = handler.handleRequest(request);
        assertSuccessResponse(response, List.of(), null);
    }

    @Test
    void testHandlePaginatedRequests() throws BadRequestException, ConflictingResourceException {
        final Consent firstConsent = TestConstants.TEST_CONSENT_WITH_ONLY_REQUIRED_FIELDS;
        final Consent secondConsent = TestUtils.clone(firstConsent).consentId("SecondConsentId");
        final Consent thirdConsent = TestUtils.clone(firstConsent).consentId("ThirdConsentId");
        final Consent fourthConsent = TestUtils.clone(firstConsent).consentId("FourthConsentId");
        final List<Consent> allConsents = List.of(firstConsent, secondConsent, thirdConsent, fourthConsent);

        for (final Consent consent : allConsents) {
            consentRepository.createServiceUserConsent(consent);
        }

        final ApiRequest firstRequest = buildApiRequest(COMPLETE_PATH_PARAMETERS, Map.of(
            ApiQueryStringParameterName.LIMIT.getValue(), 2,
            ApiQueryStringParameterName.PAGE_TOKEN.getValue(), "1"
        ));
        final Map<String, Object> firstResponse = handler.handleRequest(firstRequest);

        final String expectedNextPageToken = "3";
        assertSuccessResponse(firstResponse, List.of(secondConsent, thirdConsent), expectedNextPageToken);

        final ApiRequest secondRequest = buildApiRequest(COMPLETE_PATH_PARAMETERS, Map.of(
            ApiQueryStringParameterName.LIMIT.getValue(), 2,
            ApiQueryStringParameterName.PAGE_TOKEN.getValue(), expectedNextPageToken
        ));
        final Map<String, Object> secondResponse = handler.handleRequest(secondRequest);
        assertSuccessResponse(secondResponse, List.of(fourthConsent), null);
    }

    private void assertSuccessResponse(final Map<String, Object> response, final List<Consent> expectedConsents,
            final String expectedNextPageToken) {
        super.assertSuccessResponse(response);

        final Object responseBody = getResponseBody(response);
        assertTrue(responseBody instanceof ListServiceUserConsentResponseContent);
        final ListServiceUserConsentResponseContent listResponseContent = (ListServiceUserConsentResponseContent) responseBody;

        assertEquals(expectedConsents, listResponseContent.getData());
        assertEquals(expectedNextPageToken, listResponseContent.getNextPageToken());
    }

    private ApiRequest buildApiRequest(final Map<String, String> pathParameters, final Map<String, Object> queryStringParameters) {
        return new ApiRequest(HttpMethod.GET.name(), TestConstants.TEST_CONSENTS_PATH, pathParameters, queryStringParameters,
            null, false, null);
    }
}
