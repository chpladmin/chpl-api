package gov.healthit.chpl.changerequest.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResult.CurrentStatusSearchResult;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class ChangeRequestSearchManagerTest {

    private ChangeRequestSearchManager changeRequestSearchManager;
    private ChangeRequestDAO changeRequestDao;

    @Before
    public void setup() {
        ChangeRequestSearchRequestValidator searchRequestValidator = Mockito.mock(ChangeRequestSearchRequestValidator.class);
        changeRequestDao = Mockito.mock(ChangeRequestDAO.class);
        ResourcePermissions resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);

        changeRequestSearchManager = new ChangeRequestSearchManager(null, null, changeRequestDao,
                searchRequestValidator, resourcePermissions);
    }

    @Test
    public void search_validEmptySearchRequest_findsAllListings() throws ValidationException {
        Mockito.when(changeRequestDao.getAll())
            .thenReturn(createChangeRequestSearchResultCollection(100));
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(10, searchResponse.getResults().size());
    }

    @Test
    public void search_pageOutOfRangeSearchRequest_returnsEmptyResponse() throws ValidationException {
        Mockito.when(changeRequestDao.getAll())
            .thenReturn(createChangeRequestSearchResultCollection(100));
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .pageNumber(2)
            .pageSize(100)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(100, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_noChangeRequestsWithSearchTerm_returnsEmptyResponse() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setDeveloper(developer(1L, "Epic"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .searchTerm("Sandwich")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_sortByCertificationBodiesAscending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setCertificationBodies(Stream.of(acb(1L, "Drummond")).collect(Collectors.toList()));
        allChangeRequests.get(1).setCertificationBodies(Stream.of(acb(1L, "Drummond"), acb(2L, "ICSA Labs")).collect(Collectors.toList()));
        allChangeRequests.get(2).setCertificationBodies(Stream.of(acb(2L, "ICSA Labs")).collect(Collectors.toList()));
        allChangeRequests.get(3).setCertificationBodies(Stream.of(acb(4L, "ZZZ"), acb(5L, "RRR")).collect(Collectors.toList()));
        allChangeRequests.get(4).setCertificationBodies(Stream.of(acb(3L, "AAA")).collect(Collectors.toList()));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CERTIFICATION_BODIES)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(4L, searchResponse.getResults().get(0).getId());
        assertEquals(0L, searchResponse.getResults().get(1).getId());
        assertEquals(1L, searchResponse.getResults().get(2).getId());
        assertEquals(2L, searchResponse.getResults().get(3).getId());
        assertEquals(3L, searchResponse.getResults().get(4).getId());
    }

    @Test
    public void search_sortByCertificationBodiesDescending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setCertificationBodies(Stream.of(acb(1L, "Drummond")).collect(Collectors.toList()));
        allChangeRequests.get(1).setCertificationBodies(Stream.of(acb(1L, "Drummond"), acb(2L, "ICSA Labs")).collect(Collectors.toList()));
        allChangeRequests.get(2).setCertificationBodies(Stream.of(acb(2L, "ICSA Labs")).collect(Collectors.toList()));
        allChangeRequests.get(3).setCertificationBodies(Stream.of(acb(4L, "ZZZ"), acb(5L, "RRR")).collect(Collectors.toList()));
        allChangeRequests.get(4).setCertificationBodies(Stream.of(acb(3L, "AAA")).collect(Collectors.toList()));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CERTIFICATION_BODIES)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(3L, searchResponse.getResults().get(0).getId());
        assertEquals(2L, searchResponse.getResults().get(1).getId());
        assertEquals(1L, searchResponse.getResults().get(2).getId());
        assertEquals(0L, searchResponse.getResults().get(3).getId());
        assertEquals(4L, searchResponse.getResults().get(4).getId());
    }

    @Test
    public void search_sortByDeveloperAscending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setDeveloper(developer(1L, "z"));
        allChangeRequests.get(1).setDeveloper(developer(2L, "b"));
        allChangeRequests.get(2).setDeveloper(developer(3L, "d"));
        allChangeRequests.get(3).setDeveloper(developer(4L, "f"));
        allChangeRequests.get(4).setDeveloper(developer(5L, "y"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

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
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setDeveloper(developer(1L, "z"));
        allChangeRequests.get(1).setDeveloper(developer(2L, "b"));
        allChangeRequests.get(2).setDeveloper(developer(3L, "d"));
        allChangeRequests.get(3).setDeveloper(developer(4L, "f"));
        allChangeRequests.get(4).setDeveloper(developer(5L, "y"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.DEVELOPER)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

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
    public void search_sortByChangeRequestTypeAscending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setChangeRequestType(type(1L, "z"));
        allChangeRequests.get(1).setChangeRequestType(type(2L, "b"));
        allChangeRequests.get(2).setChangeRequestType(type(3L, "d"));
        allChangeRequests.get(3).setChangeRequestType(type(4L, "f"));
        allChangeRequests.get(4).setChangeRequestType(type(5L, "y"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CHANGE_REQUEST_TYPE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getChangeRequestType().getName());
        assertEquals("d", searchResponse.getResults().get(1).getChangeRequestType().getName());
        assertEquals("f", searchResponse.getResults().get(2).getChangeRequestType().getName());
        assertEquals("y", searchResponse.getResults().get(3).getChangeRequestType().getName());
        assertEquals("z", searchResponse.getResults().get(4).getChangeRequestType().getName());
    }

    @Test
    public void search_sortByChangeRequestTypeDescending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setChangeRequestType(type(1L, "z"));
        allChangeRequests.get(1).setChangeRequestType(type(2L, "b"));
        allChangeRequests.get(2).setChangeRequestType(type(3L, "d"));
        allChangeRequests.get(3).setChangeRequestType(type(4L, "f"));
        allChangeRequests.get(4).setChangeRequestType(type(5L, "y"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CHANGE_REQUEST_TYPE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getChangeRequestType().getName());
        assertEquals("y", searchResponse.getResults().get(1).getChangeRequestType().getName());
        assertEquals("f", searchResponse.getResults().get(2).getChangeRequestType().getName());
        assertEquals("d", searchResponse.getResults().get(3).getChangeRequestType().getName());
        assertEquals("b", searchResponse.getResults().get(4).getChangeRequestType().getName());
    }

    @Test
    public void search_sortByChangeRequestCurrentStatusAscending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "z"));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "b"));
        allChangeRequests.get(2).setCurrentStatus(status(3L, "d"));
        allChangeRequests.get(3).setCurrentStatus(status(4L, "f"));
        allChangeRequests.get(4).setCurrentStatus(status(5L, "y"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CHANGE_REQUEST_STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getCurrentStatus().getName());
        assertEquals("d", searchResponse.getResults().get(1).getCurrentStatus().getName());
        assertEquals("f", searchResponse.getResults().get(2).getCurrentStatus().getName());
        assertEquals("y", searchResponse.getResults().get(3).getCurrentStatus().getName());
        assertEquals("z", searchResponse.getResults().get(4).getCurrentStatus().getName());
    }

    @Test
    public void search_sortByChangeRequestCurrentStatusDescending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "z"));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "b"));
        allChangeRequests.get(2).setCurrentStatus(status(3L, "d"));
        allChangeRequests.get(3).setCurrentStatus(status(4L, "f"));
        allChangeRequests.get(4).setCurrentStatus(status(5L, "y"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CHANGE_REQUEST_STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals("z", searchResponse.getResults().get(0).getCurrentStatus().getName());
        assertEquals("y", searchResponse.getResults().get(1).getCurrentStatus().getName());
        assertEquals("f", searchResponse.getResults().get(2).getCurrentStatus().getName());
        assertEquals("d", searchResponse.getResults().get(3).getCurrentStatus().getName());
        assertEquals("b", searchResponse.getResults().get(4).getCurrentStatus().getName());
    }

    @Test
    public void search_sortBySubmittedDateTimeDescending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("1970-01-01T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("1980-01-01T00:00"));
        allChangeRequests.get(2).setSubmittedDateTime(LocalDateTime.parse("1970-02-01T00:00"));
        allChangeRequests.get(3).setSubmittedDateTime(LocalDateTime.parse("2022-01-01T00:00"));
        allChangeRequests.get(4).setSubmittedDateTime(LocalDateTime.parse("1970-01-01T02:00"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.SUBMITTED_DATE_TIME)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDateTime.parse("2022-01-01T00:00"), searchResponse.getResults().get(0).getSubmittedDateTime());
        assertEquals(LocalDateTime.parse("1980-01-01T00:00"), searchResponse.getResults().get(1).getSubmittedDateTime());
        assertEquals(LocalDateTime.parse("1970-02-01T00:00"), searchResponse.getResults().get(2).getSubmittedDateTime());
        assertEquals(LocalDateTime.parse("1970-01-01T02:00"), searchResponse.getResults().get(3).getSubmittedDateTime());
        assertEquals(LocalDateTime.parse("1970-01-01T00:00"), searchResponse.getResults().get(4).getSubmittedDateTime());
    }

    @Test
    public void search_sortBySubmittedDateTimeAscending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("1970-01-01T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("1980-01-01T00:00"));
        allChangeRequests.get(2).setSubmittedDateTime(LocalDateTime.parse("1970-02-01T00:00"));
        allChangeRequests.get(3).setSubmittedDateTime(LocalDateTime.parse("2022-01-01T00:00"));
        allChangeRequests.get(4).setSubmittedDateTime(LocalDateTime.parse("1970-01-01T02:00"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.SUBMITTED_DATE_TIME)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDateTime.parse("1970-01-01T00:00"), searchResponse.getResults().get(0).getSubmittedDateTime());
        assertEquals(LocalDateTime.parse("1970-01-01T02:00"), searchResponse.getResults().get(1).getSubmittedDateTime());
        assertEquals(LocalDateTime.parse("1970-02-01T00:00"), searchResponse.getResults().get(2).getSubmittedDateTime());
        assertEquals(LocalDateTime.parse("1980-01-01T00:00"), searchResponse.getResults().get(3).getSubmittedDateTime());
        assertEquals(LocalDateTime.parse("2022-01-01T00:00"), searchResponse.getResults().get(4).getSubmittedDateTime());
    }

    @Test
    public void search_sortByCurrentStatusChangeDateTimeDescending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "a", LocalDateTime.parse("1970-01-01T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "b", LocalDateTime.parse("1980-01-01T00:00")));
        allChangeRequests.get(2).setCurrentStatus(status(1L, "a", LocalDateTime.parse("1970-02-01T00:00")));
        allChangeRequests.get(3).setCurrentStatus(status(1L, "a", LocalDateTime.parse("2022-01-01T00:00")));
        allChangeRequests.get(4).setCurrentStatus(status(2L, "b", LocalDateTime.parse("1970-01-01T02:00")));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CURRENT_STATUS_CHANGE_DATE_TIME)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDateTime.parse("2022-01-01T00:00"), searchResponse.getResults().get(0).getCurrentStatus().getStatusChangeDateTime());
        assertEquals(LocalDateTime.parse("1980-01-01T00:00"), searchResponse.getResults().get(1).getCurrentStatus().getStatusChangeDateTime());
        assertEquals(LocalDateTime.parse("1970-02-01T00:00"), searchResponse.getResults().get(2).getCurrentStatus().getStatusChangeDateTime());
        assertEquals(LocalDateTime.parse("1970-01-01T02:00"), searchResponse.getResults().get(3).getCurrentStatus().getStatusChangeDateTime());
        assertEquals(LocalDateTime.parse("1970-01-01T00:00"), searchResponse.getResults().get(4).getCurrentStatus().getStatusChangeDateTime());
    }

    @Test
    public void search_sortByCurrentStatusChangeDateTimeAscending_ordersResults() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(5);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "a", LocalDateTime.parse("1970-01-01T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "b", LocalDateTime.parse("1980-01-01T00:00")));
        allChangeRequests.get(2).setCurrentStatus(status(1L, "a", LocalDateTime.parse("1970-02-01T00:00")));
        allChangeRequests.get(3).setCurrentStatus(status(1L, "a", LocalDateTime.parse("2022-01-01T00:00")));
        allChangeRequests.get(4).setCurrentStatus(status(2L, "b", LocalDateTime.parse("1970-01-01T02:00")));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CURRENT_STATUS_CHANGE_DATE_TIME)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(5, searchResponse.getRecordCount());
        assertEquals(5, searchResponse.getResults().size());
        assertEquals(LocalDateTime.parse("1970-01-01T00:00"), searchResponse.getResults().get(0).getCurrentStatus().getStatusChangeDateTime());
        assertEquals(LocalDateTime.parse("1970-01-01T02:00"), searchResponse.getResults().get(1).getCurrentStatus().getStatusChangeDateTime());
        assertEquals(LocalDateTime.parse("1970-02-01T00:00"), searchResponse.getResults().get(2).getCurrentStatus().getStatusChangeDateTime());
        assertEquals(LocalDateTime.parse("1980-01-01T00:00"), searchResponse.getResults().get(3).getCurrentStatus().getStatusChangeDateTime());
        assertEquals(LocalDateTime.parse("2022-01-01T00:00"), searchResponse.getResults().get(4).getCurrentStatus().getStatusChangeDateTime());
    }

    @Test
    public void search_searchTermProvided_findsChangeRequestsWithMatchingDeveloperNames() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setDeveloper(developer(1L, "dev name"));
        allChangeRequests.get(1).setDeveloper(developer(2L, "long DEV name here"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .searchTerm("dev name")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_developerIdProvided_findsChangeRequestsWithMatchingDeveloperIds() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setDeveloper(developer(1L, "dev name"));
        allChangeRequests.get(1).setDeveloper(developer(2L, "long DEV name here"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .developerId(1L)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_singleAcbIdProvided_findsChangeRequestsWithMatchingAcbIds() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCertificationBodies(Stream.of(acb(1L, "acb1")).collect(Collectors.toList()));
        allChangeRequests.get(1).setCertificationBodies(Stream.of(acb(2L, "acb2")).collect(Collectors.toList()));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .acbIds(Stream.of(1L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertEquals(1L, searchResponse.getResults().get(0).getCertificationBodies().get(0).getId());
    }

    @Test
    public void search_singleAcbIdProvidedAndChangeRequestHasMultiple_findsChangeRequestsWithMatchingAcbIds() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCertificationBodies(Stream.of(acb(1L, "acb1"), acb(2L, "acb2")).collect(Collectors.toList()));
        allChangeRequests.get(1).setCertificationBodies(Stream.of(acb(2L, "acb2")).collect(Collectors.toList()));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .acbIds(Stream.of(1L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertEquals(1L, searchResponse.getResults().get(0).getCertificationBodies().get(0).getId());
    }

    @Test
    public void search_multipleAcbIdsProvidedAndChangeRequestHasMultiple_findsChangeRequestsWithMatchingAcbIds() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCertificationBodies(Stream.of(acb(1L, "acb1"), acb(2L, "acb2")).collect(Collectors.toList()));
        allChangeRequests.get(1).setCertificationBodies(Stream.of(acb(2L, "acb2")).collect(Collectors.toList()));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .acbIds(Stream.of(1L, 2L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_multipleAcbIdsProvidedAndNoMatches_findsNoMatches() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCertificationBodies(Stream.of(acb(1L, "acb1"), acb(2L, "acb2")).collect(Collectors.toList()));
        allChangeRequests.get(1).setCertificationBodies(Stream.of(acb(2L, "acb2")).collect(Collectors.toList()));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .acbIds(Stream.of(3L, 4L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_singleCurrentStatusProvided_findsMatchingChangeRequests() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Accepted"));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "Rejected"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);

        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusNames(Stream.of("Rejected").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertEquals("Rejected", searchResponse.getResults().get(0).getCurrentStatus().getName());
    }

    @Test
    public void search_multipleCurrentStatusesProvided_findsMatchingChangeRequests() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Accepted"));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "Rejected"));
        allChangeRequests.get(2).setCurrentStatus(status(1L, "Accepted"));
        allChangeRequests.get(3).setCurrentStatus(status(2L, "Pending"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);

        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusNames(Stream.of("Rejected", "Accepted").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_singleTypeProvided_findsMatchingChangeRequests() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setChangeRequestType(type(1L, "Demographic"));
        allChangeRequests.get(1).setChangeRequestType(type(2L, "Attestation"));
        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);

        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .changeRequestTypeNames(Stream.of("Attestation").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertEquals("Attestation", searchResponse.getResults().get(0).getChangeRequestType().getName());
    }

    @Test
    public void search_multipleTypesProvided_findsMatchingChangeRequests() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setChangeRequestType(type(1L, "Demographic"));
        allChangeRequests.get(1).setChangeRequestType(type(2L, "Attestation"));
        allChangeRequests.get(2).setChangeRequestType(type(1L, "Attestation"));
        allChangeRequests.get(3).setChangeRequestType(type(2L, "Other"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);

        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .changeRequestTypeNames(Stream.of("Demographic", "Attestation").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeStartEqualsChangeRequestSubmittedDate_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("1980-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2020-06-25T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeStartBeforeChangeRequestSubmittedDate_findsMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("1980-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2020-06-24T23:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeStartAfterChangeRequestSubmittedDate_findsNothing() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("1980-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2021-06-24T23:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeEndEqualsChangeRequestSubmittedDate_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("2022-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeEnd("2020-06-25T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeEndAfterChangeRequestSubmittedDate_findsMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("2022-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeEnd("2021-06-24T23:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeEndBeforeChangeRequestSubmittedDate_findsNothing() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("2020-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeEnd("2019-06-24T23:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeBetweenStartAndEnd_findsMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("2020-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2020-06-24T00:00")
            .submittedDateTimeEnd("2020-06-26T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeEqualsStartAndBeforeEnd_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("2020-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2020-06-25T00:00")
            .submittedDateTimeEnd("2020-06-26T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeEqualsEndAndAfterStart_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("2020-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2020-06-24T12:30")
            .submittedDateTimeEnd("2020-06-25T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_submittedDateTimeEqualsEndAndStart_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setSubmittedDateTime(LocalDateTime.parse("2020-06-25T00:00"));
        allChangeRequests.get(1).setSubmittedDateTime(LocalDateTime.parse("2020-01-01T00:00"));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .submittedDateTimeStart("2020-06-25T00:00")
            .submittedDateTimeEnd("2020-06-25T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeStartEqualsChangeRequestStatusChangeDateTime_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Approved", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "Open", LocalDateTime.parse("1980-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2020-06-25T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeStartBeforeChangeRequestStatusChangeDateTime_findsMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "Open", LocalDateTime.parse("1980-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2020-06-24T23:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeStartAfterChangeRequestChangeDateTime_findsNothing() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "Open", LocalDateTime.parse("1980-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2021-06-24T23:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeEndEqualsChangeRequestStatusChangeDateTime_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(2L, "Rejected", LocalDateTime.parse("2022-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeEnd("2020-06-25T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeEndAfterChangeRequestStatusChangeDateTime_findsMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2022-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeEnd("2021-06-24T23:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeEndBeforeChangeRequestStatusChangeDateTime_findsNothing() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeEnd("2019-06-24T23:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeBetweenStartAndEnd_findsMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2020-06-24T00:00")
            .currentStatusChangeDateTimeEnd("2020-06-26T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeEqualsStartAndBeforeEnd_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2020-06-25T00:00")
            .currentStatusChangeDateTimeEnd("2020-06-26T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeEqualsEndAndAfterStart_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2020-06-24T12:30")
            .currentStatusChangeDateTimeEnd("2020-06-25T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_statusChangeDateTimeEqualsEndAndStart_findsNoMatching() throws ValidationException {
        List<ChangeRequestSearchResult> allChangeRequests = createChangeRequestSearchResultCollection(50);
        allChangeRequests.get(0).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-06-25T00:00")));
        allChangeRequests.get(1).setCurrentStatus(status(1L, "Active", LocalDateTime.parse("2020-01-01T00:00")));

        Mockito.when(changeRequestDao.getAll()).thenReturn(allChangeRequests);
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
            .currentStatusChangeDateTimeStart("2020-06-25T00:00")
            .currentStatusChangeDateTimeEnd("2020-06-25T00:00")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ChangeRequestSearchResponse searchResponse = changeRequestSearchManager.searchChangeRequests(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    private List<ChangeRequestSearchResult> createChangeRequestSearchResultCollection(int collectionSize) {
        List<ChangeRequestSearchResult> changeRequests = new ArrayList<ChangeRequestSearchResult>();
        for (int i = 0; i < collectionSize; i++) {
            changeRequests.add(ChangeRequestSearchResult.builder()
                    .id(Long.valueOf(i))
                    .build());
        }
        return changeRequests;
    }

    private IdNamePair developer(Long id, String name) {
        return IdNamePair.builder()
                .id(id)
                .name(name)
                .build();
    }

    private IdNamePair acb(Long id, String name) {
        return IdNamePair.builder()
                .id(id)
                .name(name)
                .build();
    }

    private IdNamePair type(Long id, String name) {
        return IdNamePair.builder()
                .id(id)
                .name(name)
                .build();
    }

    private CurrentStatusSearchResult status(Long id, String name) {
        return CurrentStatusSearchResult.builder()
                .id(id)
                .name(name)
                .build();
    }

    private CurrentStatusSearchResult status(Long id, String name, LocalDateTime localDateTime) {
        return CurrentStatusSearchResult.builder()
                .id(id)
                .name(name)
                .statusChangeDateTime(localDateTime)
                .build();
    }
}
