package gov.healthit.chpl.questionableactivity.search;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.RwtSearchOptions;
import gov.healthit.chpl.search.domain.SearchSetOperator;


public class SearchRequestNormalizerTest {

    private SearchRequestNormalizer normalizer;

    @Before
    public void setup() {
        normalizer = new SearchRequestNormalizer();
    }

    @Test
    public void normalize_emptySearchRequest_doesNotFail() {
        SearchRequest searchRequest = SearchRequest.builder().build();
        normalizer.normalize(searchRequest);
        assertNotNull(searchRequest);
    }

    @Test
    public void normalize_searchTerm_removesExtraSpaces() {
        SearchRequest searchRequest = SearchRequest.builder()
                .searchTerm("  search term   test ")
                .build();
        normalizer.normalize(searchRequest);
        assertEquals("search term test", searchRequest.getSearchTerm());
    }

    @Test
    public void normalize_triggerIdStrings_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .triggerIdStrings(Stream.of("1 ", " 2 ", "", " ", null, "3", "notanumber").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getTriggerIds().size());
        assertTrue(searchRequest.getTriggerIds().contains(1L));
        assertTrue(searchRequest.getTriggerIds().contains(2L));
        assertTrue(searchRequest.getTriggerIds().contains(3L));
    }

    @Test
    public void normalize_triggerIdLongs_noChange() {
        SearchRequest searchRequest = SearchRequest.builder()
                .triggerIds(Stream.of(1L, 2L, 3L).collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getTriggerIds().size());
        assertTrue(searchRequest.getTriggerIds().contains(1L));
        assertTrue(searchRequest.getTriggerIds().contains(2L));
        assertTrue(searchRequest.getTriggerIds().contains(3L));
    }


    @Test
    public void normalize_activityDateStartNull_noEffect() {
        SearchRequest searchRequest = SearchRequest.builder()
                .activityDateStart(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getActivityDateStart());
    }

    @Test
    public void normalize_activityDateStartWhitespace_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .activityDateStart(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getActivityDateStart());
    }

    @Test
    public void normalize_activityDateStartWhitespaceAround_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .activityDateStart(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getActivityDateStart());
    }

    @Test
    public void normalize_activityDateEndNull_noEffect() {
        SearchRequest searchRequest = SearchRequest.builder()
                .activityDateEnd(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getActivityDateEnd());
    }

    @Test
    public void normalize_activityDateEndWhitespace_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .activityDateEnd(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getActivityDateEnd());
    }

    @Test
    public void normalize_activityDateEndWhitespaceAround_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .activityDateEnd(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getActivityDateEnd());
    }

    @Test
    public void normalize_orderByStringValid_resolvesCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .orderByString("PRODUCT")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.PRODUCT, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByStringLowercase_resolvesCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .orderByString("product")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.PRODUCT, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByProduct_noChanges() {
        SearchRequest searchRequest = SearchRequest.builder()
                .orderBy(OrderByOption.PRODUCT)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.PRODUCT, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByStringInvalid_setsFieldNull() {
        SearchRequest searchRequest = SearchRequest.builder()
                .orderByString("NOTVALID")
                .build();
        normalizer.normalize(searchRequest);

        assertNull(searchRequest.getOrderBy());
    }
}
