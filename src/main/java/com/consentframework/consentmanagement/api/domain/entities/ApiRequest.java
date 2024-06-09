package com.consentframework.consentmanagement.api.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Representation of an API request.
 *
 * @param httpMethod HTTP method, eg. "GET" or "POST"
 * @param path HTTP path, eg. "/my/path"
 * @param pathParameters path parameter mappings, eg. a "/service/{serviceId}" path
 *     value of "/service/Service1234" maps "serviceId" to "Service1234"
 * @param queryStringParameters query string parameters, eg. "/my/path?limit=5" maps "limit" to 5
 * @param headers request headers, eg. "content-type" mapped to "application/json"
 * @param isBase64Encoded whether request data is Base 64 encoded
 * @param body request body
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiRequest(
    String httpMethod,
    String path,
    Map<String, String> pathParameters,
    Map<String, Object> queryStringParameters,
    Map<String, Object> headers,
    boolean isBase64Encoded,
    String body
) {}
