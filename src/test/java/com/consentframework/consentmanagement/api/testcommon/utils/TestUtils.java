package com.consentframework.consentmanagement.api.testcommon.utils;

import com.consentframework.consentmanagement.api.JSON;
import com.consentframework.consentmanagement.api.models.Consent;
import com.consentframework.consentmanagement.api.models.CreateServiceUserConsentRequestContent;
import com.consentframework.consentmanagement.api.models.UpdateServiceUserConsentRequestContent;
import com.fasterxml.jackson.core.JsonProcessingException;

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
            .consentType(consent.getConsentType())
            .consentData(consent.getConsentData() == null ? null : Map.copyOf(consent.getConsentData()))
            .status(consent.getStatus())
            .expiryTime(consent.getExpiryTime());
    }

    /**
     * Return UpdateServiceUserConsent request content string using input Consent.
     *
     * @param consent input consent data
     * @return UpdateServiceUserConsent request content as string
     * @throws JsonProcessingException exception thrown if unable to parse consent data
     */
    public static String toUpdateRequestContentString(final Consent consent) throws JsonProcessingException {
        final UpdateServiceUserConsentRequestContent requestContent = new UpdateServiceUserConsentRequestContent()
            .consentVersion(consent.getConsentVersion())
            .consentType(consent.getConsentType())
            .consentData(consent.getConsentData())
            .status(consent.getStatus())
            .expiryTime(consent.getExpiryTime());
        return new JSON().getMapper().writeValueAsString(requestContent);
    }

    /**
     * Return CreateServiceUserConsent request content string using input Consent.
     *
     * @param consent input consent data
     * @return CreateServiceUserConsent request content as string
     * @throws JsonProcessingException exception thrown if unable to parse consent data
     */
    public static String toCreateRequestContentString(final Consent consent) throws JsonProcessingException {
        final CreateServiceUserConsentRequestContent requestContent = toCreateRequestContent(consent);
        return toString(requestContent);
    }

    /**
     * Return CreateServiceUserConsent request content using input Consent.
     *
     * @param consent input consent data
     * @return CreateServiceUserConsent request content
     */
    public static CreateServiceUserConsentRequestContent toCreateRequestContent(final Consent consent) {
        return new CreateServiceUserConsentRequestContent()
            .consentType(consent.getConsentType())
            .consentData(consent.getConsentData())
            .status(consent.getStatus())
            .expiryTime(consent.getExpiryTime());
    }

    /**
     * Serialize CreateServiceUserConsentRequestContent into a string.
     *
     * @param requestContent input request content
     * @return request content as string
     * @throws JsonProcessingException exception thrown if unable to serialize input
     */
    public static String toString(final CreateServiceUserConsentRequestContent requestContent) throws JsonProcessingException {
        return new JSON().getMapper().writeValueAsString(requestContent);
    }
}
