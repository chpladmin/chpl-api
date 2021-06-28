package gov.healthit.chpl.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.domain.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.OrderByOption;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchResponse;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.service.DirectReviewSearchService;

public class ListingSearchServiceTest {

    private CertifiedProductSearchManager cpSearchManager;
    private ListingSearchService listingSearchService;

    @Before
    public void setup() {
        SearchRequestValidator searchRequestValidator = Mockito.mock(SearchRequestValidator.class);
        DirectReviewSearchService drService = Mockito.mock(DirectReviewSearchService.class);
        Mockito.when(drService.getDirectReviewsAvailable()).thenReturn(true);
        cpSearchManager = Mockito.mock(CertifiedProductSearchManager.class);

        listingSearchService = new ListingSearchService(searchRequestValidator, cpSearchManager, drService);
    }

    @Test
    public void search_validEmptySearchRequest_findsAllListings() throws ValidationException {
        Mockito.when(cpSearchManager.getSearchListingCollection())
            .thenReturn(createBasicSearchResultCollection(100));
        SearchRequest searchRequest = SearchRequest.builder()
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(10, searchResponse.getResults().size());
    }

    @Test
    public void search_pageOutOfRangeSearchRequest_returnsEmptyResponse() throws ValidationException {
        Mockito.when(cpSearchManager.getSearchListingCollection())
            .thenReturn(createBasicSearchResultCollection(100));
        SearchRequest searchRequest = SearchRequest.builder()
            .pageNumber(2)
            .pageSize(100)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_noListingsWithSearchTerm_returnsEmptyResponse() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(1).setDeveloper("another name");
        allListings.get(2).setProduct("test");
        allListings.get(3).setVersion("test");
        allListings.get(4).setChplProductNumber("15.02.02.3007.A056.01.00.0.180214");
        allListings.get(5).setAcbCertificationId("12345");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("search term")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_sortByEditionAscending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setEdition("2015");
        allListings.get(1).setEdition("2011");
        allListings.get(2).setEdition("2014");
        allListings.get(3).setEdition("2015");
        allListings.get(4).setEdition("2011");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.EDITION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("2011", searchResponse.getResults().get(0).getEdition());
        assertEquals("2011", searchResponse.getResults().get(1).getEdition());
        assertEquals("2014", searchResponse.getResults().get(2).getEdition());
        assertEquals("2015", searchResponse.getResults().get(3).getEdition());
        assertEquals("2015", searchResponse.getResults().get(4).getEdition());
    }

