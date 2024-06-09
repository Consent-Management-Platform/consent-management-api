package com.consentframework.consentmanagement.api.domain.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Representation of API exception response content.
 *
 * @param message exception message
 */
@JsonPropertyOrder({"message"})
public record ApiExceptionResponseContent(
    @JsonProperty("message")
    @JsonInclude(Include.USE_DEFAULTS)
    String message
) {}
