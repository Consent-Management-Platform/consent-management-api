package com.consentframework.consentmanagement.api.activities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.exceptions.ConflictingResourceException;
import com.consentframework.consentmanagement.api.domain.exceptions.ResourceNotFoundException;
import com.consentframework.consentmanagement.api.domain.repositories.ServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.infrastructure.adapters.InMemoryServiceUserConsentRepository;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.ConsentStatus;
import com.consentframework.consentmanagement.api.models.ListServiceUserConsentResponseContent;
import com.consentframework.consentmanagement.api.testcommon.constants.TestConstants;
import com.consentframework.consentmanagement.api.testcommon.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

class ListServiceUserConsentsActivityTest {
    private static final Integer PAGE_LIMIT = 2;

    private ListServiceUserConsentsActivity activity;
    private ServiceUserConsentRepository consentRepository;

    @BeforeEach
    void setup() {
        this.consentRepository = new InMemoryServiceUserConsentRepository();
        this.activity = new ListServiceUserConsentsActivity(this.consentRepository);
    }

    @Test
    void testListWhenNoResults() throws BadRequestException {
        final ListServiceUserConsentResponseContent response = activity.handleRequest(TestConstants.TEST_SERVICE_ID,
            TestConstants.TEST_USER_ID, PAGE_LIMIT, null);

        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
        assertNull(response.getNextPageToken());
    }

    @Test
    void testListWhenMultiplePagesOfResults() throws BadRequestException, ConflictingResourceException, ResourceNotFoundException {
        final Consent firstConsent = TestConstants.TEST_CONSENT_WITH_ALL_FIELDS;
        final Consent secondConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS)
            .consentId("SecondConsentId")
            .status(ConsentStatus.REVOKED);
        final Consent thirdConsent = TestUtils.clone(TestConstants.TEST_CONSENT_WITH_ALL_FIELDS)
            .consentId("ThirdConsentId")
            .consentData(Map.of("vendor", "TestVendor"))
            .expiryTime(OffsetDateTime.now())
            .status(ConsentStatus.EXPIRED);

        consentRepository.createServiceUserConsent(firstConsent);
        consentRepository.createServiceUserConsent(secondConsent);
        consentRepository.createServiceUserConsent(thirdConsent);

        final ListServiceUserConsentResponseContent firstPaginatedResponse = activity.handleRequest(TestConstants.TEST_SERVICE_ID,
            TestConstants.TEST_USER_ID, PAGE_LIMIT, null);
        assertNotNull(firstPaginatedResponse);
        assertEquals(List.of(firstConsent, secondConsent), firstPaginatedResponse.getData());
        assertEquals("2", firstPaginatedResponse.getNextPageToken());

        final ListServiceUserConsentResponseContent secondPaginatedResponse = activity.handleRequest(TestConstants.TEST_SERVICE_ID,
            TestConstants.TEST_USER_ID, PAGE_LIMIT, firstPaginatedResponse.getNextPageToken());
        assertNotNull(secondPaginatedResponse);
        assertEquals(List.of(thirdConsent), secondPaginatedResponse.getData());
        assertNull(secondPaginatedResponse.getNextPageToken());
    }
}