    @Test
    public void search_sortByEditionDescending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setEdition("2015");
        allListings.get(1).setEdition("2011");
        allListings.get(2).setEdition("2014");
        allListings.get(3).setEdition("2015");
        allListings.get(4).setEdition("2011");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.EDITION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("2015", searchResponse.getResults().get(0).getEdition());
        assertEquals("2015", searchResponse.getResults().get(1).getEdition());
        assertEquals("2014", searchResponse.getResults().get(2).getEdition());
        assertEquals("2011", searchResponse.getResults().get(3).getEdition());
        assertEquals("2011", searchResponse.getResults().get(4).getEdition());
    }

    @Test
    public void search_sortByDeveloperAscending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setDeveloper("z");
        allListings.get(1).setDeveloper("b");
        allListings.get(2).setDeveloper("d");
        allListings.get(3).setDeveloper("f");
        allListings.get(4).setDeveloper("y");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getDeveloper());
        assertEquals("d", searchResponse.getResults().get(1).getDeveloper());
        assertEquals("f", searchResponse.getResults().get(2).getDeveloper());
        assertEquals("y", searchResponse.getResults().get(3).getDeveloper());
        assertEquals("z", searchResponse.getResults().get(4).getDeveloper());
    }

    @Test
    public void search_sortByDeveloperDescending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setDeveloper("z");
        allListings.get(1).setDeveloper("b");
        allListings.get(2).setDeveloper("d");
        allListings.get(3).setDeveloper("f");
        allListings.get(4).setDeveloper("y");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getDeveloper());
        assertEquals("y", searchResponse.getResults().get(1).getDeveloper());
        assertEquals("f", searchResponse.getResults().get(2).getDeveloper());
        assertEquals("d", searchResponse.getResults().get(3).getDeveloper());
        assertEquals("b", searchResponse.getResults().get(4).getDeveloper());
    }

    @Test
    public void search_sortByProductAscending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setProduct("z");
        allListings.get(1).setProduct("b");
        allListings.get(2).setProduct("d");
        allListings.get(3).setProduct("f");
        allListings.get(4).setProduct("y");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.PRODUCT)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getProduct());
        assertEquals("d", searchResponse.getResults().get(1).getProduct());
        assertEquals("f", searchResponse.getResults().get(2).getProduct());
        assertEquals("y", searchResponse.getResults().get(3).getProduct());
        assertEquals("z", searchResponse.getResults().get(4).getProduct());
    }

    @Test
    public void search_sortByProductDescending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setProduct("z");
        allListings.get(1).setProduct("b");
        allListings.get(2).setProduct("d");
        allListings.get(3).setProduct("f");
        allListings.get(4).setProduct("y");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.PRODUCT)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getProduct());
        assertEquals("y", searchResponse.getResults().get(1).getProduct());
        assertEquals("f", searchResponse.getResults().get(2).getProduct());
        assertEquals("d", searchResponse.getResults().get(3).getProduct());
        assertEquals("b", searchResponse.getResults().get(4).getProduct());
    }

    @Test
    public void search_sortByVersionAscending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setVersion("z");
        allListings.get(1).setVersion("b");
        allListings.get(2).setVersion("d");
        allListings.get(3).setVersion("f");
        allListings.get(4).setVersion("y");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.VERSION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getVersion());
        assertEquals("d", searchResponse.getResults().get(1).getVersion());
        assertEquals("f", searchResponse.getResults().get(2).getVersion());
        assertEquals("y", searchResponse.getResults().get(3).getVersion());
        assertEquals("z", searchResponse.getResults().get(4).getVersion());
    }

    @Test
    public void search_sortByVersionDescending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setVersion("z");
        allListings.get(1).setVersion("b");
        allListings.get(2).setVersion("d");
        allListings.get(3).setVersion("f");
        allListings.get(4).setVersion("y");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.VERSION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getVersion());
        assertEquals("y", searchResponse.getResults().get(1).getVersion());
        assertEquals("f", searchResponse.getResults().get(2).getVersion());
        assertEquals("d", searchResponse.getResults().get(3).getVersion());
        assertEquals("b", searchResponse.getResults().get(4).getVersion());
    }

    @Test
    public void search_sortByCertificationDateDescending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setCertificationDate(0L);
        allListings.get(1).setCertificationDate(100L);
        allListings.get(2).setCertificationDate(50L);
        allListings.get(3).setCertificationDate(5000L);
        allListings.get(4).setCertificationDate(3L);
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CERTIFICATION_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(5000L, searchResponse.getResults().get(0).getCertificationDate());
        assertEquals(100L, searchResponse.getResults().get(1).getCertificationDate());
        assertEquals(50L, searchResponse.getResults().get(2).getCertificationDate());
        assertEquals(3L, searchResponse.getResults().get(3).getCertificationDate());
        assertEquals(0L, searchResponse.getResults().get(4).getCertificationDate());
    }

    @Test
    public void search_sortByCertificationDateAscending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setCertificationDate(0L);
        allListings.get(1).setCertificationDate(100L);
        allListings.get(2).setCertificationDate(50L);
        allListings.get(3).setCertificationDate(5000L);
        allListings.get(4).setCertificationDate(3L);
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CERTIFICATION_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(0L, searchResponse.getResults().get(0).getCertificationDate());
        assertEquals(3L, searchResponse.getResults().get(1).getCertificationDate());
        assertEquals(50L, searchResponse.getResults().get(2).getCertificationDate());
        assertEquals(100L, searchResponse.getResults().get(3).getCertificationDate());
        assertEquals(5000L, searchResponse.getResults().get(4).getCertificationDate());
    }

    @Test
    public void search_sortByChplIdAscending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setChplProductNumber("CHP-12345");
        allListings.get(1).setChplProductNumber("15.04.04.1234.PROD.11.1.01.123456");
        allListings.get(2).setChplProductNumber("CHP-23456");
        allListings.get(3).setChplProductNumber("14.04.04.1234.PROD.11.1.01.123456");
        allListings.get(4).setChplProductNumber("15.99.04.3078.Ninj.01.00.0.200629");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CHPL_ID)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("14.04.04.1234.PROD.11.1.01.123456", searchResponse.getResults().get(0).getChplProductNumber());
        assertEquals("15.04.04.1234.PROD.11.1.01.123456", searchResponse.getResults().get(1).getChplProductNumber());
        assertEquals("15.99.04.3078.Ninj.01.00.0.200629", searchResponse.getResults().get(2).getChplProductNumber());
        assertEquals("CHP-12345", searchResponse.getResults().get(3).getChplProductNumber());
        assertEquals("CHP-23456", searchResponse.getResults().get(4).getChplProductNumber());
    }

    @Test
    public void search_sortByChplIdDescending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setChplProductNumber("CHP-12345");
        allListings.get(1).setChplProductNumber("15.04.04.1234.PROD.11.1.01.123456");
        allListings.get(2).setChplProductNumber("CHP-23456");
        allListings.get(3).setChplProductNumber("14.04.04.1234.PROD.11.1.01.123456");
        allListings.get(4).setChplProductNumber("15.99.04.3078.Ninj.01.00.0.200629");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CHPL_ID)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("CHP-23456", searchResponse.getResults().get(0).getChplProductNumber());
        assertEquals("CHP-12345", searchResponse.getResults().get(1).getChplProductNumber());
        assertEquals("15.99.04.3078.Ninj.01.00.0.200629", searchResponse.getResults().get(2).getChplProductNumber());
        assertEquals("15.04.04.1234.PROD.11.1.01.123456", searchResponse.getResults().get(3).getChplProductNumber());
        assertEquals("14.04.04.1234.PROD.11.1.01.123456", searchResponse.getResults().get(4).getChplProductNumber());
    }

    @Test
    public void search_sortByCertificationStatusAscending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setCertificationStatus("Active");
        allListings.get(1).setCertificationStatus("Retired");
        allListings.get(2).setCertificationStatus("Withdrawn by Developer");
        allListings.get(3).setCertificationStatus("Active");
        allListings.get(4).setCertificationStatus("Suspended by ONC-ACB");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("Active", searchResponse.getResults().get(0).getCertificationStatus());
        assertEquals("Active", searchResponse.getResults().get(1).getCertificationStatus());
        assertEquals("Retired", searchResponse.getResults().get(2).getCertificationStatus());
        assertEquals("Suspended by ONC-ACB", searchResponse.getResults().get(3).getCertificationStatus());
        assertEquals("Withdrawn by Developer", searchResponse.getResults().get(4).getCertificationStatus());
    }

    @Test
    public void search_sortByCertificationStatusDescending_ordersResults() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(5);
        allListings.get(0).setCertificationStatus("Active");
        allListings.get(1).setCertificationStatus("Retired");
        allListings.get(2).setCertificationStatus("Withdrawn by Developer");
        allListings.get(3).setCertificationStatus("Active");
        allListings.get(4).setCertificationStatus("Suspended by ONC-ACB");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("Withdrawn by Developer", searchResponse.getResults().get(0).getCertificationStatus());
        assertEquals("Suspended by ONC-ACB", searchResponse.getResults().get(1).getCertificationStatus());
        assertEquals("Retired", searchResponse.getResults().get(2).getCertificationStatus());
        assertEquals("Active", searchResponse.getResults().get(3).getCertificationStatus());
        assertEquals("Active", searchResponse.getResults().get(4).getCertificationStatus());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingDevelopers() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setDeveloper("dev name");
        allListings.get(1).setDeveloper("long DEV name here");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingProducts() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setProduct("product name");
        allListings.get(1).setProduct("long PRODUCT name here");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("product name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingChplProductNumber() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setChplProductNumber("15.02.02.3007.A056.01.00.0.180214");
        allListings.get(1).setChplProductNumber("CHP-123456");
        allListings.get(2).setChplProductNumber("15.02.02.3007.A056.01.00.0.180215");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("15.02.02.3007.A056.01.00.0.18021")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingAcbCertificationId() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setAcbCertificationId("15.02.02.3007.A056.01.00.0.180214");
        allListings.get(1).setAcbCertificationId("CHP-123456");
        allListings.get(2).setAcbCertificationId("15.02.02.3007.A056.01.00.0.180215");
        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("15.02.02.3007.A056.01.00.0.18021")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_singleAcbNameProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setAcb("ACB 1");
        allListings.get(1).setAcb("ACB 2");

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> acbNames = new LinkedHashSet<String>();
        acbNames.add("ACB 1");
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationBodies(acbNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_mutlipleAcbNamesProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setAcb("ACB 1");
        allListings.get(1).setAcb("ACB 2");
        allListings.get(2).setAcb("ACB 1");
        allListings.get(3).setAcb("ACB 5");

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> acbNames = new LinkedHashSet<String>();
        acbNames.add("ACB 1");
        acbNames.add("ACB 2");
        acbNames.add("ACB 3");
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationBodies(acbNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCertificationStatusProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCertificationStatus(CertificationStatusType.Active.getName());
        allListings.get(1).setCertificationStatus(CertificationStatusType.SuspendedByAcb.getName());

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> certificationStatusNames = new LinkedHashSet<String>();
        certificationStatusNames.add(CertificationStatusType.SuspendedByAcb.getName());
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationStatuses(certificationStatusNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCertificationStatusProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCertificationStatus(CertificationStatusType.Active.getName());
        allListings.get(1).setCertificationStatus(CertificationStatusType.SuspendedByAcb.getName());
        allListings.get(2).setCertificationStatus(CertificationStatusType.Active.getName());
        allListings.get(3).setCertificationStatus(CertificationStatusType.SuspendedByOnc.getName());

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> certificationStatusNames = new LinkedHashSet<String>();
        certificationStatusNames.add(CertificationStatusType.SuspendedByAcb.getName());
        certificationStatusNames.add(CertificationStatusType.Active.getName());

        SearchRequest searchRequest = SearchRequest.builder()
            .certificationStatuses(certificationStatusNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCertificationEditionProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setEdition("2014");
        allListings.get(1).setEdition("2014");
        allListings.get(2).setEdition("2011");

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2011");
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCertificationEditionProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setEdition("2014");
        allListings.get(1).setEdition("2014");
        allListings.get(2).setEdition("2011");
        allListings.get(3).setEdition("2011");
        allListings.get(4).setEdition("2015");
        allListings.get(5).setEdition("2015");

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2011");
        editionNames.add("2015");

        SearchRequest searchRequest = SearchRequest.builder()
            .certificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(4, searchResponse.getRecordCount());
        assertEquals(4, searchResponse.getResults().size());
    }

    @Test
    public void search_developerProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setDeveloper("dev name");
        allListings.get(1).setDeveloper("long DEV name here");
        allListings.get(2).setDeveloper("doesn't match");

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .developer("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_productProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setProduct("product name");
        allListings.get(1).setProduct("long PRODUCT name here");
        allListings.get(2).setProduct("doesn't match");

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .product("product name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_versionProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setVersion("version name");
        allListings.get(1).setVersion("long VERSION name here");
        allListings.get(2).setVersion("doesn't match");

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .version("version name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_practiceTypeProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setPracticeType("ambulatory");
        allListings.get(1).setPracticeType("AMbulatory");
        allListings.get(2).setPracticeType("inpatient");

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .practiceType("Ambulatory")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCriterionIdWithAndOperatorProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCriteriaMet(Stream.of(1L, 2L, 3L).collect(Collectors.toSet()));
        allListings.get(1).setCriteriaMet(Stream.of(1L, 2L, 4L).collect(Collectors.toSet()));
        allListings.get(2).setCriteriaMet(Stream.of(5L, 2L, 3L).collect(Collectors.toSet()));

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<Long> criteriaIds = new LinkedHashSet<Long>();
        criteriaIds.add(1L);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationCriteriaIds(criteriaIds)
            .certificationCriteriaOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCriterionIdWithOrOperatorProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCriteriaMet(Stream.of(1L, 2L, 3L).collect(Collectors.toSet()));
        allListings.get(1).setCriteriaMet(Stream.of(1L, 2L, 4L).collect(Collectors.toSet()));
        allListings.get(2).setCriteriaMet(Stream.of(5L, 2L, 3L).collect(Collectors.toSet()));

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<Long> criteriaIds = new LinkedHashSet<Long>();
        criteriaIds.add(1L);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationCriteriaIds(criteriaIds)
            .certificationCriteriaOperator(SearchSetOperator.OR)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCriteriaIdsWithAndOperatorProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCriteriaMet(Stream.of(1L, 2L, 3L).collect(Collectors.toSet()));
        allListings.get(1).setCriteriaMet(Stream.of(1L, 2L, 4L).collect(Collectors.toSet()));
        allListings.get(2).setCriteriaMet(Stream.of(5L, 2L, 3L).collect(Collectors.toSet()));

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<Long> criteriaIds = new LinkedHashSet<Long>();
        criteriaIds.add(1L);
        criteriaIds.add(2L);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationCriteriaIds(criteriaIds)
            .certificationCriteriaOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCriteriaIdsWithOrOperatorProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCriteriaMet(Stream.of(1L, 2L, 3L).collect(Collectors.toSet()));
        allListings.get(1).setCriteriaMet(Stream.of(1L, 2L, 4L).collect(Collectors.toSet()));
        allListings.get(2).setCriteriaMet(Stream.of(5L, 2L, 3L).collect(Collectors.toSet()));
        allListings.get(3).setCriteriaMet(Stream.of(5L).collect(Collectors.toSet()));

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<Long> criteriaIds = new LinkedHashSet<Long>();
        criteriaIds.add(1L);
        criteriaIds.add(2L);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationCriteriaIds(criteriaIds)
            .certificationCriteriaOperator(SearchSetOperator.OR)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCqmWithAndOperatorProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCqmsMet(Stream.of("CMS1", "CMS2", "CMS3").collect(Collectors.toSet()));
        allListings.get(1).setCqmsMet(Stream.of("CMS1", "CMS2", "CMS4").collect(Collectors.toSet()));
        allListings.get(2).setCqmsMet(Stream.of("CMS5", "CMS2", "CMS3").collect(Collectors.toSet()));

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> cqms = new LinkedHashSet<String>();
        cqms.add("CMS1");
        SearchRequest searchRequest = SearchRequest.builder()
            .cqms(cqms)
            .cqmsOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCqmWithOrOperatorProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCqmsMet(Stream.of("CMS1", "CMS2", "CMS3").collect(Collectors.toSet()));
        allListings.get(1).setCqmsMet(Stream.of("CMS1", "CMS2", "CMS4").collect(Collectors.toSet()));
        allListings.get(2).setCqmsMet(Stream.of("CMS5", "CMS2", "CMS3").collect(Collectors.toSet()));

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> cqms = new LinkedHashSet<String>();
        cqms.add("CMS1");
        SearchRequest searchRequest = SearchRequest.builder()
            .cqms(cqms)
            .cqmsOperator(SearchSetOperator.OR)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCqmsWithAndOperatorProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCqmsMet(Stream.of("CMS1", "CMS2", "CMS3").collect(Collectors.toSet()));
        allListings.get(1).setCqmsMet(Stream.of("CMS1", "CMS2", "CMS4").collect(Collectors.toSet()));
        allListings.get(2).setCqmsMet(Stream.of("CMS5", "CMS2", "CMS3").collect(Collectors.toSet()));

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> cqms = new LinkedHashSet<String>();
        cqms.add("CMS1");
        cqms.add("CMS2");
        SearchRequest searchRequest = SearchRequest.builder()
            .cqms(cqms)
            .cqmsOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCqmsWithOrOperatorProvided_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        allListings.get(0).setCqmsMet(Stream.of("CMS1", "CMS2", "CMS3").collect(Collectors.toSet()));
        allListings.get(1).setCqmsMet(Stream.of("CMS1", "CMS2", "CMS4").collect(Collectors.toSet()));
        allListings.get(2).setCqmsMet(Stream.of("CMS5", "CMS2", "CMS3").collect(Collectors.toSet()));
        allListings.get(3).setCqmsMet(Stream.of("CMS5").collect(Collectors.toSet()));

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        Set<String> cqms = new LinkedHashSet<String>();
        cqms.add("CMS1");
        cqms.add("CMS2");
        SearchRequest searchRequest = SearchRequest.builder()
            .cqms(cqms)
            .cqmsOperator(SearchSetOperator.OR)
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_certificationStartDateEqualsListingCertificationDate_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        //Thu Jun 25 2020 19:37:15 GMT-0400
        allListings.get(0).setCertificationDate(1593128235254L);
        //Jun 1 2020
        allListings.get(1).setCertificationDate(1590969600000L);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_certificationStartDateBeforeListingCertificationDate_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        //Thu Jun 25 2020 19:37:15 GMT-0400
        allListings.get(0).setCertificationDate(1593128235254L);
        //Jun 1 2020
        allListings.get(1).setCertificationDate(1590969600000L);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-24")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_certificationEndDateEqualsListingCertificationDate_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        //Thu Jun 25 2020 19:37:15 GMT-0400
        allListings.get(0).setCertificationDate(1593128235254L);
        //Jun 27, 2020
        allListings.get(1).setCertificationDate(1593216000000L);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_certificationEndDateAfterListingCertificationDate_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        //Thu Jun 25 2020 19:37:15 GMT-0400
        allListings.get(0).setCertificationDate(1593128235254L);
        //Jun 27, 2020 05:00
        allListings.get(1).setCertificationDate(1593248400000L);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingCertificationDateBetweenStartAndEnd_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        //Thu Jun 25 2020 19:37:15 GMT-0400
        allListings.get(0).setCertificationDate(1593128235254L);
        //Jun 27, 2020 05:00
        allListings.get(1).setCertificationDate(1593248400000L);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-24")
            .certificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingCertificationDateEqualsStartAndBeforeEnd_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        //Thu Jun 25 2020 19:37:15 GMT-0400
        allListings.get(0).setCertificationDate(1593128235254L);
        //Jun 27, 2020 05:00
        allListings.get(1).setCertificationDate(1593248400000L);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-25")
            .certificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingCertificationDateEqualsEndAndAfterStart_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        //Thu Jun 25 2020 19:37:15 GMT-0400
        allListings.get(0).setCertificationDate(1593128235254L);
        //Jun 27, 2020 05:00
        allListings.get(1).setCertificationDate(1593248400000L);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-24")
            .certificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingCertificationDateEqualsEndAndStart_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(50);
        //Thu Jun 25 2020 19:37:15 GMT-0400
        allListings.get(0).setCertificationDate(1593128235254L);
        //Jun 27, 2020 05:00
        allListings.get(1).setCertificationDate(1593248400000L);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-25")
            .certificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingComplianceTrue_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(result.getSurveillanceCount() > 0 || result.getDirectReviewCount() > 0));
    }

    @Test
    public void search_listingComplianceFalse_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(false)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(result.getSurveillanceCount() == 0 && result.getDirectReviewCount() == 0));
    }

    @Test
    public void search_listingComplianceTrueAndOpenNonConformities_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.OPEN_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(result.getOpenSurveillanceNonConformityCount() > 0 || result.getOpenDirectReviewNonConformityCount() > 0));
    }

    @Test
    public void search_listingComplianceTrueAndClosedNonConformities_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(result.getClosedSurveillanceNonConformityCount() > 0 || result.getClosedDirectReviewNonConformityCount() > 0));
    }

    @Test
    public void search_listingComplianceFalseAndClosedNonConformities_noMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(false)
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_listingComplianceTrueAndOpenOrClosedNonConformities_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(
                            NonConformitySearchOptions.OPEN_NONCONFORMITY,
                            NonConformitySearchOptions.CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .nonConformityOptionsOperator(SearchSetOperator.OR)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(
                result.getClosedSurveillanceNonConformityCount() > 0
                || result.getClosedDirectReviewNonConformityCount() > 0
                || result.getOpenSurveillanceNonConformityCount() > 0
                || result.getOpenDirectReviewNonConformityCount() > 0));
    }

    @Test
    public void search_listingComplianceTrueAndOpenAndClosedNonConformities_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(1).setClosedSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(
                            NonConformitySearchOptions.OPEN_NONCONFORMITY,
                            NonConformitySearchOptions.CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .nonConformityOptionsOperator(SearchSetOperator.AND)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(
                (result.getClosedSurveillanceNonConformityCount() > 0
                        || result.getClosedDirectReviewNonConformityCount() > 0)
                && (result.getOpenSurveillanceNonConformityCount() > 0
                        || result.getOpenDirectReviewNonConformityCount() > 0)));
    }

    @Test
    public void search_listingComplianceFalseAndNeverNonConformities_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(false)
                    .nonConformityOptions(Stream.of(
                            NonConformitySearchOptions.NEVER_NONCONFORMITY).collect(Collectors.toSet()))
                    .nonConformityOptionsOperator(SearchSetOperator.OR)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(result.getSurveillanceCount() == 0
                && result.getDirectReviewCount() == 0
                && result.getClosedDirectReviewNonConformityCount() == 0
                && result.getOpenDirectReviewNonConformityCount() == 0
                && result.getClosedSurveillanceNonConformityCount() == 0
                && result.getOpenSurveillanceNonConformityCount() == 0));
    }

    @Test
    public void search_listingComplianceTrueAndNeverNonConformities_findsMatchingListings() throws ValidationException {
        List<CertifiedProductBasicSearchResult> allListings = createBasicSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(1L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(cpSearchManager.getSearchListingCollection()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(
                            NonConformitySearchOptions.NEVER_NONCONFORMITY).collect(Collectors.toSet()))
                    .nonConformityOptionsOperator(SearchSetOperator.OR)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        SearchResponse searchResponse = listingSearchService.search(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(
                (result.getSurveillanceCount() > 0
                        || result.getDirectReviewCount() > 0)
                && result.getClosedDirectReviewNonConformityCount() == 0
                && result.getOpenDirectReviewNonConformityCount() == 0
                && result.getClosedSurveillanceNonConformityCount() == 0
                && result.getOpenSurveillanceNonConformityCount() == 0));
    }

    private List<CertifiedProductBasicSearchResult> createBasicSearchResultCollection(int collectionSize) {
        List<CertifiedProductBasicSearchResult> listings = new ArrayList<CertifiedProductBasicSearchResult>();
        for (int i = 0; i < collectionSize; i++) {
            listings.add(new CertifiedProductBasicSearchResult());
        }
        return listings;
    }
}
