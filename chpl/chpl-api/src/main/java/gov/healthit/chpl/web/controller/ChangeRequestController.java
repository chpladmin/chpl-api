package gov.healthit.chpl.web.controller;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchResponse;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedResponseFields;
import gov.healthit.chpl.web.controller.results.ChangeRequestResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "change-requests", description = "Management of change requests.")
@RestController
@RequestMapping("/change-requests")
public class ChangeRequestController {

    private ChangeRequestManager changeRequestManager;
    private ChangeRequestSearchManager changeRequestSearchManager;

    @Autowired
    public ChangeRequestController(ChangeRequestManager changeRequestManager,
            ChangeRequestSearchManager changeRequestSearchManager) {
        this.changeRequestManager = changeRequestManager;
        this.changeRequestSearchManager = changeRequestSearchManager;
    }

    @Operation(summary = "Get details about a specific change request.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests.  ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{changeRequestId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @DeprecatedResponseFields(responseClass = ChangeRequest.class)
    public @ResponseBody ChangeRequest getChangeRequest(@PathVariable final Long changeRequestId) throws EntityRetrievalException {
        return changeRequestManager.getChangeRequest(changeRequestId);
    }

    @Operation(summary = "Search change requests accessible to the logged-in user based on a set of filters.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests. ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer. ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChangeRequestSearchResponse searchChangeRequests(
            @Parameter(description = "Searches all change requests by developer name.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
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
                .typeNames(convertToSetWithDelimeter(changeRequestTypeNamesDelimited, ","))
                .currentStatusChangeDateTimeStart(currentStatusChangeDateTimeStart.trim())
                .currentStatusChangeDateTimeEnd(currentStatusChangeDateTimeEnd.trim())
                .submittedDateTimeStart(submittedDateTimeStart)
                .submittedDateTimeEnd(submittedDateTimeEnd)
                .pageNumberString(pageNumber)
                .pageSizeString(pageSize)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return changeRequestSearchManager.searchChangeRequests(searchRequest);
    }

    @Operation(summary = "Get details about all change requests.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests.  ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @Deprecated
    public @ResponseBody List<ChangeRequest> getAllChangeRequests() throws EntityRetrievalException {
        return changeRequestManager.getAllChangeRequestsForUser();
    }

    @Operation(summary = "Create a new change request.",
            description = "Security Restrictions: ROLE_DEVELOPER can create change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    @DeprecatedResponseFields(responseClass = ChangeRequestResults.class)
    public ChangeRequestResults createChangeRequest(@RequestBody final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException {
        List<ChangeRequest> createdCrs = List.of(changeRequestManager.createChangeRequest(cr));
        ChangeRequestResults results = new ChangeRequestResults();
        results.getResults().addAll(createdCrs);
        return results;
    }

    @Operation(summary = "Update an existing request status or request details.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can update all chnage requests.  ROLE_ACB can update change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can update "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    @DeprecatedResponseFields(responseClass = ChangeRequest.class)
    public ChangeRequest updateChangeRequest(@RequestBody final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, EmailNotSentException {
        return changeRequestManager.updateChangeRequest(cr);
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
