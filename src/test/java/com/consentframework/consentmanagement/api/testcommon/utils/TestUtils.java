package com.consentframework.consentmanagement.api.testcommon.utils;

import com.consentframework.consentmanagement.api.models.Consent;

import java.util.Map;

/**
 * Utility class with common methods for test code.
 */
public final class TestUtils {
    /**
     * Clone an input Consent object.
     *
     * @param consent Consent to be cloned
     * @return new Consent object with same attribute values as input
     */
    public static Consent clone(final Consent consent) {
        return new Consent()
            .serviceId(consent.getServiceId())
            .userId(consent.getUserId())
            .consentId(consent.getConsentId())
            .consentVersion(consent.getConsentVersion())
            .consentData(consent.getConsentData() == null ? null : Map.copyOf(consent.getConsentData()))
            .status(consent.getStatus())
            .expiryTime(consent.getExpiryTime());
    }
}
