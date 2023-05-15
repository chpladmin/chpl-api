package gov.healthit.chpl.questionableactivity.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivity;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;

@SuppressWarnings("checkstyle:magicnumber")
public class QuestionableActivitySearchServiceTest {

    private QuestionableActivityDAO questionableActivityDao;
    private QuestionableActivitySearchDAO questionableActivitySearchDao;
    private QuestionableActivityTrigger devTrigger, prodTrigger, listingTrigger;
    private QuestionableActivitySearchService questionableActivitySearchService;

    @Before
    public void setup() {
        devTrigger = QuestionableActivityTrigger.builder()
                .id(1L)
                .name("Developer Name Changed")
                .level("Developer")
                .build();
        prodTrigger = QuestionableActivityTrigger.builder()
                .id(2L)
                .name("Product Name Changed")
                .level("Product")
                .build();
        listingTrigger = QuestionableActivityTrigger.builder()
                .id(3L)
                .name("Certification Status Changed")
                .level("Listing")
                .build();
        questionableActivityDao = Mockito.mock(QuestionableActivityDAO.class);
        Mockito.when(questionableActivityDao.getAllTriggers())
            .thenReturn(Stream.of(devTrigger, prodTrigger, listingTrigger).toList());
        questionableActivitySearchDao = Mockito.mock(QuestionableActivitySearchDAO.class);

        SearchRequestValidator searchRequestValidator = Mockito.mock(SearchRequestValidator.class);
        questionableActivitySearchService = new QuestionableActivitySearchService(
                questionableActivitySearchDao, questionableActivityDao, searchRequestValidator);
    }

    @Test
    public void search_validEmptySearchRequest_findsAllListings() throws ValidationException {
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(createSearchResultCollection(100));
        SearchRequest searchRequest = SearchRequest.builder()
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(10, searchResponse.getResults().size());
    }

    @Test
    public void search_pageOutOfRangeSearchRequest_returnsEmptyResponse() throws ValidationException {
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(createSearchResultCollection(100));
        SearchRequest searchRequest = SearchRequest.builder()
            .pageNumber(2)
            .pageSize(100)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_sortByDeveloperNameAscending_ordersResults() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(5);
        searchResults.get(0).setDeveloperName("a");
        searchResults.get(1).setDeveloperName("z");
        searchResults.get(2).setDeveloperName("c");
        searchResults.get(3).setDeveloperName("b");
        searchResults.get(4).setDeveloperName("f");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("a", searchResponse.getResults().get(0).getDeveloperName());
        assertEquals("b", searchResponse.getResults().get(1).getDeveloperName());
        assertEquals("c", searchResponse.getResults().get(2).getDeveloperName());
        assertEquals("f", searchResponse.getResults().get(3).getDeveloperName());
        assertEquals("z", searchResponse.getResults().get(4).getDeveloperName());
    }

