package gov.healthit.chpl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ListingSearchServiceReviewerTest {

    private ErrorMessageUtil msgUtil;
    private DimensionalDataManager dimensionalDataManager;
    private CertifiedProductSearchManager cpSearchManager;
    private ListingSearchService listingSearchService;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        dimensionalDataManager = Mockito.mock(DimensionalDataManager.class);
        cpSearchManager = Mockito.mock(CertifiedProductSearchManager.class);

        listingSearchService = new ListingSearchService(msgUtil, dimensionalDataManager, cpSearchManager);
    }

    @Test
    public void search_validEmptySearchRequest_findsAllListings() throws InvalidArgumentsException {
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
    public void search_pageOutOfRangeSearchRequest_returnsEmptyResponse() throws InvalidArgumentsException {
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
    public void search_noListingsWithSearchTerm_returnsEmptyResponse() throws InvalidArgumentsException {
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
    public void search_searchTermProvided_findsListingsWithMatchingDevelopers() throws InvalidArgumentsException {
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
    public void search_searchTermProvided_findsListingsWithMatchingProducts() throws InvalidArgumentsException {
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
    public void search_searchTermProvided_findsListingsWithMatchingChplProductNumber() throws InvalidArgumentsException {
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
    public void search_searchTermProvided_findsListingsWithMatchingAcbCertificationId() throws InvalidArgumentsException {
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
    public void search_singleAcbNameProvided_findsMatchingListings() throws InvalidArgumentsException {
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
    public void search_mutlipleAcbNamesProvided_findsMatchingListings() throws InvalidArgumentsException {
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
    public void search_singleCertificationStatusProvided_findsMatchingListings() throws InvalidArgumentsException {
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
    public void search_multipleCertificationStatusProvided_findsMatchingListings() throws InvalidArgumentsException {
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
    public void search_singleCertificationEditionProvided_findsMatchingListings() throws InvalidArgumentsException {
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
    public void search_multipleCertificationEditionProvided_findsMatchingListings() throws InvalidArgumentsException {
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
    public void search_developerProvided_findsMatchingListings() throws InvalidArgumentsException {
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
    public void search_productProvided_findsMatchingListings() throws InvalidArgumentsException {
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
    public void search_versionProvided_findsMatchingListings() throws InvalidArgumentsException {
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
    public void search_practiceTypeProvided_findsMatchingListings() throws InvalidArgumentsException {
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

    private List<CertifiedProductBasicSearchResult> createBasicSearchResultCollection(int collectionSize) {
        List<CertifiedProductBasicSearchResult> listings = new ArrayList<CertifiedProductBasicSearchResult>();
        for (int i = 0; i < collectionSize; i++) {
            listings.add(new CertifiedProductBasicSearchResult());
        }
        return listings;
    }
}
