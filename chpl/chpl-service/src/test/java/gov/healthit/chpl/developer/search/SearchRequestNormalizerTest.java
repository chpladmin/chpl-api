package gov.healthit.chpl.developer.search;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

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
    public void normalize_searchTerm_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .searchTerm("   test ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("test", searchRequest.getSearchTerm());
    }

    @Test
    public void normalize_developerName_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .developerName("   test ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("test", searchRequest.getDeveloperName());
    }

    @Test
    public void normalize_developerCode_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .developerCode("   0123 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("0123", searchRequest.getDeveloperCode());
    }

    @Test
    public void normalize_certificationBodies_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationBodies(Stream.of("ICSA Labs ", " Drummond ", "", " ", null, "test").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getCertificationBodies().size());
        assertTrue(searchRequest.getCertificationBodies().contains("ICSA Labs"));
        assertTrue(searchRequest.getCertificationBodies().contains("Drummond"));
        assertTrue(searchRequest.getCertificationBodies().contains("test"));
    }

    @Test
    public void normalize_statuses_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .statuses(Stream.of("Active ", " Under certification ban ", "", " ", null, "Suspended").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getStatuses().size());
        assertTrue(searchRequest.getStatuses().contains("Active"));
        assertTrue(searchRequest.getStatuses().contains("Under certification ban"));
        assertTrue(searchRequest.getStatuses().contains("Suspended"));
    }

    @Test
    public void normalize_decertificationDateStartNull_noEffect() {
        SearchRequest searchRequest = SearchRequest.builder()
                .decertificationDateStart(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getDecertificationDateStart());
    }

    @Test
    public void normalize_decertificationDateStartWhitespace_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .decertificationDateStart(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getDecertificationDateStart());
    }

    @Test
    public void normalize_decertificationDateStartWhitespaceAround_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .decertificationDateStart(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getDecertificationDateStart());
    }

    @Test
    public void normalize_decertificationDateEndNull_noEffect() {
        SearchRequest searchRequest = SearchRequest.builder()
                .decertificationDateEnd(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getDecertificationDateEnd());
    }

    @Test
    public void normalize_decertificationDateEndWhitespace_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .decertificationDateEnd(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getDecertificationDateEnd());
    }

    @Test
    public void normalize_decertificationDateEndWhitespaceAround_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .decertificationDateEnd(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getDecertificationDateEnd());
    }

    @Test
    public void normalize_orderByStringValid_resolvesCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .orderByString("DEVELOPER")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.DEVELOPER, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByStringLowercase_resolvesCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .orderByString("developer")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.DEVELOPER, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByDeveloper_noChanges() {
        SearchRequest searchRequest = SearchRequest.builder()
                .orderBy(OrderByOption.DEVELOPER)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.DEVELOPER, searchRequest.getOrderBy());
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
