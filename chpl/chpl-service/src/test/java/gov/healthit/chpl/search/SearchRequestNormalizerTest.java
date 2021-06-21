package gov.healthit.chpl.search;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.search.domain.SearchRequest;
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
    public void normalize_certificationEditions_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationEditions(Stream.of("2011 ", " 2015 ", "", " ", null, "2014").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getCertificationEditions().size());
        assertTrue(searchRequest.getCertificationEditions().contains("2011"));
        assertTrue(searchRequest.getCertificationEditions().contains("2015"));
        assertTrue(searchRequest.getCertificationEditions().contains("2014"));
    }

    @Test
    public void normalize_certificationCriteriaIdStrings_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationCriteriaIdStrings(Stream.of("1 ", " 2 ", "", " ", null, "3", "notanumber").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getCertificationCriteriaIds().size());
        assertTrue(searchRequest.getCertificationCriteriaIds().contains(1L));
        assertTrue(searchRequest.getCertificationCriteriaIds().contains(2L));
        assertTrue(searchRequest.getCertificationCriteriaIds().contains(3L));
    }

    @Test
    public void normalize_certificationCriteriaIdLongs_noChange() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationCriteriaIds(Stream.of(1L, 2L, 3L).collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getCertificationCriteriaIds().size());
        assertTrue(searchRequest.getCertificationCriteriaIds().contains(1L));
        assertTrue(searchRequest.getCertificationCriteriaIds().contains(2L));
        assertTrue(searchRequest.getCertificationCriteriaIds().contains(3L));
    }

    @Test
    public void normalize_certificationCriteriaOperatorStringValid_resolvesCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationCriteriaOperatorString("AND")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(SearchSetOperator.AND, searchRequest.getCertificationCriteriaOperator());
    }

    @Test
    public void normalize_certificationCriteriaOperator_noChanges() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationCriteriaOperator(SearchSetOperator.AND)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(SearchSetOperator.AND, searchRequest.getCertificationCriteriaOperator());
    }

    @Test
    public void normalize_certificationCriteriaOperatorStringInvalid_setsFieldNull() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationCriteriaOperatorString("XOR")
                .build();
        normalizer.normalize(searchRequest);

        assertNull(searchRequest.getCertificationCriteriaOperator());
    }

    @Test
    public void normalize_cqms_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .cqms(Stream.of("CMS1 ", " CMS2 ", "", " ", null, "CMS3").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getCqms().size());
        assertTrue(searchRequest.getCqms().contains("CMS1"));
        assertTrue(searchRequest.getCqms().contains("CMS2"));
        assertTrue(searchRequest.getCqms().contains("CMS3"));
    }

    @Test
    public void normalize_cqmsOperatorStringValid_resolvesCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .cqmsOperatorString("AND")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(SearchSetOperator.AND, searchRequest.getCqmsOperator());
    }

    @Test
    public void normalize_cqmsOperator_noChanges() {
        SearchRequest searchRequest = SearchRequest.builder()
                .cqmsOperator(SearchSetOperator.AND)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(SearchSetOperator.AND, searchRequest.getCqmsOperator());
    }

    @Test
    public void normalize_cqmsOperatorStringInvalid_setsFieldNull() {
        SearchRequest searchRequest = SearchRequest.builder()
                .cqmsOperatorString("XOR")
                .build();
        normalizer.normalize(searchRequest);

        assertNull(searchRequest.getCqmsOperator());
    }

    @Test
    public void normalize_certificationStatuses_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationStatuses(Stream.of("Active ", " Withdrawn ", "", " ", null, "Suspended").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getCertificationStatuses().size());
        assertTrue(searchRequest.getCertificationStatuses().contains("Active"));
        assertTrue(searchRequest.getCertificationStatuses().contains("Withdrawn"));
        assertTrue(searchRequest.getCertificationStatuses().contains("Suspended"));
    }

    @Test
    public void normalize_practiceTypeNull_noEffect() {
        SearchRequest searchRequest = SearchRequest.builder()
                .practiceType(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getPracticeType());
    }

    @Test
    public void normalize_practiceTypeWhitespace_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .practiceType(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getPracticeType());
    }

    @Test
    public void normalize_practiceTypeWhitespaceAround_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .practiceType(" Inpatient ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("Inpatient", searchRequest.getPracticeType());
    }

    @Test
    public void normalize_certificationDateStartNull_noEffect() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationDateStart(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getCertificationDateStart());
    }

    @Test
    public void normalize_certificationDateStartWhitespace_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationDateStart(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getCertificationDateStart());
    }

    @Test
    public void normalize_certificationDateStartWhitespaceAround_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationDateStart(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getCertificationDateStart());
    }

    @Test
    public void normalize_certificationDateEndNull_noEffect() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationDateEnd(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getCertificationDateEnd());
    }

    @Test
    public void normalize_certificationDateEndWhitespace_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationDateEnd(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getCertificationDateEnd());
    }

    @Test
    public void normalize_certificationDateEndWhitespaceAround_trimsCorrectly() {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationDateEnd(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getCertificationDateEnd());
    }
}
