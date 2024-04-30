package gov.healthit.chpl.web.controller;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchRequestV2;
import gov.healthit.chpl.developer.search.DeveloperSearchResponse;
import gov.healthit.chpl.developer.search.DeveloperSearchResponseV2;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.developer.search.DeveloperSearchServiceV2;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "search-developers", description = "Allows searching for developers.")
@RestController
@RequestMapping("/developers/search")
@Log4j2
public class SearchDevelopersController {

    private DeveloperSearchService developerSearchService;
    private DeveloperSearchServiceV2 developerSearchServiceV2;

    @Autowired
    public SearchDevelopersController(DeveloperSearchService developerSearchService,
            DeveloperSearchServiceV2 developerSearchServiceV2) {
        this.developerSearchService = developerSearchService;
        this.developerSearchServiceV2 = developerSearchServiceV2;
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search developers on the CHPL",
        description = "This endpoint will always use the oldest, valid version of the "
                + "/developers/search/vX endpoint. The current version being used is v2. For the "
                + "current documentation, see /developers/search/v2.",
        security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperSearchResponseV2 search(
        @Parameter(description = "Developer name or developer code", allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds developers with at least one active certificate belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
            @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
        @Parameter(description = "A comma-separated list of developer statuses to be 'or'ed together "
                + "(ex: \"Active,Under certification ban by ONC\" finds developers in either the Active or Under certification ban by ONC statuses).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "statuses")
            @RequestParam(value = "statuses", required = false, defaultValue = "") String statusesDelimited,
        @Parameter(description = "To return only developers decertified after this date. Required format is " + DeveloperSearchRequestV2.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
            @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
        @Parameter(description = "To return only developers decertified before this date. Required format is " + DeveloperSearchRequestV2.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateEnd")
            @RequestParam(value = "decertificationDateEnd", required = false, defaultValue = "") String decertificationDateEnd,
        @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
        @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @Parameter(description = "What to order by. Options are one of the following: DEVELOPER, "
                + "DECERTIFICATION_DATE, or STATUS. Defaults to DEVELOPER.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "developer") String orderBy,
        @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        return searchV2(searchTerm, certificationBodiesDelimited, statusesDelimited, decertificationDateStart,
                decertificationDateEnd, pageNumber, pageSize, orderBy, sortDescending);
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "This endpoint will be removed on or about 10-31-2024. Please begin to use /search/developers/v3. "
            + "Search the set of developers in the CHPL.",
            description = "If paging parameters are not specified, the first 20 records are returned by default. "
                    + "All parameters are optional. "
                    + "Any parameter that can accept multiple things (i.e. certificationBodies) expects "
                    + "a comma-delimited list of those things (i.e. certificationBodies = Drummond,ICSA Labs). "
                    + "Date parameters are required to be in the format "
                    + DeveloperSearchRequestV2.DATE_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/v2", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @DeprecatedApi(friendlyUrl = "/developers/search/v2", message = "This version of developer search has been deprecated and will be removed. "
            + "Please use developers/search/v3", removalDate = "2024-10-31")
    public @ResponseBody DeveloperSearchResponseV2 searchV2(
        @Parameter(description = "Developer name or developer code", allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds developers with at least one active certificate belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
            @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
        @Parameter(description = "A comma-separated list of developer statuses to be 'or'ed together "
                + "(ex: \"Active,Under certification ban by ONC\" finds developers in either the Active or Under certification ban by ONC statuses).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "statuses")
            @RequestParam(value = "statuses", required = false, defaultValue = "") String statusesDelimited,
        @Parameter(description = "To return only developers decertified on or after this date. Required format is " + DeveloperSearchRequestV2.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
            @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
        @Parameter(description = "To return only developers decertified on or before this date. Required format is " + DeveloperSearchRequestV2.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateEnd")
            @RequestParam(value = "decertificationDateEnd", required = false, defaultValue = "") String decertificationDateEnd,
        @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
        @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @Parameter(description = "What to order by. Options are one of the following: DEVELOPER, "
                + "DECERTIFICATION_DATE, or STATUS. Defaults to DEVELOPER.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "developer") String orderBy,
        @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        DeveloperSearchRequestV2 searchRequest = DeveloperSearchRequestV2.builder()
                .searchTerm(searchTerm.trim())
                .statuses(convertToSetWithDelimeter(statusesDelimited, ","))
                .certificationBodies(convertToSetWithDelimeter(certificationBodiesDelimited, ","))
                .decertificationDateStart(decertificationDateStart)
                .decertificationDateEnd(decertificationDateEnd)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return developerSearchServiceV2.findDevelopers(searchRequest);
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search the set of developers in the CHPL.",
            description = "If paging parameters are not specified, the first 20 records are returned by default. "
                    + "All parameters are optional. "
                    + "Any parameter that can accept multiple things (i.e. acbsForActiveListings) expects "
                    + "a comma-delimited list of those things (i.e. acbsForActiveListings=Drummond,ICSA Labs). "
                    + "Date parameters are required to be in the format "
                    + DeveloperSearchRequest.DATE_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/v3", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperSearchResponse searchV3(
        @Parameter(description = "Developer name or developer code", allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds developers with at least one active certificate belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "acbsForActiveListings")
            @RequestParam(value = "acbsForActiveListings", required = false, defaultValue = "") String acbsForActiveLisitngsDelimited,
        @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds developers with any certificate belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "acbsForAllListings")
            @RequestParam(value = "acbsForAllListings", required = false, defaultValue = "") String acbsForAllLisitngsDelimited,
        @Parameter(description = "A comma-separated list of developer statuses to be 'or'ed together "
                + "(ex: \"Under certification ban by ONC\" finds developers in either the Under certification ban by ONC status).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "statuses")
            @RequestParam(value = "statuses", required = false, defaultValue = "") String statusesDelimited,
        @Parameter(description = "Whether the developer has or has not submitted attestations for the most recent attestation period",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "hasSubmittedAttestationsForMostRecentPastPeriod")
            @RequestParam(value = "hasSubmittedAttestationsForMostRecentPastPeriod", required = false, defaultValue = "") Boolean hasSubmittedAttestationsForMostRecentPastPeriod,
        @Parameter(description = "Whether the developer has or has not published (submitted and been approved) attestations for the most recent attestation period",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "hasPublishedAttestationsForMostRecentPastPeriod")
        @RequestParam(value = "hasPublishedAttestationsForMostRecentPastPeriod", required = false, defaultValue = "") Boolean hasPublishedAttestationsForMostRecentPastPeriod,
        @Parameter(description = "To return only developers decertified on or after this date. Required format is " + DeveloperSearchRequestV2.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
            @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
        @Parameter(description = "To return only developers decertified on or before this date. Required format is " + DeveloperSearchRequestV2.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateEnd")
            @RequestParam(value = "decertificationDateEnd", required = false, defaultValue = "") String decertificationDateEnd,
        @Parameter(description = "A comma-separated list of filters indicating the status of listings for the developer. "
                + "Valid options are HAS_ANY_ACTIVE, HAS_NO_ACTIVE, and HAD_ANY_ACTIVE_DURING_MOST_RECENT_PAST_ATTESTATION_PERIOD.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "activeListingsOptions")
        @RequestParam(value = "activeListingsOptions", required = false, defaultValue = "") String activeListingsOptionsDelimited,
        @Parameter(description = "Either AND or OR. Defaults to OR."
                + "Indicates whether a developer must have met all activeListingsOptions "
                + "specified or may have met any one or more of the activeListingsOptions",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "activeListingsOptionsOperator")
        @RequestParam(value = "activeListingsOptionsOperator", required = false, defaultValue = "OR") String activeListingsOptionsOperator,
        @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
        @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @Parameter(description = "What to order by. Options are one of the following: DEVELOPER_NAME, DEVELOPER_CODE, "
                + "DECERTIFICATION_DATE, or STATUS. Defaults to DEVELOPER_NAME.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "developer_name") String orderBy,
        @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .statuses(convertToSetWithDelimeter(statusesDelimited, ","))
                .acbsForActiveListings(convertToSetWithDelimeter(acbsForActiveLisitngsDelimited, ","))
                .acbsForAllListings(convertToSetWithDelimeter(acbsForAllLisitngsDelimited, ","))
                .hasSubmittedAttestationsForMostRecentPastPeriod(hasSubmittedAttestationsForMostRecentPastPeriod)
                .hasPublishedAttestationsForMostRecentPastPeriod(hasPublishedAttestationsForMostRecentPastPeriod)
                .decertificationDateStart(decertificationDateStart)
                .decertificationDateEnd(decertificationDateEnd)
                .activeListingsOptionsStrings(convertToSetWithDelimeter(activeListingsOptionsDelimited, ","))
                .activeListingsOptionsOperatorString(activeListingsOptionsOperator)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return developerSearchService.findDevelopers(searchRequest);
    }

    private Set<String> convertToSetWithDelimeter(String delimitedString, String delimeter) {
        if (ObjectUtils.isEmpty(delimitedString)) {
            return new LinkedHashSet<String>();
        }
        return Stream.of(delimitedString.split(delimeter))
                .map(value -> StringUtils.normalizeSpace(value))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
