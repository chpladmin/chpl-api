package gov.healthit.chpl.search;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.ListingSearchResponse;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.CQMSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.CertificationCriterionSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.DeveloperSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.IdNamePairSearchResult;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.OrderByOption;
import gov.healthit.chpl.search.domain.RwtSearchOptions;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;

public class ListingSearchServiceTest {

    private ListingSearchManager listingSearchManager;
    private ListingSearchService listingSearchService;

    @Before
    public void setup() {
        SearchRequestValidator searchRequestValidator = Mockito.mock(SearchRequestValidator.class);
        DirectReviewSearchService drService = Mockito.mock(DirectReviewSearchService.class);
        Mockito.when(drService.doesCacheHaveAnyOkData()).thenReturn(true);
        listingSearchManager = Mockito.mock(ListingSearchManager.class);

        listingSearchService = new ListingSearchService(searchRequestValidator,
                listingSearchManager, drService);
    }

    @Test
    public void splitting() {
        String str = "1:Active:2010-12-28|2:Retired:2016-04-01";
        String[] splitStr = str.split("\\|");
        assertNotNull(splitStr);
        assertEquals(2, splitStr.length);
        assertEquals("1:Active:2010-12-28", splitStr[0]);
        assertEquals("2:Retired:2016-04-01", splitStr[1]);
    }

    @Test
    public void search_validEmptySearchRequest_findsAllListings() throws ValidationException {
        Mockito.when(listingSearchManager.getAllListings())
            .thenReturn(createListingSearchResultCollection(100));
        SearchRequest searchRequest = SearchRequest.builder()
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(10, searchResponse.getResults().size());
    }

