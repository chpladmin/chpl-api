package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.targeteduser.TargetedUserWithUsage;
import gov.healthit.chpl.targeteduser.search.OrderByOption;
import gov.healthit.chpl.targeteduser.search.SearchRequest;
import gov.healthit.chpl.targeteduser.search.TargetedUserSearchResponse;
import gov.healthit.chpl.targeteduser.search.TargetedUserSearchService;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Tag(name = "search-targeted-users", description = "Allows searching for Targeted Users.")
@RestController
@RequestMapping("/targeted-users")
@Log4j2
public class SearchTargetedUsersController {
    private static final String DOWNLOAD_FILENAME_BEGIN = "targeted-users";

    private TargetedUserSearchService targetedUserSearchService;
    private FileUtils fileUtils;

    @Autowired
    public SearchTargetedUsersController(TargetedUserSearchService targetedUserSearchService,
            FileUtils fileUtils) {
        this.targetedUserSearchService = targetedUserSearchService;
        this.fileUtils = fileUtils;
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search Targeted Users in the CHPL.",
            description = "If paging parameters are not specified, the first 20 records are returned by default."
                    + "All parameters are optional. "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.DATE_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody TargetedUserSearchResponse search(
        @Parameter(description = "Targeted User name",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "Whether the targeted user is present on any listings",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "isUsed")
            @RequestParam(value = "isUsed", required = false, defaultValue = "") String isUsed,
        @Parameter(description = "To return only targeted users that were created on or after this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "creationDateStart")
            @RequestParam(value = "creationDateStart", required = false, defaultValue = "") String creationDateStart,
        @Parameter(description = "To return only targeted users that were created on or before this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "creationDateEnd")
            @RequestParam(value = "creationDateEnd", required = false, defaultValue = "") String creationDateEnd,
        @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
        @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @Parameter(description = "What to order by. Options are one of the following: NAME, CREATION_DATE, USAGE_COUNT. "
                + "Defaults to NAME.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "name") String orderBy,
        @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        SearchRequest searchRequest = SearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .isUsed(isUsed)
                .creationDateStart(creationDateStart)
                .creationDateEnd(creationDateEnd)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return targetedUserSearchService.searchTargetedUsers(searchRequest);
    }

    @Operation(summary = "Download Targeted Users. ",
            description = "All parameters are optional. "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.DATE_SEARCH_FORMAT + ". ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = {"text/csv; charset=utf-8"})
    public void download(@Parameter(description = "Targeted User name",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "Whether the targeted user is present on any listings",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "isUsed")
            @RequestParam(value = "isUsed", required = false, defaultValue = "") String isUsed,
        @Parameter(description = "To return only targeted users that were created on or after this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "creationDateStart")
            @RequestParam(value = "creationDateStart", required = false, defaultValue = "") String creationDateStart,
        @Parameter(description = "To return only targeted users that were created on or before this date. Required format is " + SearchRequest.DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "creationDateEnd")
            @RequestParam(value = "creationDateEnd", required = false, defaultValue = "") String creationDateEnd,
            HttpServletRequest request, HttpServletResponse response)
        throws InvalidArgumentsException, ValidationException, IOException {

        SearchRequest searchRequest = SearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .isUsed(isUsed)
                .creationDateStart(creationDateStart)
                .creationDateEnd(creationDateEnd)
                .orderBy(OrderByOption.NAME)
                .sortDescending(true)
                .build();
        List<TargetedUserWithUsage> filteredTargetedUsers = targetedUserSearchService.getFilteredTargetedUsers(searchRequest);

        List<List<String>> rows = filteredTargetedUsers.stream()
                .map(tu -> tu.toListOfStringsForCsv())
                .collect(Collectors.toList());

        File file = File.createTempFile(DOWNLOAD_FILENAME_BEGIN, ".csv");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            writer.write('\ufeff');
            csvPrinter.printRecord(TargetedUserWithUsage.CSV_HEADINGS);
            for (List<String> row : rows) {
                csvPrinter.printRecord(row);
            }
        } catch (final IOException ex) {
            LOGGER.error("Could not write file " + file.getName(), ex);
        }
        fileUtils.streamFileAsResponse(file, "text/csv", response);
        try {
            file.delete();
        } catch (Exception ex) {
            LOGGER.warn("Temp file " + file.getAbsolutePath() + " could not be deleted.", ex);
        }
    }
}
