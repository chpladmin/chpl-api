package gov.healthit.chpl.web.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResponse;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.developer.search.csv.DeveloperCsvWriter;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.DateUtil;
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

@Tag(name = "search-developers", description = "Allows searching for developers.")
@RestController
@RequestMapping("/developers/search")
@Log4j2
public class SearchDevelopersController {
    private static final String DOWNLOAD_FILE_FORMAT = "text/csv";
    private DeveloperSearchService developerSearchService;
    private DeveloperCsvWriter developerCsvWriter;
    private FileUtils fileUtils;

    @Autowired
    public SearchDevelopersController(DeveloperSearchService developerSearchService,
            DeveloperCsvWriter developerCsvWriter,
            FileUtils fileUtils) {
        this.developerSearchService = developerSearchService;
        this.developerCsvWriter = developerCsvWriter;
        this.fileUtils = fileUtils;
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search developers on the CHPL",
    description = "This endpoint will always use the oldest, valid version of the "
            + "/developers/search/vX endpoint. The current version being used is v3. For the "
            + "current documentation, see /developers/search/v3.",
            security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperSearchResponse search(
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
            @Parameter(description = "A comma-separated list of filters indicating the status of attestations for the developer over the most recent past period. "
                    + "Valid options are HAS_SUBMITTED, HAS_NOT_SUBMITTED, HAS_PUBLISHED, and HAS_NOT_PUBLISHED.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "attestationsOptions")
            @RequestParam(value = "attestationsOptions", required = false, defaultValue = "") String attestationsOptionsDelimited,
            @Parameter(description = "Either AND or OR. Defaults to OR."
                    + "Indicates whether a developer must have met all attestationsOptions "
                    + "specified or may have met any one or more of the attestationsOptions",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "attestationsOptionsOperator")
            @RequestParam(value = "attestationsOptionsOperator", required = false, defaultValue = "OR") String attestationsOptionsOperator,
            @Parameter(description = "To return only developers decertified on or after this date. Required format is " + DeveloperSearchRequest.DATE_SEARCH_FORMAT,
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
            @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
            @Parameter(description = "To return only developers decertified on or before this date. Required format is " + DeveloperSearchRequest.DATE_SEARCH_FORMAT,
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
            @Parameter(description = "Either true or false. Defaults to null."
                    + "Indicates whether to search for developers that do or do not have users.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "hasUsers")
            @RequestParam(value = "hasUsers", required = false, defaultValue = "") String hasUsers,
            @Parameter(description = "A comma-separated list of Certification Criteria Ids which a developer has listing that attests to. ",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaIds")
            @RequestParam(value = "certificationCriteriaIds", required = false, defaultValue = "") String criteriaIdsDelimited,
            @Parameter(description = "Either AND or OR. Defaults to OR."
                    + "Indicates whether a developer must have met all certificationCriteriaIds "
                    + "specified or may have met any one or more of the certificationCriteriaIds",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaOperator")
            @RequestParam(value = "certificationCriteriaOperator", required = false, defaultValue = "OR") String criteriaIdsOperator,
            @Parameter(description = "Either ACTIVE or ALL. Defaults to ACTIVE."
                    + "Indicates whether criteria attested to is based all of the developer's listings are considered or only the active listings. ",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaOperator")
            @RequestParam(value = "developersListingsCriteriaOption", required = false, defaultValue = "ACTIVE") String developersListingsCriteriaOption,
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

        return searchV3(searchTerm, acbsForActiveLisitngsDelimited, acbsForAllLisitngsDelimited,
                statusesDelimited, attestationsOptionsDelimited, attestationsOptionsOperator, decertificationDateStart,
                decertificationDateEnd, activeListingsOptionsDelimited, activeListingsOptionsOperator, hasUsers,
                criteriaIdsDelimited, criteriaIdsOperator, developersListingsCriteriaOption, pageNumber, pageSize, orderBy, sortDescending);
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
            @Parameter(description = "A comma-separated list of filters indicating the status of attestations for the developer over the most recent past period. "
                    + "Valid options are HAS_SUBMITTED, HAS_NOT_SUBMITTED, HAS_PUBLISHED, and HAS_NOT_PUBLISHED.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "attestationsOptions")
            @RequestParam(value = "attestationsOptions", required = false, defaultValue = "") String attestationsOptionsDelimited,
            @Parameter(description = "Either AND or OR. Defaults to OR."
                    + "Indicates whether a developer must have met all attestationsOptions "
                    + "specified or may have met any one or more of the attestationsOptions",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "attestationsOptionsOperator")
            @RequestParam(value = "attestationsOptionsOperator", required = false, defaultValue = "OR") String attestationsOptionsOperator,
            @Parameter(description = "To return only developers decertified on or after this date. Required format is " + DeveloperSearchRequest.DATE_SEARCH_FORMAT,
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
            @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
            @Parameter(description = "To return only developers decertified on or before this date. Required format is " + DeveloperSearchRequest.DATE_SEARCH_FORMAT,
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
            @Parameter(description = "Either true or false. Defaults to null."
                    + "Indicates whether to search for developers that do or do not have users.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "hasUsers")
            @RequestParam(value = "hasUsers", required = false, defaultValue = "") String hasUsers,
            @Parameter(description = "A comma-separated list of Certification Criteria Ids which a developer has listing that attests to. ",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaIds")
            @RequestParam(value = "certificationCriteriaIds", required = false, defaultValue = "") String criteriaIdsDelimited,
            @Parameter(description = "Either AND or OR. Defaults to OR."
                    + "Indicates whether a developer must have met all certificationCriteriaIds "
                    + "specified or may have met any one or more of the certificationCriteriaIds",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaOperator")
            @RequestParam(value = "certificationCriteriaOperator", required = false, defaultValue = "OR") String criteriaIdsOperator,
            @Parameter(description = "Either ACTIVE or ALL. Defaults to ACTIVE."
                    + "Indicates whether criteria attested to is based all of the developer's listings are considered or only the active listings. ",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "developersListingsCriteriaOption")
            @RequestParam(value = "developersListingsCriteriaOption", required = false, defaultValue = "ACTIVE") String developersListingsCriteriaOption,
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
                .attestationsOptionsStrings(convertToSetWithDelimeter(attestationsOptionsDelimited, ","))
                .attestationsOptionsOperatorString(attestationsOptionsOperator)
                .decertificationDateStart(decertificationDateStart)
                .decertificationDateEnd(decertificationDateEnd)
                .activeListingsOptionsStrings(convertToSetWithDelimeter(activeListingsOptionsDelimited, ","))
                .activeListingsOptionsOperatorString(activeListingsOptionsOperator)
                .criteriaIdsOperatorString(criteriaIdsOperator)
                .criteriaIdsStrings(convertToSetWithDelimeter(criteriaIdsDelimited, ","))
                .developersListingsCriteriaOptionString(developersListingsCriteriaOption)
                .hasUsers(!StringUtils.isEmpty(hasUsers) ? BooleanUtils.toBooleanObject(hasUsers) : null)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return developerSearchService.findDevelopers(searchRequest);
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Return the set of developers matching the supplied filters as a text/CSV file, "
            + "which most browsers will interpret as a download. Any paging parameters will be ignored.",
            description = "All parameters are optional. "
                    + "Any parameter that can accept multiple things (i.e. acbsForActiveListings) expects "
                    + "a comma-delimited list of those things (i.e. acbsForActiveListings=Drummond,ICSA Labs). "
                    + "Date parameters are required to be in the format "
                    + DeveloperSearchRequest.DATE_SEARCH_FORMAT + ". ",
                    security = {
                            @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = DOWNLOAD_FILE_FORMAT)
    public void downloadV3(
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
            @Parameter(description = "A comma-separated list of filters indicating the status of attestations for the developer over the most recent past period. "
                    + "Valid options are HAS_SUBMITTED, HAS_NOT_SUBMITTED, HAS_PUBLISHED, and HAS_NOT_PUBLISHED.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "attestationsOptions")
            @RequestParam(value = "attestationsOptions", required = false, defaultValue = "") String attestationsOptionsDelimited,
            @Parameter(description = "Either AND or OR. Defaults to OR."
                    + "Indicates whether a developer must have met all attestationsOptions "
                    + "specified or may have met any one or more of the attestationsOptions",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "attestationsOptionsOperator")
            @RequestParam(value = "attestationsOptionsOperator", required = false, defaultValue = "OR") String attestationsOptionsOperator,
            @Parameter(description = "To return only developers decertified on or after this date. Required format is " + DeveloperSearchRequest.DATE_SEARCH_FORMAT,
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
            @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
            @Parameter(description = "To return only developers decertified on or before this date. Required format is " + DeveloperSearchRequest.DATE_SEARCH_FORMAT,
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
            @Parameter(description = "A comma-separated list of Certification Criteria Ids which a developer has listing that attests to. ",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaIds")
            @RequestParam(value = "certificationCriteriaIds", required = false, defaultValue = "") String criteriaIdsDelimited,
            @Parameter(description = "Either AND or OR. Defaults to OR."
                    + "Indicates whether a developer must have met all certificationCriteriaIds "
                    + "specified or may have met any one or more of the certificationCriteriaIds",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaOperator")
            @RequestParam(value = "certificationCriteriaOperator", required = false, defaultValue = "OR") String criteriaIdsOperator,
            @Parameter(description = "Either ACTIVE or ALL. Defaults to ACTIVE."
                    + "Indicates whether criteria attested to is based all of the developer's listings are considered or only the active listings. ",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "developersListingsCriteriaOption")
            @RequestParam(value = "developersListingsCriteriaOption", required = false, defaultValue = "ACTIVE") String developersListingsCriteriaOption,
            @Parameter(description = "Either true or false. Defaults to null."
                    + "Indicates whether to search for developers that do or do not have users.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "hasUsers")
            @RequestParam(value = "hasUsers", required = false, defaultValue = "") String hasUsers,
            @Parameter(description = "What to order by. Options are one of the following: DEVELOPER_NAME, DEVELOPER_CODE, "
                    + "DECERTIFICATION_DATE, or STATUS. Defaults to DEVELOPER_NAME.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "developer_name") String orderBy,
            @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
            allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending,
            HttpServletRequest request, HttpServletResponse response)
                    throws InvalidArgumentsException, ValidationException {

        DeveloperSearchRequest searchRequest = DeveloperSearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .statuses(convertToSetWithDelimeter(statusesDelimited, ","))
                .acbsForActiveListings(convertToSetWithDelimeter(acbsForActiveLisitngsDelimited, ","))
                .acbsForAllListings(convertToSetWithDelimeter(acbsForAllLisitngsDelimited, ","))
                .attestationsOptionsStrings(convertToSetWithDelimeter(attestationsOptionsDelimited, ","))
                .attestationsOptionsOperatorString(attestationsOptionsOperator)
                .decertificationDateStart(decertificationDateStart)
                .decertificationDateEnd(decertificationDateEnd)
                .activeListingsOptionsStrings(convertToSetWithDelimeter(activeListingsOptionsDelimited, ","))
                .activeListingsOptionsOperatorString(activeListingsOptionsOperator)
                .hasUsers(!StringUtils.isEmpty(hasUsers) ? BooleanUtils.toBooleanObject(hasUsers) : null)
                .criteriaIdsOperatorString(criteriaIdsOperator)
                .criteriaIdsStrings(convertToSetWithDelimeter(criteriaIdsDelimited, ","))
                .developersListingsCriteriaOptionString(developersListingsCriteriaOption)
                .pageSize(DeveloperSearchRequest.MAX_PAGE_SIZE)
                .pageNumber(0)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        File tempFile = null;
        try {
            tempFile = developerCsvWriter.getAsCsv(searchRequest, LOGGER);

            String filenameInResponse = String.format("developer-search-results-%s.csv",
                    DateUtil.formatDownloadFileSuffixInEasternTime(LocalDateTime.now()));
            fileUtils.streamFileAsResponse(tempFile, DOWNLOAD_FILE_FORMAT, response, filenameInResponse);
        } catch (Exception ex) {
            LOGGER.error("Unable to return CSV file for developer download.", ex);
        }
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
