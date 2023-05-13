package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.questionableactivity.QuestionableActivityManager;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivity;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
import gov.healthit.chpl.questionableactivity.search.QuestionableActivitySearchResponse;
import gov.healthit.chpl.questionableactivity.search.QuestionableActivitySearchService;
import gov.healthit.chpl.questionableactivity.search.SearchRequest;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "search-questionable-activity", description = "Allows searching for questionable activity.")
@RestController
@RequestMapping("/questionable-activity")
@Log4j2
public class SearchQuestionableActivityController {

    private QuestionableActivityManager questionableActivityManager;
    private QuestionableActivitySearchService questionableActivitySearchService;
    private FileUtils fileUtils;

    @Autowired
    public SearchQuestionableActivityController(QuestionableActivityManager questionableActivityManager,
            QuestionableActivitySearchService questionableActivitySearchService,
            FileUtils fileUtils) {
        this.questionableActivityManager = questionableActivityManager;
        this.questionableActivitySearchService = questionableActivitySearchService;
        this.fileUtils = fileUtils;
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Get the list of all types of actions that may trigger questionable activity to be recorded. "
            + "This is only available to ROLE_ADMIN and ROLE_ONC users.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/trigger-types", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public List<QuestionableActivityTrigger> getAllTriggerTypes() {
        return questionableActivityManager.getTriggerTypes();
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search Questionable Activity in the CHPL. This is only available to ROLE_ADMIN and ROLE_ONC users.",
            description = "If paging parameters are not specified, the first 20 records are returned by default."
                    + "All parameters are optional. "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.DATE_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody QuestionableActivitySearchResponse search(
        @Parameter(description = "Developer name, product name, or CHPL Product Number",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of questionable activity trigger IDs (ex: \"1,2,3\"). Results may match any of the provided triggers.",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "triggerIds")
            @RequestParam(value = "triggerIds", required = false, defaultValue = "") String triggerIdsDelimited,
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
                .triggerIdStrings(convertToSetWithDelimeter(triggerIdsDelimited, ","))
                .activityDateStart(activityDateStart)
                .activityDateEnd(activityDateEnd)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return questionableActivitySearchService.searchQuestionableActivities(searchRequest);
    }

    @Operation(summary = "Download Questionable Activity. This is only available to ROLE_ADMIN and ROLE_ONC users.",
            description = "All parameters are optional. "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.DATE_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = {
            "text/csv; charset=utf-8"
    })
    public void download(@Parameter(description = "Developer name, product name, or CHPL Product Number",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of questionable activity trigger IDs (ex: \"1,2,3\"). Results may match any of the provided triggers.",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "triggerIds")
            @RequestParam(value = "triggerIds", required = false, defaultValue = "") String triggerIdsDelimited,
        @Parameter(description = "To return only questionable activities that occurred on or after this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "activityDateStart")
            @RequestParam(value = "activityDateStart", required = false, defaultValue = "") String activityDateStart,
        @Parameter(description = "To return only questionable activities that occurred on or before this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "activityDateEnd")
            @RequestParam(value = "activityDateEnd", required = false, defaultValue = "") String activityDateEnd,
            HttpServletRequest request, HttpServletResponse response)
        throws InvalidArgumentsException, ValidationException, IOException {

        SearchRequest searchRequest = SearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .triggerIdStrings(convertToSetWithDelimeter(triggerIdsDelimited, ","))
                .activityDateStart(activityDateStart)
                .activityDateEnd(activityDateEnd)
                .build();
        List<QuestionableActivity> filteredQuestionableActivities
            = questionableActivitySearchService.getFilteredQuestionableActivities(searchRequest);

        List<List<String>> rows = filteredQuestionableActivities.stream()
                .map(qa -> qa.toCsvFormat())
                .collect(Collectors.toList());

        File file = new File("questionable-activity-" + System.currentTimeMillis() + ".csv");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            writer.write('\ufeff');
            for (List<String> row : rows) {
                csvPrinter.printRecord(row);
            }
        } catch (final IOException ex) {
            LOGGER.error("Could not write file " + file.getName(), ex);
        }
        fileUtils.streamFileAsResponse(file, "text/csv", response);
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
