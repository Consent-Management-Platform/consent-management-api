package com.consentframework.consentmanagement.api.usecases.requesthandlers;

import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;

import java.util.Map;

/**
 * Interface for an API request handler.
 */
public interface ApiRequestHandler {
    /**
     * Handle API request.
     *
     * @param request API request object
     * @return API response
     */
    Map<String, Object> handleRequest(final ApiRequest request);
}
