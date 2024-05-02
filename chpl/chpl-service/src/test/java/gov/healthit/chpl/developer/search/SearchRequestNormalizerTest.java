package gov.healthit.chpl.developer.search;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.search.domain.SearchSetOperator;

public class SearchRequestNormalizerTest {

    private SearchRequestNormalizer normalizer;

    @Before
    public void setup() {
        normalizer = new SearchRequestNormalizer();
    }

    @Test
    public void normalize_emptySearchRequest_doesNotFail() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder().build();
        normalizer.normalize(searchRequest);
        assertNotNull(searchRequest);
    }

    @Test
    public void normalize_searchTerm_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .searchTerm("   test ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("test", searchRequest.getSearchTerm());
    }

    @Test
    public void normalize_developerName_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .developerName("   test ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("test", searchRequest.getDeveloperName());
    }

    @Test
    public void normalize_developerCode_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .developerCode("   0123 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("0123", searchRequest.getDeveloperCode());
    }

    @Test
    public void normalize_anyCertificationBodies_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .acbsForAllListings(Stream.of("ICSA Labs ", " Drummond ", "", " ", null, "test").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getAcbsForAllListings().size());
        assertTrue(searchRequest.getAcbsForAllListings().contains("ICSA Labs"));
        assertTrue(searchRequest.getAcbsForAllListings().contains("Drummond"));
        assertTrue(searchRequest.getAcbsForAllListings().contains("test"));
    }

    @Test
    public void normalize_activeCertificationBodies_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .acbsForActiveListings(Stream.of("ICSA Labs ", " Drummond ", "", " ", null, "test").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getAcbsForActiveListings().size());
        assertTrue(searchRequest.getAcbsForActiveListings().contains("ICSA Labs"));
        assertTrue(searchRequest.getAcbsForActiveListings().contains("Drummond"));
        assertTrue(searchRequest.getAcbsForActiveListings().contains("test"));
    }

    @Test
    public void normalize_activeListingsStringsFilter_setsActiveListingsOptionsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .activeListingsOptionsStrings(Stream.of("HAS_ANY_ACTIVE ", " HAS_NO_ACTIVE ", "", " ", null, "HAD_ANY_ACTIVE_DURING_MOST_RECENT_PAST_ATTESTATION_PERIOD").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(3, searchRequest.getActiveListingsOptions().size());
        assertTrue(searchRequest.getActiveListingsOptions().contains(ActiveListingSearchOptions.HAS_ANY_ACTIVE));
        assertTrue(searchRequest.getActiveListingsOptions().contains(ActiveListingSearchOptions.HAS_NO_ACTIVE));
        assertTrue(searchRequest.getActiveListingsOptions().contains(ActiveListingSearchOptions.HAD_ANY_ACTIVE_DURING_MOST_RECENT_PAST_ATTESTATION_PERIOD));
    }

    @Test
    public void normalize_activeListingsOperatorStringAnd_setsActiveListingsOperatorCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .activeListingsOptionsOperatorString("AND")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(SearchSetOperator.AND, searchRequest.getActiveListingsOptionsOperator());
    }

    @Test
    public void normalize_activeListingsOperatorStringOr_setsActiveListingsOperatorCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .activeListingsOptionsOperatorString("OR")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(SearchSetOperator.OR, searchRequest.getActiveListingsOptionsOperator());
    }

    @Test
    public void normalize_activeListingsOperatorStringXor_setsActiveListingsOperatorNull() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .activeListingsOptionsOperatorString("XOR")
                .build();
        normalizer.normalize(searchRequest);

        assertNull(searchRequest.getActiveListingsOptionsOperator());
    }

    @Test
    public void normalize_attestationsStringsFilter_setsAttestationsOptionsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .attestationsOptionsStrings(Stream.of("HAS_SUBMITTED ", " HAS_NOT_PUBLISHED ", "", " ",
                        null, "HAS_NOT_SUBMITTED", "  HAS_PUBLISHED   ").collect(Collectors.toSet()))
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(4, searchRequest.getAttestationsOptions().size());
        assertTrue(searchRequest.getAttestationsOptions().contains(AttestationsSearchOptions.HAS_SUBMITTED));
        assertTrue(searchRequest.getAttestationsOptions().contains(AttestationsSearchOptions.HAS_NOT_PUBLISHED));
        assertTrue(searchRequest.getAttestationsOptions().contains(AttestationsSearchOptions.HAS_NOT_SUBMITTED));
        assertTrue(searchRequest.getAttestationsOptions().contains(AttestationsSearchOptions.HAS_PUBLISHED));
    }

    @Test
    public void normalize_attestationsOperatorStringAnd_setsAttestationsOperatorCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .attestationsOptionsOperatorString("AND")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(SearchSetOperator.AND, searchRequest.getAttestationsOptionsOperator());
    }

    @Test
    public void normalize_attestationsOperatorStringOr_setsAttestationsOperatorCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .attestationsOptionsOperatorString("OR")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(SearchSetOperator.OR, searchRequest.getAttestationsOptionsOperator());
    }

    @Test
    public void normalize_attestationsOperatorStringXor_setsAttestationsOperatorNull() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .attestationsOptionsOperatorString("XOR")
                .build();
        normalizer.normalize(searchRequest);

        assertNull(searchRequest.getAttestationsOptionsOperator());
    }

    @Test
    public void normalize_statuses_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
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
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .decertificationDateStart(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getDecertificationDateStart());
    }

    @Test
    public void normalize_decertificationDateStartWhitespace_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .decertificationDateStart(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getDecertificationDateStart());
    }

    @Test
    public void normalize_decertificationDateStartWhitespaceAround_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .decertificationDateStart(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getDecertificationDateStart());
    }

    @Test
    public void normalize_decertificationDateEndNull_noEffect() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .decertificationDateEnd(null)
                .build();
        normalizer.normalize(searchRequest);

        assertNull(null, searchRequest.getDecertificationDateEnd());
    }

    @Test
    public void normalize_decertificationDateEndWhitespace_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .decertificationDateEnd(" ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("", searchRequest.getDecertificationDateEnd());
    }

    @Test
    public void normalize_decertificationDateEndWhitespaceAround_trimsCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .decertificationDateEnd(" 2015-01-01 ")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals("2015-01-01", searchRequest.getDecertificationDateEnd());
    }

    @Test
    public void normalize_orderByStringValid_resolvesCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .orderByString("DEVELOPER")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.DEVELOPER, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByStringLowercase_resolvesCorrectly() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .orderByString("developer")
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.DEVELOPER, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByDeveloper_noChanges() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .orderBy(OrderByOption.DEVELOPER)
                .build();
        normalizer.normalize(searchRequest);

        assertEquals(OrderByOption.DEVELOPER, searchRequest.getOrderBy());
    }

    @Test
    public void normalize_orderByStringInvalid_setsFieldNull() {
        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .orderByString("NOTVALID")
                .build();
        normalizer.normalize(searchRequest);

        assertNull(searchRequest.getOrderBy());
    }

}
