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

import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResponse;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchService;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchServiceV1;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "search-change-requests", description = "Allows searching of change requests.")
@RestController
@RequestMapping("/change-requests/search")
public class SearchChangeRequestsController {

    private ChangeRequestSearchService changeRequestSearchService;
    private ChangeRequestSearchServiceV1 changeRequestSearchServiceV1;

    @Autowired
    public SearchChangeRequestsController(ChangeRequestSearchService changeRequestSearchService,
            ChangeRequestSearchServiceV1 changeRequestSearchServiceV1) {
        this.changeRequestSearchService = changeRequestSearchService;
        this.changeRequestSearchServiceV1 = changeRequestSearchServiceV1;
    }

    @DeprecatedApi(friendlyUrl = "/change-requests/search", removalDate = "2023-08-30",
            message = "This endpoint is resolving to a deprecated endpoint. As of 2023-08-30 this endpoint will resolve to /change-requests/search/v2. "
            + "The endpoint /change-requests/search/v2 interprets date range start and end search parameters as inclusive.")
    @Operation(summary = "Search change requests on the CHPL",
            description = "This endpoint will always use the oldest, valid version of the "
                    + "/change-requests/search/vX endpoint. The current version being used is v1. For the "
                    + "current documentation, see /change-requests/search/v1.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChangeRequestSearchResponse search(
            @Parameter(description = "Searches all change requests by developer name.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm,
            @Parameter(description = "To return only change requests associated with the given developer.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "developerId")
            @RequestParam(value = "developerId", required = false) String developerId,
            @Parameter(description = "A comma-separated list of change request status names (ex: \"Accepted,Rejected\"). Results may match any of the provided statuses.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatusNames")
            @RequestParam(value = "currentStatusNames", required = false, defaultValue = "") String currentStatusNamesDelimited,
            @Parameter(description = "A comma-separated list of change request type names (ex: \"Developer Attestaion Change Request,Developer Demographics Change Request\"). Results may match any of the provided types.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "changeRequestTypeNames")
            @RequestParam(value = "changeRequestTypeNames", required = false, defaultValue = "") String changeRequestTypeNamesDelimited,
            @Parameter(description = "To return only change requests last modified after this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatusChangeDateTimeStart")
            @RequestParam(value = "currentStatusChangeDateTimeStart", required = false, defaultValue = "") String currentStatusChangeDateTimeStart,
            @Parameter(description = "To return only change requests last modified before this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatusChangeDateTimeEnd")
            @RequestParam(value = "currentStatusChangeDateTimeEnd", required = false, defaultValue = "") String currentStatusChangeDateTimeEnd,
            @Parameter(description = "To return only change requests submitted after this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "submittedDateTimeStart")
            @RequestParam(value = "submittedDateTimeStart", required = false, defaultValue = "") String submittedDateTimeStart,
            @Parameter(description = "To return only change requests submitted before this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "submittedDateTimeEnd")
            @RequestParam(value = "submittedDateTimeEnd", required = false, defaultValue = "") String submittedDateTimeEnd,
            @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
            @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 250.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize,
            @Parameter(description = "What to order by. Options are one of the following: "
                + "DEVELOPER, CHANGE_REQUEST_TYPE, CHANGE_REQUEST_STATUS, SUBMITTED_DATE_TIME, CURRENT_STATUS_CHANGE_DATE_TIME, or CERTIFICATION_BODIES."
                + "Defaults to CURRENT_STATUS_CHANGE_DATE_TIME.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "CURRENT_STATUS_CHANGE_DATE_TIME") String orderBy,
            @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
                    throws EntityRetrievalException, ValidationException {
        return searchV1(searchTerm, developerId, currentStatusNamesDelimited, changeRequestTypeNamesDelimited,
                currentStatusChangeDateTimeStart, currentStatusChangeDateTimeEnd, submittedDateTimeStart,
                submittedDateTimeEnd, pageNumber, pageSize, orderBy, sortDescending);
    }

    @DeprecatedApi(friendlyUrl = "/change-requests/search/v1", removalDate = "2023-08-30",
            message = "This endpoint has been deprecated and will be removed. Please use /change-requests/search/v2.")
    @Deprecated
    @Operation(summary = "Search change requests accessible to the logged-in user based on a set of filters.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests. ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer. ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/v1", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChangeRequestSearchResponse searchV1(
            @Parameter(description = "Searches all change requests by developer name.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm,
            @Parameter(description = "To return only change requests associated with the given developer.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "developerId")
            @RequestParam(value = "developerId", required = false) String developerId,
            @Parameter(description = "A comma-separated list of change request status names (ex: \"Accepted,Rejected\"). Results may match any of the provided statuses.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatusNames")
            @RequestParam(value = "currentStatusNames", required = false, defaultValue = "") String currentStatusNamesDelimited,
            @Parameter(description = "A comma-separated list of change request type names (ex: \"Developer Attestaion Change Request,Developer Demographics Change Request\"). Results may match any of the provided types.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "changeRequestTypeNames")
            @RequestParam(value = "changeRequestTypeNames", required = false, defaultValue = "") String changeRequestTypeNamesDelimited,
            @Parameter(description = "To return only change requests last modified after this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatusChangeDateTimeStart")
            @RequestParam(value = "currentStatusChangeDateTimeStart", required = false, defaultValue = "") String currentStatusChangeDateTimeStart,
            @Parameter(description = "To return only change requests last modified before this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatusChangeDateTimeEnd")
            @RequestParam(value = "currentStatusChangeDateTimeEnd", required = false, defaultValue = "") String currentStatusChangeDateTimeEnd,
            @Parameter(description = "To return only change requests submitted after this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "submittedDateTimeStart")
            @RequestParam(value = "submittedDateTimeStart", required = false, defaultValue = "") String submittedDateTimeStart,
            @Parameter(description = "To return only change requests submitted before this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "submittedDateTimeEnd")
            @RequestParam(value = "submittedDateTimeEnd", required = false, defaultValue = "") String submittedDateTimeEnd,
            @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
            @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 250.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize,
            @Parameter(description = "What to order by. Options are one of the following: "
                + "DEVELOPER, CHANGE_REQUEST_TYPE, CHANGE_REQUEST_STATUS, SUBMITTED_DATE_TIME, CURRENT_STATUS_CHANGE_DATE_TIME, or CERTIFICATION_BODIES."
                + "Defaults to CURRENT_STATUS_CHANGE_DATE_TIME.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "CURRENT_STATUS_CHANGE_DATE_TIME") String orderBy,
            @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
                    throws EntityRetrievalException, ValidationException {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .developerIdString(developerId)
                .currentStatusNames(convertToSetWithDelimeter(currentStatusNamesDelimited, ","))
                .changeRequestTypeNames(convertToSetWithDelimeter(changeRequestTypeNamesDelimited, ","))
                .currentStatusChangeDateTimeStart(currentStatusChangeDateTimeStart.trim())
                .currentStatusChangeDateTimeEnd(currentStatusChangeDateTimeEnd.trim())
                .submittedDateTimeStart(submittedDateTimeStart)
                .submittedDateTimeEnd(submittedDateTimeEnd)
                .pageNumberString(pageNumber)
                .pageSizeString(pageSize)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return changeRequestSearchServiceV1.searchChangeRequests(searchRequest);
    }

    @Operation(summary = "Search change requests accessible to the logged-in user based on a set of filters.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests. ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer. ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/v2", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChangeRequestSearchResponse searchV2(
            @Parameter(description = "Searches all change requests by developer name.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm,
            @Parameter(description = "To return only change requests associated with the given developer.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "developerId")
            @RequestParam(value = "developerId", required = false) String developerId,
            @Parameter(description = "A comma-separated list of change request status names (ex: \"Accepted,Rejected\"). Results may match any of the provided statuses.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatusNames")
            @RequestParam(value = "currentStatusNames", required = false, defaultValue = "") String currentStatusNamesDelimited,
            @Parameter(description = "A comma-separated list of change request type names (ex: \"Developer Attestaion Change Request,Developer Demographics Change Request\"). Results may match any of the provided types.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "changeRequestTypeNames")
            @RequestParam(value = "changeRequestTypeNames", required = false, defaultValue = "") String changeRequestTypeNamesDelimited,
            @Parameter(description = "To return only change requests last modified on or after this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatusChangeDateTimeStart")
            @RequestParam(value = "currentStatusChangeDateTimeStart", required = false, defaultValue = "") String currentStatusChangeDateTimeStart,
            @Parameter(description = "To return only change requests last modified on or before this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "currentStatusChangeDateTimeEnd")
            @RequestParam(value = "currentStatusChangeDateTimeEnd", required = false, defaultValue = "") String currentStatusChangeDateTimeEnd,
            @Parameter(description = "To return only change requests submitted on or after this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "submittedDateTimeStart")
            @RequestParam(value = "submittedDateTimeStart", required = false, defaultValue = "") String submittedDateTimeStart,
            @Parameter(description = "To return only change requests submitted on or before this date and time. Required format is " + ChangeRequestSearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "submittedDateTimeEnd")
            @RequestParam(value = "submittedDateTimeEnd", required = false, defaultValue = "") String submittedDateTimeEnd,
            @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") String pageNumber,
            @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 250.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSize,
            @Parameter(description = "What to order by. Options are one of the following: "
                + "DEVELOPER, CHANGE_REQUEST_TYPE, CHANGE_REQUEST_STATUS, SUBMITTED_DATE_TIME, CURRENT_STATUS_CHANGE_DATE_TIME, or CERTIFICATION_BODIES."
                + "Defaults to CURRENT_STATUS_CHANGE_DATE_TIME.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "CURRENT_STATUS_CHANGE_DATE_TIME") String orderBy,
            @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
                    throws EntityRetrievalException, ValidationException {
        ChangeRequestSearchRequest searchRequest = ChangeRequestSearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .developerIdString(developerId)
                .currentStatusNames(convertToSetWithDelimeter(currentStatusNamesDelimited, ","))
                .changeRequestTypeNames(convertToSetWithDelimeter(changeRequestTypeNamesDelimited, ","))
                .currentStatusChangeDateTimeStart(currentStatusChangeDateTimeStart.trim())
                .currentStatusChangeDateTimeEnd(currentStatusChangeDateTimeEnd.trim())
                .submittedDateTimeStart(submittedDateTimeStart)
                .submittedDateTimeEnd(submittedDateTimeEnd)
                .pageNumberString(pageNumber)
                .pageSizeString(pageSize)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return changeRequestSearchService.searchChangeRequests(searchRequest);
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
