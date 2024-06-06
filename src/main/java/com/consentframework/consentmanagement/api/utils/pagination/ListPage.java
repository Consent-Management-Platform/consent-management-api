package com.consentframework.consentmanagement.api.utils.pagination;

import java.util.List;
import java.util.OptionalInt;

/**
 * Representation of a single page from a paginated list.
 *
 * @param resultsOnPage results on the current page
 * @param nextPageToken index of the start of the next page, or empty if no subsequent results
 */
public record ListPage<T>(List<T> resultsOnPage, OptionalInt nextPageToken) {}
