package gov.healthit.chpl.web.controller;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.complaint.ComplaintManager;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.complaint.search.ComplaintSearchRequest;
import gov.healthit.chpl.complaint.search.ComplaintSearchResponse;
import gov.healthit.chpl.complaint.search.ComplaintSearchService;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "complaints", description = "Allows management of complaints.")
@RestController
@RequestMapping("/complaints")
public class ComplaintController {
    private ComplaintManager complaintManager;
    private ComplaintSearchService complaintSearchService;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public ComplaintController(ComplaintManager complaintManager,  ComplaintSearchService complaintSearchService,
            ErrorMessageUtil errorMessageUtil) {
        this.complaintManager = complaintManager;
        this.complaintSearchService = complaintSearchService;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Operation(summary = "Search complaints accessible to the logged-in user based on a set of filters.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, and ROLE_ONC_STAFF can get all complaints. "
                    + "ROLE_ACB can get complaints related to ONC-ACBs to which they have permissins.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
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
        return complaintSearchService.searchComplaints(searchRequest);
    }

    @Operation(summary = "Save complaint for use in Surveillance Quarterly Report.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody Complaint create(@RequestBody Complaint complaint) throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException error = new ValidationException();
        // Make sure there is an ACB
        if (complaint.getCertificationBody() == null || complaint.getCertificationBody().getId() == null) {
            error.getErrorMessages().add(errorMessageUtil.getMessage("complaints.create.acbRequired"));
            throw error;
        }

        return complaintManager.create(complaint);
    }

    @Operation(summary = "Update complaint for use in Surveillance Quarterly Report.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody Complaint update(@RequestBody Complaint complaint)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException error = new ValidationException();
        if (complaint.getCertificationBody() == null || complaint.getCertificationBody().getId() == null) {
            error.getErrorMessages().add(errorMessageUtil.getMessage("complaints.update.acbRequired"));
            throw error;
        }
        return complaintManager.update(complaint);
    }

    @Operation(summary = "Delete complaint for use in Surveillance Quarterly Report.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("complaintId") Long complaintId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        complaintManager.delete(complaintId);
    }

    @Operation(summary = "Generate the Complaints Report and email the results to the logged-in user.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, and ROLE_ONC_STAFF have access.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/report-request", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ChplOneTimeTrigger triggerComplaintsReport() throws SchedulerException, ValidationException {
        ChplOneTimeTrigger jobTrigger = complaintManager.triggerComplaintsReport();
        return jobTrigger;
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