    @Test
    public void search_pageOutOfRangeSearchRequest_returnsEmptyResponse() throws ValidationException {
        Mockito.when(listingSearchManager.getAllListings())
            .thenReturn(createListingSearchResultCollection(100));
        SearchRequest searchRequest = SearchRequest.builder()
            .pageNumber(2)
            .pageSize(100)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_noListingsWithSearchTerm_returnsEmptyResponse() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(1).setDeveloper(developer("another name"));
        allListings.get(2).setProduct(product("test"));
        allListings.get(3).setVersion(version("test"));
        allListings.get(4).setChplProductNumber("15.02.02.3007.A056.01.00.0.180214");
        allListings.get(5).setAcbCertificationId("12345");
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("search term")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_sortByEditionAscending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setEdition(edition("2015"));
        allListings.get(1).setEdition(edition("2011"));
        allListings.get(2).setEdition(edition("2014"));
        allListings.get(3).setEdition(edition("2015"));
        allListings.get(4).setEdition(edition("2011"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.EDITION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("2011", searchResponse.getResults().get(0).getEdition().getName());
        assertEquals("2011", searchResponse.getResults().get(1).getEdition().getName());
        assertEquals("2014", searchResponse.getResults().get(2).getEdition().getName());
        assertEquals("2015", searchResponse.getResults().get(3).getEdition().getName());
        assertEquals("2015", searchResponse.getResults().get(4).getEdition().getName());
    }

    @Test
    public void search_sortByEditionDescending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setEdition(edition("2015"));
        allListings.get(1).setEdition(edition("2011"));
        allListings.get(2).setEdition(edition("2014"));
        allListings.get(3).setEdition(edition("2015"));
        allListings.get(4).setEdition(edition("2011"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.EDITION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("2015", searchResponse.getResults().get(0).getEdition().getName());
        assertEquals("2015", searchResponse.getResults().get(1).getEdition().getName());
        assertEquals("2014", searchResponse.getResults().get(2).getEdition().getName());
        assertEquals("2011", searchResponse.getResults().get(3).getEdition().getName());
        assertEquals("2011", searchResponse.getResults().get(4).getEdition().getName());
    }

    @Test
    public void search_sortByDerivedEditionAscending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setEdition(edition("2015"));
        allListings.get(0).setCuresUpdate(false);
        allListings.get(1).setEdition(edition("2011"));
        allListings.get(2).setEdition(edition("2014"));
        allListings.get(3).setEdition(edition("2015"));
        allListings.get(3).setCuresUpdate(true);
        allListings.get(4).setEdition(edition("2011"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.DERIVED_EDITION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("2011", searchResponse.getResults().get(0).getEdition().getName());
        assertEquals("2011", searchResponse.getResults().get(1).getEdition().getName());
        assertEquals("2014", searchResponse.getResults().get(2).getEdition().getName());
        assertEquals("2015", searchResponse.getResults().get(3).getEdition().getName());
        assertFalse(searchResponse.getResults().get(3).getCuresUpdate());
        assertEquals("2015", searchResponse.getResults().get(4).getEdition().getName());
        assertTrue(searchResponse.getResults().get(4).getCuresUpdate());
    }

    @Test
    public void search_sortByDerivedEditionDescending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setEdition(edition("2015"));
        allListings.get(0).setCuresUpdate(false);
        allListings.get(1).setEdition(edition("2011"));
        allListings.get(2).setEdition(edition("2014"));
        allListings.get(3).setEdition(edition("2015"));
        allListings.get(3).setCuresUpdate(true);
        allListings.get(4).setEdition(edition("2011"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.DERIVED_EDITION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("2015", searchResponse.getResults().get(0).getEdition().getName());
        assertTrue(searchResponse.getResults().get(0).getCuresUpdate());
        assertEquals("2015", searchResponse.getResults().get(1).getEdition().getName());
        assertFalse(searchResponse.getResults().get(1).getCuresUpdate());
        assertEquals("2014", searchResponse.getResults().get(2).getEdition().getName());
        assertEquals("2011", searchResponse.getResults().get(3).getEdition().getName());
        assertEquals("2011", searchResponse.getResults().get(4).getEdition().getName());
    }

    @Test
    public void search_sortByDeveloperAscending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setDeveloper(developer("z"));
        allListings.get(1).setDeveloper(developer("b"));
        allListings.get(2).setDeveloper(developer("d"));
        allListings.get(3).setDeveloper(developer("f"));
        allListings.get(4).setDeveloper(developer("y"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getDeveloper().getName());
        assertEquals("d", searchResponse.getResults().get(1).getDeveloper().getName());
        assertEquals("f", searchResponse.getResults().get(2).getDeveloper().getName());
        assertEquals("y", searchResponse.getResults().get(3).getDeveloper().getName());
        assertEquals("z", searchResponse.getResults().get(4).getDeveloper().getName());
    }

    @Test
    public void search_sortByDeveloperDescending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setDeveloper(developer("z"));
        allListings.get(1).setDeveloper(developer("b"));
        allListings.get(2).setDeveloper(developer("d"));
        allListings.get(3).setDeveloper(developer("f"));
        allListings.get(4).setDeveloper(developer("y"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getDeveloper().getName());
        assertEquals("y", searchResponse.getResults().get(1).getDeveloper().getName());
        assertEquals("f", searchResponse.getResults().get(2).getDeveloper().getName());
        assertEquals("d", searchResponse.getResults().get(3).getDeveloper().getName());
        assertEquals("b", searchResponse.getResults().get(4).getDeveloper().getName());
    }

    @Test
    public void search_sortByProductAscending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setProduct(product("z"));
        allListings.get(1).setProduct(product("b"));
        allListings.get(2).setProduct(product("d"));
        allListings.get(3).setProduct(product("f"));
        allListings.get(4).setProduct(product("y"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.PRODUCT)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getProduct().getName());
        assertEquals("d", searchResponse.getResults().get(1).getProduct().getName());
        assertEquals("f", searchResponse.getResults().get(2).getProduct().getName());
        assertEquals("y", searchResponse.getResults().get(3).getProduct().getName());
        assertEquals("z", searchResponse.getResults().get(4).getProduct().getName());
    }

    @Test
    public void search_sortByProductDescending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setProduct(product("z"));
        allListings.get(1).setProduct(product("b"));
        allListings.get(2).setProduct(product("d"));
        allListings.get(3).setProduct(product("f"));
        allListings.get(4).setProduct(product("y"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.PRODUCT)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getProduct().getName());
        assertEquals("y", searchResponse.getResults().get(1).getProduct().getName());
        assertEquals("f", searchResponse.getResults().get(2).getProduct().getName());
        assertEquals("d", searchResponse.getResults().get(3).getProduct().getName());
        assertEquals("b", searchResponse.getResults().get(4).getProduct().getName());
    }

    @Test
    public void search_sortByVersionAscending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setVersion(version("z"));
        allListings.get(1).setVersion(version("b"));
        allListings.get(2).setVersion(version("d"));
        allListings.get(3).setVersion(version("f"));
        allListings.get(4).setVersion(version("y"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.VERSION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getVersion().getName());
        assertEquals("d", searchResponse.getResults().get(1).getVersion().getName());
        assertEquals("f", searchResponse.getResults().get(2).getVersion().getName());
        assertEquals("y", searchResponse.getResults().get(3).getVersion().getName());
        assertEquals("z", searchResponse.getResults().get(4).getVersion().getName());
    }

    @Test
    public void search_sortByVersionDescending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setVersion(version("z"));
        allListings.get(1).setVersion(version("b"));
        allListings.get(2).setVersion(version("d"));
        allListings.get(3).setVersion(version("f"));
        allListings.get(4).setVersion(version("y"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.VERSION)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getVersion().getName());
        assertEquals("y", searchResponse.getResults().get(1).getVersion().getName());
        assertEquals("f", searchResponse.getResults().get(2).getVersion().getName());
        assertEquals("d", searchResponse.getResults().get(3).getVersion().getName());
        assertEquals("b", searchResponse.getResults().get(4).getVersion().getName());
    }

    @Test
    public void search_sortByCertificationDateDescending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setCertificationDate(LocalDate.parse("1970-01-01"));
        allListings.get(1).setCertificationDate(LocalDate.parse("1980-01-01"));
        allListings.get(2).setCertificationDate(LocalDate.parse("1970-02-01"));
        allListings.get(3).setCertificationDate(LocalDate.parse("2022-01-01"));
        allListings.get(4).setCertificationDate(LocalDate.parse("1970-01-15"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CERTIFICATION_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDate.parse("2022-01-01"), searchResponse.getResults().get(0).getCertificationDate());
        assertEquals(LocalDate.parse("1980-01-01"), searchResponse.getResults().get(1).getCertificationDate());
        assertEquals(LocalDate.parse("1970-02-01"), searchResponse.getResults().get(2).getCertificationDate());
        assertEquals(LocalDate.parse("1970-01-15"), searchResponse.getResults().get(3).getCertificationDate());
        assertEquals(LocalDate.parse("1970-01-01"), searchResponse.getResults().get(4).getCertificationDate());
    }

    @Test
    public void search_sortByCertificationDateAscending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setCertificationDate(LocalDate.parse("1970-01-01"));
        allListings.get(1).setCertificationDate(LocalDate.parse("1980-01-01"));
        allListings.get(2).setCertificationDate(LocalDate.parse("1970-02-01"));
        allListings.get(3).setCertificationDate(LocalDate.parse("2022-01-01"));
        allListings.get(4).setCertificationDate(LocalDate.parse("1970-01-15"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CERTIFICATION_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDate.parse("1970-01-01"), searchResponse.getResults().get(0).getCertificationDate());
        assertEquals(LocalDate.parse("1970-01-15"), searchResponse.getResults().get(1).getCertificationDate());
        assertEquals(LocalDate.parse("1970-02-01"), searchResponse.getResults().get(2).getCertificationDate());
        assertEquals(LocalDate.parse("1980-01-01"), searchResponse.getResults().get(3).getCertificationDate());
        assertEquals(LocalDate.parse("2022-01-01"), searchResponse.getResults().get(4).getCertificationDate());
    }

    @Test
    public void search_sortByChplIdAscending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setChplProductNumber("CHP-12345");
        allListings.get(1).setChplProductNumber("15.04.04.1234.PROD.11.1.01.123456");
        allListings.get(2).setChplProductNumber("CHP-23456");
        allListings.get(3).setChplProductNumber("14.04.04.1234.PROD.11.1.01.123456");
        allListings.get(4).setChplProductNumber("15.99.04.3078.Ninj.01.00.0.200629");
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CHPL_ID)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

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
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setChplProductNumber("CHP-12345");
        allListings.get(1).setChplProductNumber("15.04.04.1234.PROD.11.1.01.123456");
        allListings.get(2).setChplProductNumber("CHP-23456");
        allListings.get(3).setChplProductNumber("14.04.04.1234.PROD.11.1.01.123456");
        allListings.get(4).setChplProductNumber("15.99.04.3078.Ninj.01.00.0.200629");
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CHPL_ID)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

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
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setCertificationStatus(status(CertificationStatusType.Active.getName()));
        allListings.get(1).setCertificationStatus(status(CertificationStatusType.Retired.getName()));
        allListings.get(2).setCertificationStatus(status(CertificationStatusType.WithdrawnByDeveloper.getName()));
        allListings.get(3).setCertificationStatus(status(CertificationStatusType.Active.getName()));
        allListings.get(4).setCertificationStatus(status(CertificationStatusType.SuspendedByAcb.getName()));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(CertificationStatusType.Active.getName(), searchResponse.getResults().get(0).getCertificationStatus().getName());
        assertEquals(CertificationStatusType.Active.getName(), searchResponse.getResults().get(1).getCertificationStatus().getName());
        assertEquals(CertificationStatusType.Retired.getName(), searchResponse.getResults().get(2).getCertificationStatus().getName());
        assertEquals(CertificationStatusType.SuspendedByAcb.getName(), searchResponse.getResults().get(3).getCertificationStatus().getName());
        assertEquals(CertificationStatusType.WithdrawnByDeveloper.getName(), searchResponse.getResults().get(4).getCertificationStatus().getName());
    }

    @Test
    public void search_sortByCertificationStatusDescending_ordersResults() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(5);
        allListings.get(0).setCertificationStatus(status(CertificationStatusType.Active.getName()));
        allListings.get(1).setCertificationStatus(status(CertificationStatusType.Retired.getName()));
        allListings.get(2).setCertificationStatus(status(CertificationStatusType.WithdrawnByDeveloper.getName()));
        allListings.get(3).setCertificationStatus(status(CertificationStatusType.Active.getName()));
        allListings.get(4).setCertificationStatus(status(CertificationStatusType.SuspendedByAcb.getName()));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(CertificationStatusType.WithdrawnByDeveloper.getName(), searchResponse.getResults().get(0).getCertificationStatus().getName());
        assertEquals(CertificationStatusType.SuspendedByAcb.getName(), searchResponse.getResults().get(1).getCertificationStatus().getName());
        assertEquals(CertificationStatusType.Retired.getName(), searchResponse.getResults().get(2).getCertificationStatus().getName());
        assertEquals(CertificationStatusType.Active.getName(), searchResponse.getResults().get(3).getCertificationStatus().getName());
        assertEquals(CertificationStatusType.Active.getName(), searchResponse.getResults().get(4).getCertificationStatus().getName());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingDevelopers() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDeveloper(developer("dev name"));
        allListings.get(1).setDeveloper(developer("long DEV name here"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingProductOwnerHistory() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setPreviousDevelopers(Stream.of(
                productOwner("dev name"),
                productOwner("some other owner name")).collect(Collectors.toSet()));
        allListings.get(1).setPreviousDevelopers(Stream.of(
                productOwner("long DEV name here"),
                productOwner("some other owner name")).collect(Collectors.toSet()));
        allListings.get(2).setPreviousDevelopers(Stream.of(
                productOwner("no match 1"),
                productOwner("no match 2")).collect(Collectors.toSet()));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingProducts() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setProduct(product("product name"));
        allListings.get(1).setProduct(product("long PRODUCT name here"));
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("product name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingChplProductNumber() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setChplProductNumber("15.02.02.3007.A056.01.00.0.180214");
        allListings.get(1).setChplProductNumber("CHP-123456");
        allListings.get(2).setChplProductNumber("15.02.02.3007.A056.01.00.0.180215");
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("15.02.02.3007.A056.01.00.0.18021")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingChplProductNumberHistory() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setChplProductNumber("15.02.02.3007.A055.01.00.0.180214");
        allListings.get(0).setPreviousChplProductNumbers(Stream.of("15.02.02.3007.A056.01.00.0.180215")
                .collect(Collectors.toSet()));
        allListings.get(1).setChplProductNumber("CHP-123456");
        allListings.get(2).setChplProductNumber("15.02.02.3007.A056.01.00.0.180215");
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("15.02.02.3007.A056.01.00.0.180215")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsListingsWithMatchingAcbCertificationId() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setAcbCertificationId("15.02.02.3007.A056.01.00.0.180214");
        allListings.get(1).setAcbCertificationId("CHP-123456");
        allListings.get(2).setAcbCertificationId("15.02.02.3007.A056.01.00.0.180215");
        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("15.02.02.3007.A056.01.00.0.18021")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_singleAcbNameProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationBody(acb("ACB 1"));
        allListings.get(1).setCertificationBody(acb("ACB 2"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> acbNames = new LinkedHashSet<String>();
        acbNames.add("ACB 1");
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationBodies(acbNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_mutlipleAcbNamesProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationBody(acb("ACB 1"));
        allListings.get(1).setCertificationBody(acb("ACB 2"));
        allListings.get(2).setCertificationBody(acb("ACB 1"));
        allListings.get(3).setCertificationBody(acb("ACB 5"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> acbNames = new LinkedHashSet<String>();
        acbNames.add("ACB 1");
        acbNames.add("ACB 2");
        acbNames.add("ACB 3");
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationBodies(acbNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCertificationStatusProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationStatus(status(CertificationStatusType.Active.getName()));
        allListings.get(1).setCertificationStatus(status(CertificationStatusType.SuspendedByAcb.getName()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> certificationStatusNames = new LinkedHashSet<String>();
        certificationStatusNames.add(CertificationStatusType.SuspendedByAcb.getName());
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationStatuses(certificationStatusNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCertificationStatusProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationStatus(status(CertificationStatusType.Active.getName()));
        allListings.get(1).setCertificationStatus(status(CertificationStatusType.SuspendedByAcb.getName()));
        allListings.get(2).setCertificationStatus(status(CertificationStatusType.Active.getName()));
        allListings.get(3).setCertificationStatus(status(CertificationStatusType.SuspendedByOnc.getName()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> certificationStatusNames = new LinkedHashSet<String>();
        certificationStatusNames.add(CertificationStatusType.SuspendedByAcb.getName());
        certificationStatusNames.add(CertificationStatusType.Active.getName());

        SearchRequest searchRequest = SearchRequest.builder()
            .certificationStatuses(certificationStatusNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCertificationEditionProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setEdition(edition("2014"));
        allListings.get(1).setEdition(edition("2014"));
        allListings.get(2).setEdition(edition("2011"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2011");
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCertificationEditionProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setEdition(edition("2014"));
        allListings.get(1).setEdition(edition("2014"));
        allListings.get(2).setEdition(edition("2011"));
        allListings.get(3).setEdition(edition("2011"));
        allListings.get(4).setEdition(edition("2015"));
        allListings.get(5).setEdition(edition("2015"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2011");
        editionNames.add("2015");

        SearchRequest searchRequest = SearchRequest.builder()
            .certificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(4, searchResponse.getRecordCount());
        assertEquals(4, searchResponse.getResults().size());
    }

    @Test
    public void search_singleDerivedCertificationEditionProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setEdition(edition("2014"));
        allListings.get(1).setEdition(edition("2014"));
        allListings.get(2).setEdition(edition("2011"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2011");
        SearchRequest searchRequest = SearchRequest.builder()
            .derivedCertificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_2015DerivedCertificationEditionProvided_finds2015ButNot2015CuresUpdateListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setId(1L);
        allListings.get(0).setEdition(edition("2015"));
        allListings.get(0).setCuresUpdate(false);
        allListings.get(1).setId(2L);
        allListings.get(1).setEdition(edition("2015"));
        allListings.get(1).setCuresUpdate(true);
        allListings.get(2).setEdition(edition("2014"));
        allListings.get(3).setEdition(edition("2011"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2015");
        SearchRequest searchRequest = SearchRequest.builder()
            .derivedCertificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertTrue(searchResponse.getResults().get(0).getId().equals(1L));
    }

    @Test
    public void search_2015CuresUpdateDerivedCertificationEditionProvided_finds2015CuresUpdateButNot2015Listings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setId(1L);
        allListings.get(0).setEdition(edition("2015"));
        allListings.get(1).setCuresUpdate(false);
        allListings.get(1).setId(2L);
        allListings.get(1).setEdition(edition("2015"));
        allListings.get(1).setCuresUpdate(true);
        allListings.get(2).setId(3L);
        allListings.get(2).setEdition(edition("2015"));
        allListings.get(2).setCuresUpdate(true);
        allListings.get(3).setEdition(edition("2014"));
        allListings.get(4).setEdition(edition("2011"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2015 Cures Update");
        SearchRequest searchRequest = SearchRequest.builder()
            .derivedCertificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
        assertTrue(searchResponse.getResults().stream().map(result -> result.getId()).collect(Collectors.toList())
                .contains(2L));
        assertTrue(searchResponse.getResults().stream().map(result -> result.getId()).collect(Collectors.toList())
                .contains(3L));
    }

    @Test
    public void search_2011And2015DerivedCertificationEditionProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setEdition(edition("2014"));
        allListings.get(1).setEdition(edition("2014"));
        allListings.get(2).setEdition(edition("2011"));
        allListings.get(3).setEdition(edition("2011"));
        allListings.get(4).setEdition(edition("2015"));
        allListings.get(4).setCuresUpdate(false);
        allListings.get(5).setEdition(edition("2015"));
        allListings.get(5).setCuresUpdate(false);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2011");
        editionNames.add("2015");

        SearchRequest searchRequest = SearchRequest.builder()
            .derivedCertificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(4, searchResponse.getRecordCount());
        assertEquals(4, searchResponse.getResults().size());
    }

    @Test
    public void search_2011And2015CuresUpdateDerivedCertificationEditionProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setEdition(edition("2014"));
        allListings.get(1).setEdition(edition("2014"));
        allListings.get(2).setEdition(edition("2011"));
        allListings.get(3).setEdition(edition("2011"));
        allListings.get(4).setEdition(edition("2015"));
        allListings.get(4).setCuresUpdate(false);
        allListings.get(5).setEdition(edition("2015"));
        allListings.get(5).setCuresUpdate(true);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2011");
        editionNames.add("2015 Cures Update");

        SearchRequest searchRequest = SearchRequest.builder()
            .derivedCertificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_2015And2015DerivedCertificationEditionProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setEdition(edition("2014"));
        allListings.get(1).setEdition(edition("2014"));
        allListings.get(2).setEdition(edition("2011"));
        allListings.get(3).setEdition(edition("2011"));
        allListings.get(4).setEdition(edition("2015"));
        allListings.get(4).setCuresUpdate(false);
        allListings.get(5).setEdition(edition("2015"));
        allListings.get(5).setCuresUpdate(true);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> editionNames = new LinkedHashSet<String>();
        editionNames.add("2015");
        editionNames.add("2015 Cures Update");

        SearchRequest searchRequest = SearchRequest.builder()
            .derivedCertificationEditions(editionNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_developerProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDeveloper(developer("dev name"));
        allListings.get(1).setDeveloper(developer("long DEV name here"));
        allListings.get(2).setDeveloper(developer("doesn't match"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .developer("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_productProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setProduct(product("product name"));
        allListings.get(1).setProduct(product("long PRODUCT name here"));
        allListings.get(2).setProduct(product("doesn't match"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .product("product name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_versionProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setVersion(version("version name"));
        allListings.get(1).setVersion(version("long VERSION name here"));
        allListings.get(2).setVersion(version("doesn't match"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .version("version name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_practiceTypeProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setPracticeType(practiceType("ambulatory"));
        allListings.get(1).setPracticeType(practiceType("AMbulatory"));
        allListings.get(2).setPracticeType(practiceType("inpatient"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .practiceType("Ambulatory")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCriterionIdWithAndOperatorProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCriteriaMet(Stream.of(criterion(1L), criterion(2L), criterion(3L)).collect(Collectors.toSet()));
        allListings.get(1).setCriteriaMet(Stream.of(criterion(1L), criterion(2L), criterion(4L)).collect(Collectors.toSet()));
        allListings.get(2).setCriteriaMet(Stream.of(criterion(5L), criterion(2L), criterion(3L)).collect(Collectors.toSet()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<Long> criteriaIds = new LinkedHashSet<Long>();
        criteriaIds.add(1L);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationCriteriaIds(criteriaIds)
            .certificationCriteriaOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCriterionIdWithOrOperatorProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCriteriaMet(Stream.of(criterion(1L), criterion(2L), criterion(3L)).collect(Collectors.toSet()));
        allListings.get(1).setCriteriaMet(Stream.of(criterion(1L), criterion(2L), criterion(4L)).collect(Collectors.toSet()));
        allListings.get(2).setCriteriaMet(Stream.of(criterion(5L), criterion(2L), criterion(3L)).collect(Collectors.toSet()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<Long> criteriaIds = new LinkedHashSet<Long>();
        criteriaIds.add(1L);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationCriteriaIds(criteriaIds)
            .certificationCriteriaOperator(SearchSetOperator.OR)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCriteriaIdsWithAndOperatorProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCriteriaMet(Stream.of(criterion(1L), criterion(2L), criterion(3L)).collect(Collectors.toSet()));
        allListings.get(1).setCriteriaMet(Stream.of(criterion(1L), criterion(2L), criterion(4L)).collect(Collectors.toSet()));
        allListings.get(2).setCriteriaMet(Stream.of(criterion(5L), criterion(2L), criterion(3L)).collect(Collectors.toSet()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<Long> criteriaIds = new LinkedHashSet<Long>();
        criteriaIds.add(1L);
        criteriaIds.add(2L);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationCriteriaIds(criteriaIds)
            .certificationCriteriaOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCriteriaIdsWithOrOperatorProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCriteriaMet(Stream.of(criterion(1L), criterion(2L), criterion(3L)).collect(Collectors.toSet()));
        allListings.get(1).setCriteriaMet(Stream.of(criterion(1L), criterion(2L), criterion(4L)).collect(Collectors.toSet()));
        allListings.get(2).setCriteriaMet(Stream.of(criterion(5L), criterion(2L), criterion(3L)).collect(Collectors.toSet()));
        allListings.get(3).setCriteriaMet(Stream.of(criterion(5L)).collect(Collectors.toSet()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<Long> criteriaIds = new LinkedHashSet<Long>();
        criteriaIds.add(1L);
        criteriaIds.add(2L);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationCriteriaIds(criteriaIds)
            .certificationCriteriaOperator(SearchSetOperator.OR)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCqmWithAndOperatorProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCqmsMet(Stream.of(cqm("CMS1"), cqm("CMS2"), cqm("CMS3")).collect(Collectors.toSet()));
        allListings.get(1).setCqmsMet(Stream.of(cqm("CMS1"), cqm("CMS2"), cqm("CMS4")).collect(Collectors.toSet()));
        allListings.get(2).setCqmsMet(Stream.of(cqm("CMS5"), cqm("CMS2"), cqm("CMS3")).collect(Collectors.toSet()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> cqms = new LinkedHashSet<String>();
        cqms.add("CMS1");
        SearchRequest searchRequest = SearchRequest.builder()
            .cqms(cqms)
            .cqmsOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCqmWithOrOperatorProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCqmsMet(Stream.of(cqm("CMS1"), cqm("CMS2"), cqm("CMS3")).collect(Collectors.toSet()));
        allListings.get(1).setCqmsMet(Stream.of(cqm("CMS1"), cqm("CMS2"), cqm("CMS4")).collect(Collectors.toSet()));
        allListings.get(2).setCqmsMet(Stream.of(cqm("CMS5"), cqm("CMS2"), cqm("CMS3")).collect(Collectors.toSet()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> cqms = new LinkedHashSet<String>();
        cqms.add("CMS1");
        SearchRequest searchRequest = SearchRequest.builder()
            .cqms(cqms)
            .cqmsOperator(SearchSetOperator.OR)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCqmsWithAndOperatorProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCqmsMet(Stream.of(cqm("CMS1"), cqm("CMS2"), cqm("CMS3")).collect(Collectors.toSet()));
        allListings.get(1).setCqmsMet(Stream.of(cqm("CMS1"), cqm("CMS2"), cqm("CMS4")).collect(Collectors.toSet()));
        allListings.get(2).setCqmsMet(Stream.of(cqm("CMS5"), cqm("CMS2"), cqm("CMS3")).collect(Collectors.toSet()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> cqms = new LinkedHashSet<String>();
        cqms.add("CMS1");
        cqms.add("CMS2");
        SearchRequest searchRequest = SearchRequest.builder()
            .cqms(cqms)
            .cqmsOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleCqmsWithOrOperatorProvided_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCqmsMet(Stream.of(cqm("CMS1"), cqm("CMS2"), cqm("CMS3")).collect(Collectors.toSet()));
        allListings.get(1).setCqmsMet(Stream.of(cqm("CMS1"), cqm("CMS2"), cqm("CMS4")).collect(Collectors.toSet()));
        allListings.get(2).setCqmsMet(Stream.of(cqm("CMS5"), cqm("CMS2"), cqm("CMS3")).collect(Collectors.toSet()));
        allListings.get(3).setCqmsMet(Stream.of(cqm("CMS5")).collect(Collectors.toSet()));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        Set<String> cqms = new LinkedHashSet<String>();
        cqms.add("CMS1");
        cqms.add("CMS2");
        SearchRequest searchRequest = SearchRequest.builder()
            .cqms(cqms)
            .cqmsOperator(SearchSetOperator.OR)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_certificationStartDateEqualsListingCertificationDate_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setCertificationDate(LocalDate.parse("2020-06-01"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_certificationStartDateBeforeListingCertificationDate_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setCertificationDate(LocalDate.parse("2020-06-01"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-24")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_certificationEndDateEqualsListingCertificationDate_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setCertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_certificationEndDateAfterListingCertificationDate_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setCertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingCertificationDateBetweenStartAndEnd_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setCertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-24")
            .certificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingCertificationDateEqualsStartAndBeforeEnd_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setCertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-25")
            .certificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingCertificationDateEqualsEndAndAfterStart_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setCertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-24")
            .certificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingCertificationDateEqualsEndAndStart_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setCertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setCertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .certificationDateStart("2020-06-25")
            .certificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_decertificationStartDateEqualsListingDecertificationDate_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setDecertificationDate(LocalDate.parse("2020-06-01"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .decertificationDateStart("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_decertificationStartDateBeforeListingDecertificationDate_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setDecertificationDate(LocalDate.parse("2020-06-01"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .decertificationDateStart("2020-06-24")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_decertificationEndDateEqualsListingDecertificationDate_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .decertificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_decertificationEndDateAfterListingDecertificationDate_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .decertificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingDecertificationDateBetweenStartAndEnd_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .decertificationDateStart("2020-06-24")
            .decertificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingDecertificationDateEqualsStartAndBeforeEnd_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .decertificationDateStart("2020-06-25")
            .decertificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingDecertificationDateEqualsEndAndAfterStart_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .decertificationDateStart("2020-06-24")
            .decertificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingDecertificationDateEqualsEndAndStart_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(50);
        allListings.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allListings.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .decertificationDateStart("2020-06-25")
            .decertificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_listingComplianceTrue_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(result.getSurveillanceCount() > 0 || result.getDirectReviewCount() > 0));
    }

    @Test
    public void search_listingComplianceFalse_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(false)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(result.getSurveillanceCount() == 0 && result.getDirectReviewCount() == 0));
    }

    @Test
    public void search_listingComplianceTrueAndOpenNonConformities_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.OPEN_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(result.getOpenSurveillanceNonConformityCount() > 0 || result.getOpenDirectReviewNonConformityCount() > 0));
    }

    @Test
    public void search_listingComplianceTrueAndClosedNonConformities_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(result.getClosedSurveillanceNonConformityCount() > 0 || result.getClosedDirectReviewNonConformityCount() > 0));
    }

    @Test
    public void search_listingComplianceFalseAndClosedNonConformities_noMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(false)
                    .nonConformityOptions(Stream.of(NonConformitySearchOptions.CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_listingComplianceTrueAndOpenOrClosedNonConformities_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
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
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

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
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(1).setClosedSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
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
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

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
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(0L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
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
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

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
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(1L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
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
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

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

    @Test
    public void search_listingComplianceTrueAndNotNeverNonConformities_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(1L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(
                            NonConformitySearchOptions.NOT_NEVER_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(
                result.getClosedDirectReviewNonConformityCount() > 0
                || result.getOpenDirectReviewNonConformityCount() > 0
                || result.getClosedSurveillanceNonConformityCount() > 0
                || result.getOpenSurveillanceNonConformityCount() > 0));
    }

    @Test
    public void search_listingComplianceTrueAndNotOpenNonConformities_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(1L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(
                            NonConformitySearchOptions.NOT_OPEN_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(
                result.getOpenDirectReviewNonConformityCount() == 0
                && result.getOpenSurveillanceNonConformityCount() == 0));
    }

    @Test
    public void search_listingComplianceTrueAndNotClosedNonConformities_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(1L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(
                            NonConformitySearchOptions.NOT_CLOSED_NONCONFORMITY).collect(Collectors.toSet()))
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(
                result.getClosedDirectReviewNonConformityCount() == 0
                && result.getClosedSurveillanceNonConformityCount() == 0));
    }

    @Test
    public void search_listingComplianceTrueAndNotOpenAndNotClosedNonConformities_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(1L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(
                            NonConformitySearchOptions.NOT_CLOSED_NONCONFORMITY,
                            NonConformitySearchOptions.NOT_OPEN_NONCONFORMITY).collect(Collectors.toSet()))
                    .nonConformityOptionsOperator(SearchSetOperator.AND)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(
                result.getClosedDirectReviewNonConformityCount() == 0
                && result.getClosedSurveillanceNonConformityCount() == 0
                && result.getOpenDirectReviewNonConformityCount() == 0
                && result.getOpenSurveillanceNonConformityCount() == 0));
    }

    @Test
    public void search_listingComplianceTrueAndOpenAndNotClosedAndNotNeverNonConformities_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setSurveillanceCount(1L);
        allListings.get(0).setDirectReviewCount(0);
        allListings.get(1).setSurveillanceCount(2L);
        allListings.get(1).setDirectReviewCount(0);
        allListings.get(1).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setSurveillanceCount(0L);
        allListings.get(2).setDirectReviewCount(1);
        allListings.get(2).setOpenSurveillanceNonConformityCount(1L);
        allListings.get(2).setClosedDirectReviewNonConformityCount(1);

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .complianceActivity(ComplianceSearchFilter.builder()
                    .hasHadComplianceActivity(true)
                    .nonConformityOptions(Stream.of(
                            NonConformitySearchOptions.NOT_NEVER_NONCONFORMITY,
                            NonConformitySearchOptions.NOT_CLOSED_NONCONFORMITY,
                            NonConformitySearchOptions.OPEN_NONCONFORMITY).collect(Collectors.toSet()))
                    .nonConformityOptionsOperator(SearchSetOperator.AND)
                    .build())
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_hasResultsUrlAndHasPlansUrl_findsMatchingListings() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setRwtPlansUrl("someurl");
        allListings.get(0).setRwtResultsUrl("someurl");
        allListings.get(1).setRwtResultsUrl("someurl");
        allListings.get(2).setRwtPlansUrl("someurl");

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .rwtOptions(Stream.of(RwtSearchOptions.HAS_RESULTS_URL, RwtSearchOptions.HAS_PLANS_URL).collect(Collectors.toSet()))
            .rwtOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        searchResponse.getResults().forEach(result -> assertTrue(!StringUtils.isBlank(result.getRwtPlansUrl()) && !StringUtils.isBlank(result.getRwtResultsUrl())));
    }

    @Test
    public void search_hasResultsUrlAndNotHasResultsUrl_findsNoMatches() throws ValidationException {
        List<ListingSearchResult> allListings = createListingSearchResultCollection(3);
        allListings.get(0).setRwtResultsUrl("test");
        allListings.get(2).setRwtResultsUrl("someurl");

        Mockito.when(listingSearchManager.getAllListings()).thenReturn(allListings);
        SearchRequest searchRequest = SearchRequest.builder()
            .rwtOptions(Stream.of(RwtSearchOptions.HAS_RESULTS_URL, RwtSearchOptions.NO_RESULTS_URL).collect(Collectors.toSet()))
            .rwtOperator(SearchSetOperator.AND)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    private List<ListingSearchResult> createListingSearchResultCollection(int collectionSize) {
        List<ListingSearchResult> listings = new ArrayList<ListingSearchResult>();
        for (int i = 0; i < collectionSize; i++) {
            listings.add(new ListingSearchResult());
        }
        return listings;
    }

    private IdNamePairSearchResult acb(String name) {
        return IdNamePairSearchResult.builder()
                .name(name)
                .build();
    }

    private DeveloperSearchResult developer(String name) {
        return DeveloperSearchResult.builder()
                .name(name)
                .build();
    }

    private IdNamePairSearchResult productOwner(String name) {
        return IdNamePairSearchResult.builder()
                .name(name)
                .build();
    }

    private IdNamePairSearchResult product(String name) {
        return IdNamePairSearchResult.builder()
                .name(name)
                .build();
    }

    private IdNamePairSearchResult version(String name) {
        return IdNamePairSearchResult.builder()
                .name(name)
                .build();
    }

    private IdNamePairSearchResult edition(String year) {
        return IdNamePairSearchResult.builder()
                .name(year)
                .build();
    }

    private IdNamePairSearchResult status(String name) {
        return IdNamePairSearchResult.builder()
                .name(name)
                .build();
    }

    private IdNamePairSearchResult practiceType(String name) {
        return IdNamePairSearchResult.builder()
                .name(name)
                .build();
    }

    private CertificationCriterionSearchResult criterion(Long id) {
        return CertificationCriterionSearchResult.builder()
                .id(id)
                .build();
    }

    private CQMSearchResult cqm(String cqmNumber) {
        return CQMSearchResult.builder()
                .number(cqmNumber)
                .build();
    }
}
