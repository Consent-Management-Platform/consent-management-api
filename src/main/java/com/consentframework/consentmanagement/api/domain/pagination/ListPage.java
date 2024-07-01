package com.consentframework.consentmanagement.api.domain.pagination;

import java.util.List;
import java.util.Optional;

/**
 * Representation of a single page from a paginated list.
 *
 * @param resultsOnPage results on the current page
 * @param nextPageToken index of the start of the next page, or empty if no subsequent results
 */
public record ListPage<T>(List<T> resultsOnPage, Optional<String> nextPageToken) {}
