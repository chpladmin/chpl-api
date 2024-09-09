package gov.healthit.chpl.web.controller;

import java.util.LinkedHashSet;
import java.util.List;
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

import gov.healthit.chpl.activity.search.ActivitySearchResponse;
import gov.healthit.chpl.activity.search.ActivitySearchService;
import gov.healthit.chpl.activity.search.SearchRequest;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "search-activity", description = "Allows ADMIN or ONC users to search all activity.")
@RestController
@RequestMapping("/activity")
@Log4j2
public class SearchActivityController {
    private ActivitySearchService activitySearchService;

    @Autowired
    public SearchActivityController(ActivitySearchService activitySearchService) {
        this.activitySearchService = activitySearchService;
    }

    @Operation(summary = "Get the list of all things that may have activity recorded about them. Use in the 'concepts' parameter of the search endpoint.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
            })
    @RequestMapping(value = "/concepts", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ActivityConcept> getAllActivityConcepts() {
        return Stream.of(ActivityConcept.values()).toList();
    }

    @Operation(summary = "Search across all recorded activity in the CHPL. This is only available to ROLE_ADMIN and ROLE_ONC users.",
            description = "If paging parameters are not specified, the first 20 records are returned by default."
                    + "All parameters are optional. "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.TIMESTAMP_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ActivitySearchResponse search(
        @Parameter(description = "One or more of the available activity concepts separated by ','. Ex: CERTIFIED_PRODUCT,DEVELOPER",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "concepts")
            @RequestParam(value = "concepts", required = false, defaultValue = "") String conceptsDelimited,
        @Parameter(description = "To return only activities that occurred on or after this date. Required format is " + SearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "activityDateStart")
            @RequestParam(value = "activityDateStart", required = false, defaultValue = "") String activityDateStart,
        @Parameter(description = "To return only activities that occurred on or before this date. Required format is " + SearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "activityDateEnd")
            @RequestParam(value = "activityDateEnd", required = false, defaultValue = "") String activityDateEnd,
        @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
        @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @Parameter(description = "What to order by. Options are one of the following: ACTIVITY_DATE. Defaults to ACTIVITY_DATE.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "activity_date") String orderBy,
        @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        SearchRequest searchRequest = SearchRequest.builder()
                .concepts(convertToSetWithDelimeter(conceptsDelimited, ","))
                .activityDateStart(activityDateStart)
                .activityDateEnd(activityDateEnd)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return activitySearchService.searchActivities(searchRequest);
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
