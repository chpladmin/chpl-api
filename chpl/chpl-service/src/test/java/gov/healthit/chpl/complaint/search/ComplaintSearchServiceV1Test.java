package gov.healthit.chpl.complaint.search;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.complaint.ComplaintDAO;
import gov.healthit.chpl.complaint.domain.ComplainantType;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.complaint.domain.ComplaintCriterionMap;
import gov.healthit.chpl.complaint.domain.ComplaintListingMap;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.permissions.ResourcePermissions;

public class ComplaintSearchServiceV1Test {
    private ResourcePermissions resourcePermissions;
    private ComplaintDAO complaintDao;
    private ComplaintSearchServiceV1 complaintSearchService;

    @Before
    public void setup() {
        ComplaintSearchRequestValidator searchRequestValidator = Mockito.mock(ComplaintSearchRequestValidator.class);
        ComplaintSearchRequestNormalizer searchRequestNormalizer = Mockito.mock(ComplaintSearchRequestNormalizer.class);

        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        Mockito.when(resourcePermissions.getAllAcbsForCurrentUser())
            .thenReturn(Stream.of(CertificationBody.builder().id(1L).build(),
                    CertificationBody.builder().id(2L).build(),
                    CertificationBody.builder().id(3L).build()).collect(Collectors.toList()));

        complaintDao = Mockito.mock(ComplaintDAO.class);
        Mockito.when(complaintDao.getAllComplaints())
            .thenReturn(buildMockComplaintList());

        complaintSearchService = new ComplaintSearchServiceV1(complaintDao, searchRequestNormalizer, searchRequestValidator, resourcePermissions);
    }