    @Test
    public void search_sortByDeveloperNameDescending_ordersResults() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(5);
        searchResults.get(0).setDeveloperName("a");
        searchResults.get(1).setDeveloperName("z");
        searchResults.get(2).setDeveloperName("c");
        searchResults.get(3).setDeveloperName("b");
        searchResults.get(4).setDeveloperName("f");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getDeveloperName());
        assertEquals("f", searchResponse.getResults().get(1).getDeveloperName());
        assertEquals("c", searchResponse.getResults().get(2).getDeveloperName());
        assertEquals("b", searchResponse.getResults().get(3).getDeveloperName());
        assertEquals("a", searchResponse.getResults().get(4).getDeveloperName());
    }

    @Test
    public void search_sortByProductNameAscending_ordersResults() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(5);
        searchResults.get(0).setProductName("a");
        searchResults.get(1).setProductName("z");
        searchResults.get(2).setProductName("c");
        searchResults.get(3).setProductName("b");
        searchResults.get(4).setProductName("f");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.PRODUCT)
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("a", searchResponse.getResults().get(0).getProductName());
        assertEquals("b", searchResponse.getResults().get(1).getProductName());
        assertEquals("c", searchResponse.getResults().get(2).getProductName());
        assertEquals("f", searchResponse.getResults().get(3).getProductName());
        assertEquals("z", searchResponse.getResults().get(4).getProductName());
    }

    @Test
    public void search_sortByProductNameDescending_ordersResults() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(5);
        searchResults.get(0).setProductName("a");
        searchResults.get(1).setProductName("z");
        searchResults.get(2).setProductName("c");
        searchResults.get(3).setProductName("b");
        searchResults.get(4).setProductName("f");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.PRODUCT)
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getProductName());
        assertEquals("f", searchResponse.getResults().get(1).getProductName());
        assertEquals("c", searchResponse.getResults().get(2).getProductName());
        assertEquals("b", searchResponse.getResults().get(3).getProductName());
        assertEquals("a", searchResponse.getResults().get(4).getProductName());
    }

    @Test
    public void search_sortByChplProductNumberAscending_ordersResults() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(5);
        searchResults.get(0).setChplProductNumber("a");
        searchResults.get(1).setChplProductNumber("z");
        searchResults.get(2).setChplProductNumber("c");
        searchResults.get(3).setChplProductNumber("b");
        searchResults.get(4).setChplProductNumber("f");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CHPL_PRODUCT_NUMBER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("a", searchResponse.getResults().get(0).getChplProductNumber());
        assertEquals("b", searchResponse.getResults().get(1).getChplProductNumber());
        assertEquals("c", searchResponse.getResults().get(2).getChplProductNumber());
        assertEquals("f", searchResponse.getResults().get(3).getChplProductNumber());
        assertEquals("z", searchResponse.getResults().get(4).getChplProductNumber());
    }

    @Test
    public void search_sortByChplProductNumberDescending_ordersResults() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(5);
        searchResults.get(0).setChplProductNumber("a");
        searchResults.get(1).setChplProductNumber("z");
        searchResults.get(2).setChplProductNumber("c");
        searchResults.get(3).setChplProductNumber("b");
        searchResults.get(4).setChplProductNumber("f");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CHPL_PRODUCT_NUMBER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getChplProductNumber());
        assertEquals("f", searchResponse.getResults().get(1).getChplProductNumber());
        assertEquals("c", searchResponse.getResults().get(2).getChplProductNumber());
        assertEquals("b", searchResponse.getResults().get(3).getChplProductNumber());
        assertEquals("a", searchResponse.getResults().get(4).getChplProductNumber());
    }

    @Test
    public void search_sortByActivityDateAscending_ordersResults() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(5);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2022-02-01T00:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2022-01-01T00:00:00"));
        searchResults.get(2).setActivityDate(LocalDateTime.parse("2022-04-01T00:00:00"));
        searchResults.get(3).setActivityDate(LocalDateTime.parse("2022-03-01T00:00:00"));
        searchResults.get(4).setActivityDate(LocalDateTime.parse("2022-05-01T00:00:00"));
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.ACTIVITY_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDateTime.parse("2022-01-01T00:00:00"), searchResponse.getResults().get(0).getActivityDate());
        assertEquals(LocalDateTime.parse("2022-02-01T00:00:00"), searchResponse.getResults().get(1).getActivityDate());
        assertEquals(LocalDateTime.parse("2022-03-01T00:00:00"), searchResponse.getResults().get(2).getActivityDate());
        assertEquals(LocalDateTime.parse("2022-04-01T00:00:00"), searchResponse.getResults().get(3).getActivityDate());
        assertEquals(LocalDateTime.parse("2022-05-01T00:00:00"), searchResponse.getResults().get(4).getActivityDate());
    }

    @Test
    public void search_sortByActivityDateDescending_ordersResults() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(5);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2022-02-01T00:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2022-01-01T00:00:00"));
        searchResults.get(2).setActivityDate(LocalDateTime.parse("2022-04-01T00:00:00"));
        searchResults.get(3).setActivityDate(LocalDateTime.parse("2022-03-01T00:00:00"));
        searchResults.get(4).setActivityDate(LocalDateTime.parse("2022-05-01T00:00:00"));
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.ACTIVITY_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDateTime.parse("2022-05-01T00:00:00"), searchResponse.getResults().get(0).getActivityDate());
        assertEquals(LocalDateTime.parse("2022-04-01T00:00:00"), searchResponse.getResults().get(1).getActivityDate());
        assertEquals(LocalDateTime.parse("2022-03-01T00:00:00"), searchResponse.getResults().get(2).getActivityDate());
        assertEquals(LocalDateTime.parse("2022-02-01T00:00:00"), searchResponse.getResults().get(3).getActivityDate());
        assertEquals(LocalDateTime.parse("2022-01-01T00:00:00"), searchResponse.getResults().get(4).getActivityDate());
    }

    @Test
    public void search_noQuestionableActivitiesWithSearchTerm_returnsEmptyResponse() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(1).setDeveloperName("developer name");
        searchResults.get(2).setProductName("product name");
        searchResults.get(3).setChplProductNumber("15.02.02.3007.A056.01.00.0.180214");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("search term")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_questionableActivityDeveloperNameMatchesSearchTerm_returnsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(1).setDeveloperName("developer name");
        searchResults.get(2).setProductName("product name");
        searchResults.get(3).setChplProductNumber("15.02.02.3007.A056.01.00.0.180214");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("dev")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertTrue(searchResponse.getResults().get(0).getDeveloperName().contains("dev"));
    }

    @Test
    public void search_questionableActivityProductNameMatchesSearchTerm_returnsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(1).setDeveloperName("developer name");
        searchResults.get(2).setProductName("product name");
        searchResults.get(3).setChplProductNumber("15.02.02.3007.A056.01.00.0.180214");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("prod")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertTrue(searchResponse.getResults().get(0).getProductName().contains("prod"));
    }

    @Test
    public void search_questionableActivityChplProductNumberMatchesSearchTerm_returnsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(1).setDeveloperName("developer name");
        searchResults.get(2).setProductName("product name");
        searchResults.get(3).setChplProductNumber("15.02.02.3007.A056.01.00.0.180214");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("3007")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertTrue(searchResponse.getResults().get(0).getChplProductNumber().contains("3007"));
    }

    @Test
    public void search_questionableActivityDeveloperAndProductMatchesSearchTerm_returnsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(1).setDeveloperName("developer name");
        searchResults.get(2).setProductName("product name");
        searchResults.get(3).setChplProductNumber("15.02.02.3007.A056.01.00.0.180214");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .searchTerm("name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_questionableActivitySingleTriggerId_returnsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(1).setTriggerLevel("Developer");
        searchResults.get(1).setTriggerName("Developer Name Changed");
        searchResults.get(2).setTriggerLevel("Product");
        searchResults.get(2).setTriggerName("Product Name Changed");
        searchResults.get(3).setTriggerLevel("Listing");
        searchResults.get(3).setTriggerName("Certification Status Changed");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .triggerIds(Stream.of(1L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_questionableActivityMultipleTriggersId_returnsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(1).setTriggerLevel("Developer");
        searchResults.get(1).setTriggerName("Developer Name Changed");
        searchResults.get(2).setTriggerLevel("Product");
        searchResults.get(2).setTriggerName("Product Name Changed");
        searchResults.get(3).setTriggerLevel("Listing");
        searchResults.get(3).setTriggerName("Certification Status Changed");
        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .triggerIds(Stream.of(1L, 2L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }


    @Test
    public void search_activityStartDateEqualsActivityDate_findsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2020-06-25T05:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2020-06-01T05:00:00"));

        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .activityDateStart("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_activityStartDateBeforeActivityDate_findsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2020-06-25T05:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2020-06-01T05:00:00"));

        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .activityDateStart("2020-06-24")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_activityEndDateEqualsActivityDate_findsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2020-06-25T05:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2020-06-27T05:00:00"));

        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .activityDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_activityEndDateAfterActivityDate_findsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2020-06-25T05:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2020-06-27T05:00:00"));

        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .activityDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_questionableActivityDateBetweenStartAndEnd_findsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2020-06-25T05:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2020-06-27T05:00:00"));

        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .activityDateStart("2020-06-24")
            .activityDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_questionableActivityDateEqualsStartAndBeforeEnd_findsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2020-06-25T05:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2020-06-27T05:00:00"));

        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .activityDateStart("2020-06-25")
            .activityDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_questionableActivityDateEqualsEndAndAfterStart_findsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2020-06-25T05:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2020-06-27T05:00:00"));

        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .activityDateStart("2020-06-24")
            .activityDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_questionableActivityDateEqualsEndAndStart_findsMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2020-06-25T05:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2020-06-27T05:00:00"));

        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .activityDateStart("2020-06-25")
            .activityDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_questionableActivityDateEndBeforeStart_findsNoMatches() throws ValidationException {
        List<QuestionableActivity> searchResults = createSearchResultCollection(50);
        searchResults.get(0).setActivityDate(LocalDateTime.parse("2020-06-25T05:00:00"));
        searchResults.get(1).setActivityDate(LocalDateTime.parse("2020-06-27T05:00:00"));
        searchResults.get(2).setActivityDate(LocalDateTime.parse("2020-06-01T05:00:00"));

        Mockito.when(questionableActivitySearchDao.getAll()).thenReturn(searchResults);
        SearchRequest searchRequest = SearchRequest.builder()
            .activityDateStart("2020-07-01")
            .activityDateEnd("2020-06-01")
            .pageNumber(0)
            .pageSize(10)
        .build();
        QuestionableActivitySearchResponse searchResponse = questionableActivitySearchService.searchQuestionableActivities(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    private List<QuestionableActivity> createSearchResultCollection(int collectionSize) {
        List<QuestionableActivity> searchResults = new ArrayList<QuestionableActivity>();
        for (int i = 0; i < collectionSize; i++) {
            searchResults.add(new QuestionableActivity());
        }
        return searchResults;
    }
}
