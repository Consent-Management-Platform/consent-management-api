package com.consentframework.consentmanagement.api.domain.requesthandlers;

import com.consentframework.consentmanagement.api.domain.entities.ApiRequest;

import java.util.Map;

/**
 * Interface for an API request handler.
 *
 * @param <T> the type of context
 */
public interface ApiRequestHandler<T> {
    /**
     * Handle API request.
     *
     * @param request API request object
     * @param context request context
     * @return API response
     */
    Map<String, Object> handleRequest(final ApiRequest request, final T context);
}
