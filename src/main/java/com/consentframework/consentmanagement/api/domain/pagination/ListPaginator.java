package com.consentframework.consentmanagement.api.domain.pagination;

import java.util.List;
import java.util.OptionalInt;

/**
 * Encapsulates logic for retrieving paginated results from a list.
 */
public class ListPaginator<T> {
    /**
     * Retrieve a single page of results given the full results and input pagination settings.
     *
     * @param allResults full set of results from which to retrieve a page
     * @param limit optional limit on the maximum number of results to retrieve
     * @param pageToken optional page token indicating the starting index to return results from
     * @return page of results with next page token if there are additional results
     */
    public ListPage<T> getSinglePage(final List<T> allResults, final Integer limit, final Integer pageToken) {
        if (allResults == null || allResults.isEmpty() || !isPageTokenInBounds(allResults, pageToken)) {
            return new ListPage<T>(List.of(), OptionalInt.empty());
        }

        final int startIndex = (pageToken == null) ? 0 : pageToken;
        final int endIndex = (limit == null)
            ? allResults.size()
            : Math.min(startIndex + limit, allResults.size());

        final List<T> pageOfResults = allResults.subList(startIndex, endIndex);
        final OptionalInt nextPageToken = (endIndex >= allResults.size()) ? OptionalInt.empty() : OptionalInt.of(endIndex);
        return new ListPage<T>(pageOfResults, nextPageToken);
    }

    private boolean isPageTokenInBounds(final List<T> allResults, final Integer pageToken) {
        if (pageToken == null) {
            return true;
        }
        return pageToken >= 0 && pageToken < allResults.size();
    }
}
