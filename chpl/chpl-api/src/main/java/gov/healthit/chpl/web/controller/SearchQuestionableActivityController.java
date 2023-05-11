package gov.healthit.chpl.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.questionableactivity.search.QuestionableActivitySearchResponse;
import gov.healthit.chpl.questionableactivity.search.QuestionableActivitySearchService;
import gov.healthit.chpl.questionableactivity.search.SearchRequest;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "search-questionable-activity", description = "Allows searching for questionable activity.")
@RestController
@RequestMapping("/questionable-activity/search")
@Log4j2
public class SearchQuestionableActivityController {

    private QuestionableActivitySearchService questionableActivitySearchService;

    @Autowired
    public SearchQuestionableActivityController(QuestionableActivitySearchService questionableActivitySearchService) {
        this.questionableActivitySearchService = questionableActivitySearchService;
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search Questionable Activity in the CHPL. This is only available to ROLE_ADMIN and ROLE_ONC users.",
            description = "If paging parameters are not specified, the first 20 records are returned by default. "
                    + "All parameters are optional. "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.DATE_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody QuestionableActivitySearchResponse search(
        @Parameter(description = "Developer name, product name, version name, or CHPL Product Number",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "To return only questionable activities that occurred on or after this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "activityDateStart")
            @RequestParam(value = "activityDateStart", required = false, defaultValue = "") String activityDateStart,
        @Parameter(description = "To return only questionable activities that occurred on or before this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "activityDateEnd")
            @RequestParam(value = "activityDateEnd", required = false, defaultValue = "") String activityDateEnd,
        @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
        @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @Parameter(description = "What to order by. Options are one of the following: ACTIVITY_DATE. "
                + "Defaults to ACTIVITY_DATE.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "activity_date") String orderBy,
        @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        SearchRequest searchRequest = SearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .activityDateStart(activityDateStart)
                .activityDateEnd(activityDateEnd)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return questionableActivitySearchService.searchQuestionableActivities(searchRequest);
    }
}
