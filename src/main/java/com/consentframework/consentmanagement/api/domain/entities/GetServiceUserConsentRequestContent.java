package com.consentframework.consentmanagement.api.domain.entities;

import com.consentframework.consentmanagement.api.domain.constants.ApiPathParameterName;
import com.consentframework.consentmanagement.api.domain.exceptions.BadRequestException;
import com.consentframework.consentmanagement.api.domain.parsers.ApiPathParameterParser;

/**
 * Encapsulates GetServiceUserConsent API request contents.
 */
public record GetServiceUserConsentRequestContent(
    String serviceId,
    String userId,
    String consentId
) {
    /**
     * Parse GetServiceUserConsentRequest API content from request.
     *
     * @param request API request
     * @return GetServiceUserConsentRequest API request content
     * @throws BadRequestException exception thrown if unable to parse required attributes from request
     */
    public static GetServiceUserConsentRequestContent parseFromRequest(final ApiRequest request) throws BadRequestException {
        final String serviceId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.SERVICE_ID);
        final String userId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.USER_ID);
        final String consentId = ApiPathParameterParser.parsePathParameter(request, ApiPathParameterName.CONSENT_ID);

        return new GetServiceUserConsentRequestContent(serviceId, userId, consentId);
    }
}
