package gov.healthit.chpl.web.controller;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.complaint.search.ComplaintSearchRequest;
import gov.healthit.chpl.complaint.search.ComplaintSearchResponse;
import gov.healthit.chpl.complaint.search.ComplaintSearchService;
import gov.healthit.chpl.complaint.search.ComplaintSearchServiceV1;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "search-complaints", description = "Allows searching of complaints.")
@RestController
@RequestMapping("/complaints/search")
public class SearchComplaintsController {
    private ComplaintSearchService complaintSearchService;
    private ComplaintSearchServiceV1 complaintSearchServiceV1;

    @Autowired
    public SearchComplaintsController(ComplaintSearchService complaintSearchService,
            ComplaintSearchServiceV1 complaintSearchServiceV1) {
        this.complaintSearchService = complaintSearchService;
        this.complaintSearchServiceV1 = complaintSearchServiceV1;
    }

    @DeprecatedApi(friendlyUrl = "/complaints/search", removalDate = "2023-08-30",
            message = "This endpoint is resolving to a deprecated endpoint. As of 2023-08-30 this endpoint will resolve to /complaints/search/v2. "
            + "The endpoint /complaints/search/v2 interprets date range start and end search parameters as inclusive.")
    @Operation(summary = "Search complaints on the CHPL",
        description = "This endpoint will always use the oldest, valid version of the "
                + "/complaints/search/vX endpoint. The current version being used is v1. For the "
                + "current documentation, see /complaints/search/v1.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ComplaintSearchResponse search(
            @Parameter(description = "Searches all complaints by ONC-ACB Complaint ID, ONC Complaint ID, Associated Certified Product, or Associated Criteria.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm,
            @Parameter(description = "A comma-separated list of 'Informed ONC' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "informedOnc")
            @RequestParam(value = "informedOnc", defaultValue = "", required = false) String informedOncDelimited,
            @Parameter(description = "A comma-separated list of 'ONC-ATL Contacted' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "oncAtlContacted")
            @RequestParam(value = "oncAtlContacted", defaultValue = "", required = false) String oncAtlContactedDelimited,
            @Parameter(description = "A comma-separated list of 'Complainant Contacted' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "complainantContacted")
            @RequestParam(value = "complainantContacted", defaultValue = "", required = false) String complainantContactedDelimited,
            @Parameter(description = "A comma-separated list of 'Developer Contacted' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "developerContacted")
            @RequestParam(value = "developerContacted", defaultValue = "", required = false) String developerContactedDelimited,
            @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds complaints belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
            @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
            @Parameter(description = "A comma-separated list of complaint status names (ex: \"Open,Closed\"). Results may match any of the provided statuses.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatuses")
            @RequestParam(value = "currentStatuses", required = false, defaultValue = "") String currentStatusNamesDelimited,
            @Parameter(description = "A comma-separated list of complainant type names (ex: \"Developer,Patient,Provider\"). Results may match any of the provided types.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "complainantTypes")
            @RequestParam(value = "complainantTypes", required = false, defaultValue = "") String complainantTypeNamesDelimited,
            @Parameter(description = "To return only complaints closed after this date. Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "closedDateStart")
            @RequestParam(value = "closedDateStart", required = false, defaultValue = "") String closedDateStart,
            @Parameter(description = "To return only complaints closed before this date . Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "closedDateEnd")
            @RequestParam(value = "closedDateEnd", required = false, defaultValue = "") String closedDateEnd,
            @Parameter(description = "To return only complaints received after this date. Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "receivedDateStart")
            @RequestParam(value = "receivedDateStart", required = false, defaultValue = "") String receivedDateStart,
            @Parameter(description = "To return only complaints received before this date. Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "receivedDateEnd")
            @RequestParam(value = "receivedDateEnd", required = false, defaultValue = "") String receivedDateEnd,
            @Parameter(description = "To return only complaints open during this date range. A comma-separated \"start date,end date\" is required. "
                    + "Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT + "," + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "openDuringDateRange")
            @RequestParam(value = "openDuringDateRange", required = false, defaultValue = "") String openDuringDateRange,
            @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
            @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 250.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize,
            @Parameter(description = "What to order by. Options are one of the following: "
                + "COMPLAINANT_TYPE, ONC_COMPLAINT_ID, ACB_COMPLAINT_ID, RECEIVED_DATE, CURRENT_STATUS, or CERTIFICATION_BODY."
                + "Defaults to RECEIVED_DATE.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "RECEIVED_DATE") String orderBy,
            @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
                    throws EntityRetrievalException, ValidationException {
        return searchV1(searchTerm, informedOncDelimited, oncAtlContactedDelimited, complainantContactedDelimited,
                developerContactedDelimited, certificationBodiesDelimited, currentStatusNamesDelimited,
                complainantTypeNamesDelimited, closedDateStart, closedDateEnd, receivedDateStart,
                receivedDateEnd, openDuringDateRange, pageNumber, pageSize, orderBy, sortDescending);
    }

    @DeprecatedApi(friendlyUrl = "/complaints/search/v1", removalDate = "2023-08-30",
            message = "This endpoint has been deprecated and will be removed. Please use /complaints/search/v2.")
    @Deprecated
    @Operation(summary = "Search complaints accessible to the logged-in user based on a set of filters.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, and ROLE_ONC_STAFF can get all complaints. "
                    + "ROLE_ACB can get complaints related to ONC-ACBs to which they have permissins.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/v1", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ComplaintSearchResponse searchV1(
            @Parameter(description = "Searches all complaints by ONC-ACB Complaint ID, ONC Complaint ID, Associated Certified Product, or Associated Criteria.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm,
            @Parameter(description = "A comma-separated list of 'Informed ONC' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "informedOnc")
            @RequestParam(value = "informedOnc", defaultValue = "", required = false) String informedOncDelimited,
            @Parameter(description = "A comma-separated list of 'ONC-ATL Contacted' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "oncAtlContacted")
            @RequestParam(value = "oncAtlContacted", defaultValue = "", required = false) String oncAtlContactedDelimited,
            @Parameter(description = "A comma-separated list of 'Complainant Contacted' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "complainantContacted")
            @RequestParam(value = "complainantContacted", defaultValue = "", required = false) String complainantContactedDelimited,
            @Parameter(description = "A comma-separated list of 'Developer Contacted' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "developerContacted")
            @RequestParam(value = "developerContacted", defaultValue = "", required = false) String developerContactedDelimited,
            @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds complaints belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
            @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
            @Parameter(description = "A comma-separated list of complaint status names (ex: \"Open,Closed\"). Results may match any of the provided statuses.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatuses")
            @RequestParam(value = "currentStatuses", required = false, defaultValue = "") String currentStatusNamesDelimited,
            @Parameter(description = "A comma-separated list of complainant type names (ex: \"Developer,Patient,Provider\"). Results may match any of the provided types.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "complainantTypes")
            @RequestParam(value = "complainantTypes", required = false, defaultValue = "") String complainantTypeNamesDelimited,
            @Parameter(description = "To return only complaints closed after this date. Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "closedDateStart")
            @RequestParam(value = "closedDateStart", required = false, defaultValue = "") String closedDateStart,
            @Parameter(description = "To return only complaints closed before this date . Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "closedDateEnd")
            @RequestParam(value = "closedDateEnd", required = false, defaultValue = "") String closedDateEnd,
            @Parameter(description = "To return only complaints received after this date. Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "receivedDateStart")
            @RequestParam(value = "receivedDateStart", required = false, defaultValue = "") String receivedDateStart,
            @Parameter(description = "To return only complaints received before this date. Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "receivedDateEnd")
            @RequestParam(value = "receivedDateEnd", required = false, defaultValue = "") String receivedDateEnd,
            @Parameter(description = "To return only complaints open during this date range. A comma-separated \"start date,end date\" is required. "
                    + "Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT + "," + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "openDuringDateRange")
            @RequestParam(value = "openDuringDateRange", required = false, defaultValue = "") String openDuringDateRange,
            @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
            @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 250.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize,
            @Parameter(description = "What to order by. Options are one of the following: "
                + "COMPLAINANT_TYPE, ONC_COMPLAINT_ID, ACB_COMPLAINT_ID, RECEIVED_DATE, CURRENT_STATUS, or CERTIFICATION_BODY."
                + "Defaults to RECEIVED_DATE.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "RECEIVED_DATE") String orderBy,
            @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
                    throws EntityRetrievalException, ValidationException {
        String[] openDuringDateRangeArr = openDuringDateRange.split(",");
        String openDuringRangeStart = openDuringDateRangeArr != null && openDuringDateRangeArr.length > 0 ? openDuringDateRangeArr[0] : "";
        String openDuringRangeEnd = openDuringDateRangeArr != null && openDuringDateRangeArr.length > 1 ? openDuringDateRangeArr[1] : "";

        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .informedOncStrings(convertToSetWithDelimeter(informedOncDelimited, ","))
                .oncAtlContactedStrings(convertToSetWithDelimeter(oncAtlContactedDelimited, ","))
                .complainantContactedStrings(convertToSetWithDelimeter(complainantContactedDelimited, ","))
                .developerContactedStrings(convertToSetWithDelimeter(developerContactedDelimited, ","))
                .certificationBodyNames(convertToSetWithDelimeter(certificationBodiesDelimited, ","))
                .currentStatusNames(convertToSetWithDelimeter(currentStatusNamesDelimited, ","))
                .complainantTypeNames(convertToSetWithDelimeter(complainantTypeNamesDelimited, ","))
                .closedDateStart(closedDateStart)
                .closedDateEnd(closedDateEnd)
                .receivedDateStart(receivedDateStart)
                .receivedDateEnd(receivedDateEnd)
                .openDuringRangeStart(openDuringRangeStart)
                .openDuringRangeEnd(openDuringRangeEnd)
                .pageNumberString(pageNumber)
                .pageSizeString(pageSize)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return complaintSearchServiceV1.searchComplaints(searchRequest);
    }

    @Operation(summary = "Search complaints accessible to the logged-in user based on a set of filters.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, and ROLE_ONC_STAFF can get all complaints. "
                    + "ROLE_ACB can get complaints related to ONC-ACBs to which they have permissins.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/v2", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ComplaintSearchResponse searchV2(
            @Parameter(description = "Searches all complaints by ONC-ACB Complaint ID, ONC Complaint ID, Associated Certified Product, or Associated Criteria.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm,
            @Parameter(description = "A comma-separated list of 'Informed ONC' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "informedOnc")
            @RequestParam(value = "informedOnc", defaultValue = "", required = false) String informedOncDelimited,
            @Parameter(description = "A comma-separated list of 'ONC-ATL Contacted' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "oncAtlContacted")
            @RequestParam(value = "oncAtlContacted", defaultValue = "", required = false) String oncAtlContactedDelimited,
            @Parameter(description = "A comma-separated list of 'Complainant Contacted' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "complainantContacted")
            @RequestParam(value = "complainantContacted", defaultValue = "", required = false) String complainantContactedDelimited,
            @Parameter(description = "A comma-separated list of 'Developer Contacted' values to be 'or'ed together. Valid values include true or false.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "developerContacted")
            @RequestParam(value = "developerContacted", defaultValue = "", required = false) String developerContactedDelimited,
            @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds complaints belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
            @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
            @Parameter(description = "A comma-separated list of complaint status names (ex: \"Open,Closed\"). Results may match any of the provided statuses.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatuses")
            @RequestParam(value = "currentStatuses", required = false, defaultValue = "") String currentStatusNamesDelimited,
            @Parameter(description = "A comma-separated list of complainant type names (ex: \"Developer,Patient,Provider\"). Results may match any of the provided types.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "complainantTypes")
            @RequestParam(value = "complainantTypes", required = false, defaultValue = "") String complainantTypeNamesDelimited,
            @Parameter(description = "A comma-separated list of listing IDs 'or'ed together "
                    + "(ex: \"1,2\" finds complaints associated with either listing 1 or listing 2).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "listingIds")
            @RequestParam(value = "listingIds", required = false, defaultValue = "") String listingIdsDelimited,
            @Parameter(description = "A comma-separated list of surveillance IDs 'or'ed together "
                    + "(ex: \"1,2\" finds complaints associated with either surveillance 1 or surveillance 2).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "surveillanceIds")
            @RequestParam(value = "surveillanceIds", required = false, defaultValue = "") String surveillanceIdsDelimited,
            @Parameter(description = "A comma-separated list of criteria IDs 'or'ed together "
                    + "(ex: \"1,2\" finds complaints associated with either criterion 1 or criterion 2).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "criteriaIds")
            @RequestParam(value = "criteriaIds", required = false, defaultValue = "") String criteriaIdsDelimited,
            @Parameter(description = "To return only complaints closed on or after this date. Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "closedDateStart")
            @RequestParam(value = "closedDateStart", required = false, defaultValue = "") String closedDateStart,
            @Parameter(description = "To return only complaints closed on or before this date . Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "closedDateEnd")
            @RequestParam(value = "closedDateEnd", required = false, defaultValue = "") String closedDateEnd,
            @Parameter(description = "To return only complaints received on or after this date. Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "receivedDateStart")
            @RequestParam(value = "receivedDateStart", required = false, defaultValue = "") String receivedDateStart,
            @Parameter(description = "To return only complaints received on or before this date. Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "receivedDateEnd")
            @RequestParam(value = "receivedDateEnd", required = false, defaultValue = "") String receivedDateEnd,
            @Parameter(description = "To return only complaints open during this date range, inclusive. A comma-separated \"start date,end date\" is required. "
                    + "Required format is " + ComplaintSearchRequest.DATE_SEARCH_FORMAT + "," + ComplaintSearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "openDuringDateRange")
            @RequestParam(value = "openDuringDateRange", required = false, defaultValue = "") String openDuringDateRange,
            @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
            @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 250.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize,
            @Parameter(description = "What to order by. Options are one of the following: "
                + "COMPLAINANT_TYPE, ONC_COMPLAINT_ID, ACB_COMPLAINT_ID, RECEIVED_DATE, CURRENT_STATUS, or CERTIFICATION_BODY."
                + "Defaults to RECEIVED_DATE.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "RECEIVED_DATE") String orderBy,
            @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
                    throws EntityRetrievalException, ValidationException {
        String[] openDuringDateRangeArr = openDuringDateRange.split(",");
        String openDuringRangeStart = openDuringDateRangeArr != null && openDuringDateRangeArr.length > 0 ? openDuringDateRangeArr[0] : "";
        String openDuringRangeEnd = openDuringDateRangeArr != null && openDuringDateRangeArr.length > 1 ? openDuringDateRangeArr[1] : "";

        ComplaintSearchRequest searchRequest = ComplaintSearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .informedOncStrings(convertToSetWithDelimeter(informedOncDelimited, ","))
                .oncAtlContactedStrings(convertToSetWithDelimeter(oncAtlContactedDelimited, ","))
                .complainantContactedStrings(convertToSetWithDelimeter(complainantContactedDelimited, ","))
                .developerContactedStrings(convertToSetWithDelimeter(developerContactedDelimited, ","))
                .certificationBodyNames(convertToSetWithDelimeter(certificationBodiesDelimited, ","))
                .currentStatusNames(convertToSetWithDelimeter(currentStatusNamesDelimited, ","))
                .complainantTypeNames(convertToSetWithDelimeter(complainantTypeNamesDelimited, ","))
                .listingIdStrings(convertToSetWithDelimeter(listingIdsDelimited, ","))
                .surveillanceIdStrings(convertToSetWithDelimeter(surveillanceIdsDelimited, ","))
                .certificationCriteriaIdStrings(convertToSetWithDelimeter(criteriaIdsDelimited, ","))
                .closedDateStart(closedDateStart)
                .closedDateEnd(closedDateEnd)
                .receivedDateStart(receivedDateStart)
                .receivedDateEnd(receivedDateEnd)
                .openDuringRangeStart(openDuringRangeStart)
                .openDuringRangeEnd(openDuringRangeEnd)
                .pageNumberString(pageNumber)
                .pageSizeString(pageSize)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return complaintSearchService.searchComplaints(searchRequest);
    }

    private Set<String> convertToSetWithDelimeter(String delimitedString, String delimeter) {
        if (ObjectUtils.isEmpty(delimitedString)) {
            return new LinkedHashSet<String>();
        }
        return Stream.of(delimitedString.split(delimeter))
                .map(value -> value.trim())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
