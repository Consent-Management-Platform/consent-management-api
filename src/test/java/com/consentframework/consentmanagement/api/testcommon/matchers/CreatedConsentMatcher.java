package com.consentframework.consentmanagement.api.testcommon.matchers;

import com.consentframework.consentmanagement.api.models.Consent;
import org.mockito.ArgumentMatcher;

/**
 * Argument matcher for created Consent objects.
 */
public class CreatedConsentMatcher implements ArgumentMatcher<Consent> {
    private Consent expectedConsent;

    /**
     * Construct CreatedConsentMatcher with reference Consent.
     *
     * @param expectedConsent consent that other matcher inputs should be compared against
     */
    public CreatedConsentMatcher(final Consent expectedConsent) {
        this.expectedConsent = expectedConsent;
    }

    /**
     * Return true if input matches the expected consent content.
     *
     * Skips non-deterministic attributes such as consentId, which
     * is a randomly generated UUID.
     */
    @Override
    public boolean matches(final Consent actualConsent) {
        return expectedConsent.getServiceId().equals(actualConsent.getServiceId())
            && expectedConsent.getUserId().equals(actualConsent.getUserId())
            && expectedConsent.getConsentVersion().equals(actualConsent.getConsentVersion())
            && expectedConsent.getConsentType().equals(actualConsent.getConsentType())
            && expectedConsent.getConsentData().equals(actualConsent.getConsentData())
            && expectedConsent.getStatus().equals(actualConsent.getStatus())
            && expectedConsent.getExpiryTime().equals(actualConsent.getExpiryTime());
    }
}