    @Test
    public void search_validEmptySearchRequest_findsAllDevelopers() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_pageOutOfRangeSearchRequest_returnsEmptyResponse() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .pageNumber(2)
            .pageSize(100)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_sortByAcbComplaintIdAscending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.ACB_COMPLAINT_ID)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals("aaa", searchResponse.getResults().get(0).getAcbComplaintId());
        assertEquals("c", searchResponse.getResults().get(1).getAcbComplaintId());
        assertEquals("e", searchResponse.getResults().get(2).getAcbComplaintId());
    }

    @Test
    public void search_sortByAcbComplaintIdDescending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.ACB_COMPLAINT_ID)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals("e", searchResponse.getResults().get(0).getAcbComplaintId());
        assertEquals("c", searchResponse.getResults().get(1).getAcbComplaintId());
        assertEquals("aaa", searchResponse.getResults().get(2).getAcbComplaintId());
    }

    @Test
    public void search_sortByAcbAscending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CERTIFICATION_BODY)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals("Drummond", searchResponse.getResults().get(0).getCertificationBody().getName());
        assertEquals("ICSA Labs", searchResponse.getResults().get(1).getCertificationBody().getName());
        assertEquals("ICSA Labs", searchResponse.getResults().get(2).getCertificationBody().getName());
    }

    @Test
    public void search_sortByAcbDescending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CERTIFICATION_BODY)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals("ICSA Labs", searchResponse.getResults().get(0).getCertificationBody().getName());
        assertEquals("ICSA Labs", searchResponse.getResults().get(1).getCertificationBody().getName());
        assertEquals("Drummond", searchResponse.getResults().get(2).getCertificationBody().getName());
    }

    @Test
    public void search_sortByComplainantTypeAscending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.COMPLAINANT_TYPE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals("type1", searchResponse.getResults().get(0).getComplainantType().getName());
        assertEquals("type1", searchResponse.getResults().get(1).getComplainantType().getName());
        assertEquals("type2", searchResponse.getResults().get(2).getComplainantType().getName());
    }

    @Test
    public void search_sortByComplainantTypeDescending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.COMPLAINANT_TYPE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals("type2", searchResponse.getResults().get(0).getComplainantType().getName());
        assertEquals("type1", searchResponse.getResults().get(1).getComplainantType().getName());
        assertEquals("type1", searchResponse.getResults().get(2).getComplainantType().getName());
    }

    @Test
    public void search_sortByCurrentStatusAscending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.CURRENT_STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertNotNull(searchResponse.getResults().get(0).getClosedDate());
        assertNull(searchResponse.getResults().get(1).getClosedDate());
        assertNull(searchResponse.getResults().get(2).getClosedDate());
    }

    @Test
    public void search_sortByCurrentStatusDescending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.CURRENT_STATUS)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertNull(searchResponse.getResults().get(0).getClosedDate());
        assertNull(searchResponse.getResults().get(1).getClosedDate());
        assertNotNull(searchResponse.getResults().get(2).getClosedDate());
    }

    @Test
    public void search_sortByOncComplaintIdAscending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.ONC_COMPLAINT_ID)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals("b", searchResponse.getResults().get(0).getOncComplaintId());
        assertEquals("d", searchResponse.getResults().get(1).getOncComplaintId());
        assertEquals("f", searchResponse.getResults().get(2).getOncComplaintId());
    }

    @Test
    public void search_sortByOncComplaintIdDescending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.ONC_COMPLAINT_ID)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals("f", searchResponse.getResults().get(0).getOncComplaintId());
        assertEquals("d", searchResponse.getResults().get(1).getOncComplaintId());
        assertEquals("b", searchResponse.getResults().get(2).getOncComplaintId());
    }

    @Test
    public void search_sortByReceivedDateAscending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(false)
            .orderBy(OrderByOption.RECEIVED_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals(LocalDate.parse("2022-01-02"), searchResponse.getResults().get(0).getReceivedDate());
        assertEquals(LocalDate.parse("2022-02-02"), searchResponse.getResults().get(1).getReceivedDate());
        assertEquals(LocalDate.parse("2022-03-02"), searchResponse.getResults().get(2).getReceivedDate());
    }

    @Test
    public void search_sortByReceivedDateDescending_ordersResults() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .sortDescending(true)
            .orderBy(OrderByOption.RECEIVED_DATE)
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
        assertEquals(LocalDate.parse("2022-03-02"), searchResponse.getResults().get(0).getReceivedDate());
        assertEquals(LocalDate.parse("2022-02-02"), searchResponse.getResults().get(1).getReceivedDate());
        assertEquals(LocalDate.parse("2022-01-02"), searchResponse.getResults().get(2).getReceivedDate());
    }

    @Test
    public void search_acbIdNotAllowedProvidedAndNoMatches_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .acbIds(Stream.of(5L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_acbIdAllowedProvidedAndHasMatches_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .acbIds(Stream.of(1L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_acbIdsAllowedProvidedAndHasAllMatches_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .acbIds(Stream.of(1L, 2L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_acbIdsAllowedAndNotAllowedProvidedAndHasSomeMatches_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .acbIds(Stream.of(1L, 4L).collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_statusProvidedAndNoMatches_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .currentStatusNames(Stream.of("Pending").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_statusOpenProvidedAndHasMatches_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .currentStatusNames(Stream.of("Open").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_statusClosedProvidedAndHasMatches_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .currentStatusNames(Stream.of("Closed").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_statusOpenAndClosedProvidedAndHasMatches_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .currentStatusNames(Stream.of("Open", "Closed").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_complainantTypeProvidedAndNoMatches_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .complainantTypeNames(Stream.of("junk").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_complainantTypeProvidedAndHasMatches_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .complainantTypeNames(Stream.of("type1").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_complainantTypesProvidedAndHasMatches_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .complainantTypeNames(Stream.of("type1", "type2").collect(Collectors.toSet()))
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvidedMatchingOncComplaintId_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .searchTerm("f")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvidedMatchingAcbComplaintId_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .searchTerm("aaa")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_searchTermProvidedMatchingAssociatedCertifiedProduct_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .searchTerm("12345")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertEquals(3, searchResponse.getResults().get(0).getId());
    }

    @Test
    public void search_searchTermProvidedMatchingPartialAssociatedCertifiedProduct_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .searchTerm("23")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertEquals(3, searchResponse.getResults().get(0).getId());
    }

    @Test
    public void search_searchTermProvidedMatchingAssociatedCriteria_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .searchTerm("170.315 (a)(1)")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertEquals(2, searchResponse.getResults().get(0).getId());
    }

    @Test
    public void search_searchTermProvidedMatchingPartialAssociatedCriteria_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .searchTerm("(a)(1)")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
        assertEquals(2, searchResponse.getResults().get(0).getId());
    }

    @Test
    public void search_searchTermProvidedMatchingPartialAssociatedCriteriaAndAcbComplaintId_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
            .searchTerm("1")
            .pageNumber(0)
            .pageSize(10)
        .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_closedStartDateEqualsComplaintClosedDate_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart("2022-04-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_closedStartDateBeforeComplaintClosedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart("2022-04-01")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_closedStartDateAfterComplaintClosedDate_noMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart("2022-04-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_closedEndDateEqualsComplaintClosedDate_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateEnd("2022-04-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_closedEndDateAfterComplaintClosedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateEnd("2022-04-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_closedEndDateBeforeComplaintClosedDate_noMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateEnd("2022-04-01")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintClosedDateBetweenStartAndEnd_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart("2022-04-01")
                .closedDateEnd("2022-04-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintClosedDateEqualsStartAndBeforeEnd_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart("2022-04-02")
                .closedDateEnd("2022-04-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintClosedDateEqualsEndAndAfterStart_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart("2022-04-01")
                .closedDateEnd("2022-04-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintClosedDateEqualsEndAndStart_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart("2022-04-02")
                .closedDateEnd("2022-04-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintClosedDateOutsideOfEndAndStart_noMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .closedDateStart("2022-05-01")
                .closedDateEnd("2022-05-30")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_receivedStartDateEqualsComplaintReceivedDate_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart("2022-03-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_receivedStartDateBeforeComplaintReceivedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart("2022-03-01")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_receivedStartDateAfterComplaintReceivedDate_noMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart("2022-03-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_receivedEndDateEqualsComplaintReceivedDate_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateEnd("2022-01-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_receivedEndDateAfterComplaintReceivedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateEnd("2022-01-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_receivedEndDateBeforeComplaintReceivedDate_noMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateEnd("2022-01-01")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintReceivedDateBetweenStartAndEnd_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart("2022-03-01")
                .receivedDateEnd("2022-03-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintReceivedDateEqualsStartAndBeforeEnd_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart("2022-03-02")
                .receivedDateEnd("2022-03-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintReceivedDateEqualsEndAndAfterStart_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart("2022-03-01")
                .receivedDateEnd("2022-03-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintReceivedDateEqualsEndAndStart_findsNoMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart("2022-03-02")
                .receivedDateEnd("2022-03-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_complaintReceivedDateOutsideOfEndAndStart_noMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .receivedDateStart("2022-05-01")
                .receivedDateEnd("2022-05-30")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeStartDateEqualsOpenComplaintReceivedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2022-01-02")
                .openDuringRangeEnd("2022-01-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeEndDateEqualsOpenComplaintReceivedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2022-01-01")
                .openDuringRangeEnd("2022-01-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeStartDateBeforeOpenComplaintReceivedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2021-12-01")
                .openDuringRangeEnd("2022-01-31")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeStartDateAfterOpenComplaintReceivedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2022-01-15")
                .openDuringRangeEnd("2022-01-31")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeStartDateBeforeOpenComplaintReceivedDate_noMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2021-12-01")
                .openDuringRangeEnd("2021-12-31")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.getRecordCount());
        assertEquals(0, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeStartDateEqualsClosedComplaintReceivedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2022-03-02")
                .openDuringRangeEnd("2022-03-03")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeEndDateEqualsClosedComplaintReceivedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2022-03-01")
                .openDuringRangeEnd("2022-03-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeStartAndEndDateEqualsClosedComplaintReceivedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2022-03-02")
                .openDuringRangeEnd("2022-03-02")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeAfterClosedComplaintReceivedDateAndBeforeComplaintClosedDate_findsMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2022-03-10")
                .openDuringRangeEnd("2022-03-25")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_openDuringRangeAfterClosedComplaintClosedDate_hasOpenMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2022-04-04")
                .openDuringRangeEnd("2022-04-10")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
        searchResponse.getResults().stream()
            .forEach(sr -> assertNull(sr.getClosedDate()));
    }

    @Test
    public void search_openDuringRangeBeforeClosedComplaintReceivedDate_hasOpenMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .openDuringRangeStart("2022-02-20")
                .openDuringRangeEnd("2022-03-01")
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
        searchResponse.getResults().stream()
            .forEach(sr -> assertNull(sr.getClosedDate()));
    }

    @Test
    public void search_informedOncTrue_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOnc(Stream.of(true).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_informedOncFalse_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOnc(Stream.of(false).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_informedOncTrueOrFalse_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .informedOnc(Stream.of(true, false).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_atlContactedTrue_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContacted(Stream.of(true).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_atlContactedFalse_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContacted(Stream.of(false).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_atlContactedTrueOrFalse_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .oncAtlContacted(Stream.of(true, false).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_complainantContactedTrue_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContacted(Stream.of(true).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_complainantContactedcFalse_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContacted(Stream.of(false).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_complainantContactedTrueOrFalse_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .complainantContacted(Stream.of(true, false).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    @Test
    public void search_developerContactedTrue_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContacted(Stream.of(true).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.getRecordCount());
        assertEquals(1, searchResponse.getResults().size());
    }

    @Test
    public void search_developerContactedFalse_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContacted(Stream.of(false).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(2, searchResponse.getRecordCount());
        assertEquals(2, searchResponse.getResults().size());
    }

    @Test
    public void search_developerContactedTrueOrFalse_hasMatches() throws ValidationException {
        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .developerContacted(Stream.of(false, true).collect(Collectors.toSet()))
                .pageNumber(0)
                .pageSize(10)
            .build();
        ComplaintSearchResponse searchResponse = complaintSearchService.searchComplaints(searchRequest);

        assertNotNull(searchResponse);
        assertEquals(3, searchResponse.getRecordCount());
        assertEquals(3, searchResponse.getResults().size());
    }

    private List<Complaint> buildMockComplaintList() {
        return Stream.of(Complaint.builder()
                    .id(1L)
                    .acbComplaintId("aaa")
                    .oncComplaintId("b")
                    .complainantContacted(false)
                    .developerContacted(false)
                    .flagForOncReview(true)
                    .oncAtlContacted(false)
                    .certificationBody(CertificationBody.builder()
                            .id(1L)
                            .name("Drummond")
                            .build())
                    .complainantType(ComplainantType.builder()
                            .name("type1")
                            .build())
                    .closedDate(null)
                    .receivedDate(LocalDate.parse("2022-01-02"))
                .build(),
                Complaint.builder()
                    .id(2L)
                    .acbComplaintId("c")
                    .oncComplaintId("d")
                    .complainantContacted(true)
                    .developerContacted(false)
                    .flagForOncReview(false)
                    .oncAtlContacted(false)
                    .certificationBody(CertificationBody.builder()
                            .id(2L)
                            .name("ICSA Labs")
                            .build())
                    .complainantType(ComplainantType.builder()
                            .name("type2")
                            .build())
                    .closedDate(null)
                    .receivedDate(LocalDate.parse("2022-02-02"))
                    .criteria(Stream.of(ComplaintCriterionMap.builder()
                            .certificationCriterionId(1L)
                            .complaintId(2L)
                            .certificationCriterion(CertificationCriterion.builder()
                                    .id(1L)
                                    .number("170.315 (a)(1)")
                                    .build())
                            .build())
                        .collect(Collectors.toSet()))
                .build(),
                Complaint.builder()
                    .id(3L)
                    .acbComplaintId("e")
                    .oncComplaintId("f")
                    .complainantContacted(false)
                    .developerContacted(true)
                    .flagForOncReview(false)
                    .oncAtlContacted(true)
                    .certificationBody(CertificationBody.builder()
                            .id(2L)
                            .name("ICSA Labs")
                            .build())
                    .complainantType(ComplainantType.builder()
                            .name("type1")
                            .build())
                    .closedDate(LocalDate.parse("2022-04-02"))
                    .receivedDate(LocalDate.parse("2022-03-02"))
                    .listings(Stream.of(ComplaintListingMap.builder()
                                .chplProductNumber("12345")
                                .complaintId(3L)
                                .developerName("Epic")
                                .listingId(100L)
                                .build())
                            .collect(Collectors.toSet()))
                .build()).collect(Collectors.toList());
    }
}
