package com.consentframework.consentmanagement.api.utils.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;

class ListPaginatorTest {
    private final ListPaginator<String> testPaginator = new ListPaginator<String>();

    private static final List<String> PRESENT_RESULTS = List.of("a", "b", "c", "d");

    @Test
    void testGetSinglePageWhenNoResults() {
        final List<String> emptyResults = List.of();
        final ListPage<String> pagedResults = testPaginator.getSinglePage(emptyResults, null, null);
        assertEmpty(pagedResults);
    }

    @Test
    void testGetSinglePageWhenPageTokenPastEndOfResults() {
        final ListPage<String> pagedResults = testPaginator.getSinglePage(PRESENT_RESULTS, 10, PRESENT_RESULTS.size());
        assertEmpty(pagedResults);
    }

    @Test
    void testGetSinglePageWhenPageTokenBeforeStartOfResults() {
        final ListPage<String> pagedResults = testPaginator.getSinglePage(PRESENT_RESULTS, 10, -1);
        assertEmpty(pagedResults);
    }

    @Test
    void testGetSinglePageWithLimitWithoutPageToken() {
        final Integer limit = 2;
        final ListPage<String> pagedResults = testPaginator.getSinglePage(PRESENT_RESULTS, 2, null);
        assertNotNull(pagedResults);
        assertEquals(List.of("a", "b"), pagedResults.resultsOnPage());
        assertNextPageTokenEquals(limit, pagedResults.nextPageToken());
    }

    @Test
    void testGetSinglePageWithPageTokenWithoutLimit() {
        final Integer pageToken = 2;
        final ListPage<String> pagedResults = testPaginator.getSinglePage(PRESENT_RESULTS, null, pageToken);
        assertNotNull(pagedResults);
        assertEquals(List.of("c", "d"), pagedResults.resultsOnPage());
        assertEmpty(pagedResults.nextPageToken());
    }

    @Test
    void testGetSinglePageWithLimitAndPageToken() {
        final Integer limit = 2;
        final Integer pageToken = 1;
        final ListPage<String> pagedResults = testPaginator.getSinglePage(PRESENT_RESULTS, limit, pageToken);
        assertNotNull(pagedResults);
        assertEquals(List.of("b", "c"), pagedResults.resultsOnPage());
        assertNextPageTokenEquals(3, pagedResults.nextPageToken());
    }

    @Test
    void testGetSinglePageWithoutLimitOrPageToken() {
        final ListPage<String> pagedResults = testPaginator.getSinglePage(PRESENT_RESULTS, null, null);
        assertNotNull(pagedResults);
        assertEquals(PRESENT_RESULTS, pagedResults.resultsOnPage());
        assertEmpty(pagedResults.nextPageToken());
    }

    private void assertEmpty(final ListPage<String> pagedResults) {
        assertNotNull(pagedResults);
        assertNotNull(pagedResults.resultsOnPage());
        assertTrue(pagedResults.resultsOnPage().isEmpty());
        assertEmpty(pagedResults.nextPageToken());
    }

    private void assertEmpty(final OptionalInt nextPageToken) {
        assertTrue(nextPageToken.isEmpty(),
            String.format("Expected nextPageToken to be empty but was %s", nextPageToken.toString()));
    }

    private void assertNextPageTokenEquals(final int expectedIntValue, final OptionalInt nextPageToken) {
        assertNotNull(nextPageToken);
        assertEquals(expectedIntValue, nextPageToken.getAsInt(),
            String.format("Expected nextPageToken to be %d but was %s", expectedIntValue, nextPageToken));
    }
}
