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

import gov.healthit.chpl.developer.search.DeveloperSearchResponse;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.developer.search.DeveloperSearchServiceV1;
import gov.healthit.chpl.developer.search.SearchRequest;
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
    private DeveloperSearchServiceV1 developerSearchServiceV1;

    @Autowired
    public SearchDevelopersController(DeveloperSearchService developerSearchService,
            DeveloperSearchServiceV1 developerSearchServiceV1) {
        this.developerSearchService = developerSearchService;
        this.developerSearchServiceV1 = developerSearchServiceV1;
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @DeprecatedApi(friendlyUrl = "/developers/search", removalDate = "2023-08-30",
        message = "This endpoint is resolving to a deprecated endpoint. As of 2023-08-30 this endpoint will resolve to /developers/search/v2. "
        + "The endpoint /developers/search/v2 interprets date range start and end search parameters as inclusive.")
    @Operation(summary = "Search the CHPL",
        description = "This endpoint will always use the oldest, valid version of the "
                + "/developers/search/vX endpoint. The current version being used is v1. For the "
                + "current documentation, see /developers/search/v1.",
        security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody DeveloperSearchResponse search(
        @Parameter(description = "Developer name or developer code", allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds developers with at least one listing belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
            @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
        @Parameter(description = "A comma-separated list of developer statuses to be 'or'ed together "
                + "(ex: \"Active,Under certification ban by ONC\" finds developers in either the Active or Under certification ban by ONC statuses).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "statuses")
            @RequestParam(value = "statuses", required = false, defaultValue = "") String statusesDelimited,
        @Parameter(description = "To return only developers decertified after this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
            @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
        @Parameter(description = "To return only developers decertified before this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
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

        return searchV1(searchTerm, certificationBodiesDelimited, statusesDelimited, decertificationDateStart,
                decertificationDateEnd, pageNumber, pageSize, orderBy, sortDescending);
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @DeprecatedApi(friendlyUrl = "/developers/search/v1", removalDate = "2023-08-30",
        message = "This endpoint has been deprecated and will be removed. Please use /developers/search/v2.")
    @Deprecated
    @Operation(summary = "Search the set of developers in the CHPL.",
            description = "If paging parameters are not specified, the first 20 records are returned by default. "
                    + "All parameters are optional. "
                    + "Any parameter that can accept multiple things (i.e. certificationBodies) expects "
                    + "a comma-delimited list of those things (i.e. certificationBodies = Drummond,ICSA Labs). "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.DATE_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/v1", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody DeveloperSearchResponse searchV1(
        @Parameter(description = "Developer name or developer code", allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds developers with at least one listing belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
            @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
        @Parameter(description = "A comma-separated list of developer statuses to be 'or'ed together "
                + "(ex: \"Active,Under certification ban by ONC\" finds developers in either the Active or Under certification ban by ONC statuses).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "statuses")
            @RequestParam(value = "statuses", required = false, defaultValue = "") String statusesDelimited,
        @Parameter(description = "To return only developers decertified after this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
            @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
        @Parameter(description = "To return only developers decertified before this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
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

        SearchRequest searchRequest = SearchRequest.builder()
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
        return developerSearchServiceV1.findDevelopers(searchRequest);
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search the set of developers in the CHPL.",
            description = "If paging parameters are not specified, the first 20 records are returned by default. "
                    + "All parameters are optional. "
                    + "Any parameter that can accept multiple things (i.e. certificationBodies) expects "
                    + "a comma-delimited list of those things (i.e. certificationBodies = Drummond,ICSA Labs). "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.DATE_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/v2", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody DeveloperSearchResponse searchV2(
        @Parameter(description = "Developer name or developer code", allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds developers with at least one listing belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
            @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
        @Parameter(description = "A comma-separated list of developer statuses to be 'or'ed together "
                + "(ex: \"Active,Under certification ban by ONC\" finds developers in either the Active or Under certification ban by ONC statuses).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "statuses")
            @RequestParam(value = "statuses", required = false, defaultValue = "") String statusesDelimited,
        @Parameter(description = "To return only developers decertified on or after this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
            @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
        @Parameter(description = "To return only developers decertified on or before this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
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

        SearchRequest searchRequest = SearchRequest.builder()
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
