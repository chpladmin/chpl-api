package gov.healthit.chpl.developer.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.DeveloperManager;

public class DeveloperSearchServiceV2Test {

    private DeveloperManager developerManager;
    private DeveloperSearchServiceV2 developerSearchService;

    @Before
    public void setup() {
        SearchRequestValidatorV2 searchRequestValidator = Mockito.mock(SearchRequestValidatorV2.class);
        developerManager = Mockito.mock(DeveloperManager.class);

        developerSearchService = new DeveloperSearchServiceV2(searchRequestValidator,
                developerManager);
    }

    @Test
    public void search_validEmptySearchRequest_findsAllDevelopers() throws ValidationException {
        Mockito.when(developerManager.getDeveloperSearchResultsV2())
            .thenReturn(createDeveloperSearchResultCollection(100));
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(10, searchResponse.getResults().size());
    }

    @Test
    public void search_pageOutOfRangeSearchRequest_returnsEmptyResponse() throws ValidationException {
        Mockito.when(developerManager.getDeveloperSearchResultsV2())
            .thenReturn(createDeveloperSearchResultCollection(100));
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .pageNumber(2)
            .pageSize(100)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_sortByDeveloperAscending_ordersResults() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(5);
        allDevelopers.get(0).setName("z");
        allDevelopers.get(1).setName("b");
        allDevelopers.get(2).setName("d");
        allDevelopers.get(3).setName("f");
        allDevelopers.get(4).setName("y");
        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getName());
        assertEquals("d", searchResponse.getResults().get(1).getName());
        assertEquals("f", searchResponse.getResults().get(2).getName());
        assertEquals("y", searchResponse.getResults().get(3).getName());
        assertEquals("z", searchResponse.getResults().get(4).getName());
    }

    @Test
    public void search_sortByDeveloperDescending_ordersResults() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(5);
        allDevelopers.get(0).setName("z");
        allDevelopers.get(1).setName("b");
        allDevelopers.get(2).setName("d");
        allDevelopers.get(3).setName("f");
        allDevelopers.get(4).setName("y");
        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getName());
        assertEquals("y", searchResponse.getResults().get(1).getName());
        assertEquals("f", searchResponse.getResults().get(2).getName());
        assertEquals("d", searchResponse.getResults().get(3).getName());
        assertEquals("b", searchResponse.getResults().get(4).getName());
    }

    @Test
    public void search_sortByDecertificationDateDescending_ordersResults() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(5);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("1970-01-01"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("1980-01-01"));
        allDevelopers.get(2).setDecertificationDate(LocalDate.parse("1970-02-01"));
        allDevelopers.get(3).setDecertificationDate(LocalDate.parse("2022-01-01"));
        allDevelopers.get(4).setDecertificationDate(LocalDate.parse("1970-01-15"));
        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.DECERTIFICATION_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDate.parse("2022-01-01"), searchResponse.getResults().get(0).getDecertificationDate());
        assertEquals(LocalDate.parse("1980-01-01"), searchResponse.getResults().get(1).getDecertificationDate());
        assertEquals(LocalDate.parse("1970-02-01"), searchResponse.getResults().get(2).getDecertificationDate());
        assertEquals(LocalDate.parse("1970-01-15"), searchResponse.getResults().get(3).getDecertificationDate());
        assertEquals(LocalDate.parse("1970-01-01"), searchResponse.getResults().get(4).getDecertificationDate());
    }

    @Test
    public void search_sortByDecertificationDateAscending_ordersResults() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(5);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("1970-01-01"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("1980-01-01"));
        allDevelopers.get(2).setDecertificationDate(LocalDate.parse("1970-02-01"));
        allDevelopers.get(3).setDecertificationDate(LocalDate.parse("2022-01-01"));
        allDevelopers.get(4).setDecertificationDate(LocalDate.parse("1970-01-15"));
        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.DECERTIFICATION_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDate.parse("1970-01-01"), searchResponse.getResults().get(0).getDecertificationDate());
        assertEquals(LocalDate.parse("1970-01-15"), searchResponse.getResults().get(1).getDecertificationDate());
        assertEquals(LocalDate.parse("1970-02-01"), searchResponse.getResults().get(2).getDecertificationDate());
        assertEquals(LocalDate.parse("1980-01-01"), searchResponse.getResults().get(3).getDecertificationDate());
        assertEquals(LocalDate.parse("2022-01-01"), searchResponse.getResults().get(4).getDecertificationDate());
    }

    @Test
    public void search_sortByStatusAscending_ordersResults() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(5);
        allDevelopers.get(1).setStatus(status(DeveloperStatusType.SuspendedByOnc.getName()));
        allDevelopers.get(4).setStatus(status(DeveloperStatusType.UnderCertificationBanByOnc.getName()));
        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(null, searchResponse.getResults().get(0).getStatus());
        assertEquals(null, searchResponse.getResults().get(1).getStatus());
        assertEquals(null, searchResponse.getResults().get(2).getStatus());
        assertEquals(DeveloperStatusType.SuspendedByOnc.getName(), searchResponse.getResults().get(3).getStatus().getName());
        assertEquals(DeveloperStatusType.UnderCertificationBanByOnc.getName(), searchResponse.getResults().get(4).getStatus().getName());
    }

    @Test
    public void search_sortByCertificationStatusDescending_ordersResults() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(5);
        allDevelopers.get(1).setStatus(status(DeveloperStatusType.SuspendedByOnc.getName()));
        allDevelopers.get(4).setStatus(status(DeveloperStatusType.UnderCertificationBanByOnc.getName()));
        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(DeveloperStatusType.UnderCertificationBanByOnc.getName(), searchResponse.getResults().get(0).getStatus().getName());
        assertEquals(DeveloperStatusType.SuspendedByOnc.getName(), searchResponse.getResults().get(1).getStatus().getName());
        assertEquals(null, searchResponse.getResults().get(2).getStatus());
        assertEquals(null, searchResponse.getResults().get(3).getStatus());
        assertEquals(null, searchResponse.getResults().get(4).getStatus());
    }

    @Test
    public void search_singleAcbNameProvidedAndDeveloperHasSingleAcb_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setAssociatedAcbs(Stream.of(acb("ACB 1")).collect(Collectors.toSet()));
        allDevelopers.get(1).setAssociatedAcbs(Stream.of(acb("ACB 2")).collect(Collectors.toSet()));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        Set<String> acbNames = new LinkedHashSet<String>();
        acbNames.add("ACB 1");
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .certificationBodies(acbNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_singleAcbNameProvidedAndDeveloperHasMultipleAcbs_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setAssociatedAcbs(Stream.of(acb("ACB 1")).collect(Collectors.toSet()));
        allDevelopers.get(1).setAssociatedAcbs(Stream.of(acb("ACB 2"), acb("ACB 1")).collect(Collectors.toSet()));
        allDevelopers.get(2).setAssociatedAcbs(Stream.of(acb("ACB 2")).collect(Collectors.toSet()));
        allDevelopers.get(3).setAssociatedAcbs(Stream.of(acb("ACB 2"), acb("ACB 3")).collect(Collectors.toSet()));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        Set<String> acbNames = new LinkedHashSet<String>();
        acbNames.add("ACB 1");
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .certificationBodies(acbNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_mutlipleAcbNamesProvidedDeveloperHasSingleAcb_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setAssociatedAcbs(Stream.of(acb("ACB 1")).collect(Collectors.toSet()));
        allDevelopers.get(1).setAssociatedAcbs(Stream.of(acb("ACB 2")).collect(Collectors.toSet()));
        allDevelopers.get(2).setAssociatedAcbs(Stream.of(acb("ACB 1")).collect(Collectors.toSet()));
        allDevelopers.get(3).setAssociatedAcbs(Stream.of(acb("ACB 5")).collect(Collectors.toSet()));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        Set<String> acbNames = new LinkedHashSet<String>();
        acbNames.add("ACB 1");
        acbNames.add("ACB 2");
        acbNames.add("ACB 3");
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .certificationBodies(acbNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_mutlipleAcbNamesProvidedDeveloperHasMultipleAcbs_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setAssociatedAcbs(Stream.of(acb("ACB 1")).collect(Collectors.toSet()));
        allDevelopers.get(1).setAssociatedAcbs(Stream.of(acb("ACB 2"), acb("ACB 3")).collect(Collectors.toSet()));
        allDevelopers.get(2).setAssociatedAcbs(Stream.of(acb("ACB 1")).collect(Collectors.toSet()));
        allDevelopers.get(3).setAssociatedAcbs(Stream.of(acb("ACB 5"), acb("ACB 1")).collect(Collectors.toSet()));
        allDevelopers.get(4).setAssociatedAcbs(Stream.of(acb("ACB 0"), acb("ACB 7")).collect(Collectors.toSet()));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        Set<String> acbNames = new LinkedHashSet<String>();
        acbNames.add("ACB 1");
        acbNames.add("ACB 2");
        acbNames.add("ACB 3");
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .certificationBodies(acbNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(4, searchResponse.getRecordCount());
        assertEquals(4, searchResponse.getResults().size());
    }

    @Test
    public void search_singleStatusProvided_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(1).setStatus(status(DeveloperStatusType.SuspendedByOnc.getName()));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        Set<String> statusNames = new LinkedHashSet<String>();
        statusNames.add(DeveloperStatusType.SuspendedByOnc.getName());
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .statuses(statusNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleStatusesProvided_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(1).setStatus(status(DeveloperStatusType.SuspendedByOnc.getName()));
        allDevelopers.get(3).setStatus(status(DeveloperStatusType.UnderCertificationBanByOnc.getName()));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        Set<String> statusNames = new LinkedHashSet<String>();
        statusNames.add(DeveloperStatusType.SuspendedByOnc.getName());
        statusNames.add(DeveloperStatusType.UnderCertificationBanByOnc.getName());

        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .statuses(statusNames)
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsMatchesOnDeveloperName() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setName("dev name");
        allDevelopers.get(1).setName("long DEV name here");
        allDevelopers.get(2).setName("doesn't match");

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .searchTerm("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsMatchesOnDeveloperCode() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setCode("dev name");
        allDevelopers.get(1).setCode("long DEV name here");
        allDevelopers.get(2).setCode("doesn't match");

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .searchTerm("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvided_findsMatchesOnDeveloperNameAndCode() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setCode("dev name");
        allDevelopers.get(1).setCode("long DEV name here");
        allDevelopers.get(2).setCode("doesn't match");
        allDevelopers.get(2).setName("dev name 2");

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .searchTerm("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_developeNameProvided_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setName("dev name");
        allDevelopers.get(1).setName("long DEV name here");
        allDevelopers.get(2).setName("doesn't match");

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .developerName("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_developeCodeProvided_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setCode("1234");
        allDevelopers.get(1).setCode("2345");
        allDevelopers.get(2).setCode("0000");

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .developerCode("2345")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_decertificationStartDateAfterDecertificationDateEnd_noMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-06-01"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-07-01"));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .decertificationDateStart("2020-07-01")
            .decertificationDateEnd("2020-06-01")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_decertificationStartDateEqualsDeveloperDecertificationDate_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-06-01"));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .decertificationDateStart("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_decertificationStartDateBeforeDeveloperDecertificationDate_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-06-01"));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .decertificationDateStart("2020-06-24")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_decertificationEndDateEqualsDeveloperDecertificationDate_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .decertificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_decertificationEndDateAfterDeveloperDecertificationDate_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .decertificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_developerDecertificationDateBetweenStartAndEnd_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .decertificationDateStart("2020-06-24")
            .decertificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_developerDecertificationDateEqualsStartAndBeforeEnd_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .decertificationDateStart("2020-06-25")
            .decertificationDateEnd("2020-06-26")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_developerDecertificationDateEqualsEndAndAfterStart_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .decertificationDateStart("2020-06-24")
            .decertificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_developerDecertificationDateEqualsEndAndStart_findsMatches() throws ValidationException {
        List<DeveloperSearchResultV2> allDevelopers = createDeveloperSearchResultCollection(50);
        allDevelopers.get(0).setDecertificationDate(LocalDate.parse("2020-06-25"));
        allDevelopers.get(1).setDecertificationDate(LocalDate.parse("2020-06-27"));

        Mockito.when(developerManager.getDeveloperSearchResultsV2()).thenReturn(allDevelopers);
        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
            .decertificationDateStart("2020-06-25")
            .decertificationDateEnd("2020-06-25")
            .pageNumber(0)
            .pageSize(10)
        .build();
        DeveloperSearchResponseV2 searchResponse = developerSearchService.findDevelopers(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    private List<DeveloperSearchResultV2> createDeveloperSearchResultCollection(int collectionSize) {
        List<DeveloperSearchResultV2> developers = new ArrayList<DeveloperSearchResultV2>();
        for (int i = 0; i < collectionSize; i++) {
            developers.add(new DeveloperSearchResultV2());
        }
        return developers;
    }

    private IdNamePair acb(String name) {
        return IdNamePair.builder()
                .name(name)
                .build();
    }

    private IdNamePair status(String name) {
        return IdNamePair.builder()
                .name(name)
                .build();
    }
}