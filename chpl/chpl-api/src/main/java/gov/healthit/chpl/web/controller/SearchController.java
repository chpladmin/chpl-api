package gov.healthit.chpl.web.controller;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchResponse;
import gov.healthit.chpl.util.ErrorMessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Api
@RestController
@Loggable
@Log4j2
public class SearchController {
    private ListingSearchService searchService;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SearchController(ListingSearchService searchService,
            ErrorMessageUtil msgUtil) {
        this.searchService = searchService;
        this.msgUtil = msgUtil;
    }

    @SuppressWarnings({"checkstyle:methodlength", "checkstyle:parameternumber"})
    @ApiOperation(value = "Search the CHPL",
    notes = "If paging parameters are not specified, the first 20 records are returned by default. "
            + "All parameters are optional. "
            + "Any parameter that can accept multiple things (i.e. certificationStatuses) expects "
            + "a comma-delimited list of those things (i.e. certificationStatuses = Active,Suspended). "
            + "Date parameters are required to be in the format "
            + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT + ". ")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "searchTerm",
                value = "CHPL ID, Developer (or previous developer) Name, Product Name, ONC-ACB Certification ID",
                required = false,
                dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "certificationStatuses",
        value = "A comma-separated list of certification statuses "
                + "(ex: \"Active,Retired,Withdrawn by Developer\")).",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "certificationEditions",
        value = "A comma-separated list of certification editions to be 'or'ed together "
                + "(ex: \"2014,2015\" finds listings with either edition 2014 or 2015).",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "certificationCriteriaIds",
        value = "A comma-separated list of certification criteria IDs to be queried together "
                + "(ex: \"1,2\" finds listings "
                + "attesting to either 170.315 (a)(1) or 170.315 (a(2)).",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "certificationCriteriaOperator",
        value = "Either AND or OR. Defaults to OR. "
                + "Indicates whether a listing must have all certificationCriteria or "
                + "may have any one or more of the certificationCriteria.",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "cqms",
        value = "A comma-separated list of cqms to be queried together (ex: \"CMS2,CMS9\" "
                + "finds listings with either CMS2 or CMS9).",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "cqmsCriteriaOperator",
        value = "Either AND or OR. Defaults to OR. "
                + "Indicates whether a listing must have all cqms or may have any one or more of the cqms.",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "certificationBodies",
        value = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds listings belonging to either Drummond or ICSA).",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "nonconformityOptions",
        value = "A comma-separated list of nonconformity search options. Valid options are "
                + "OPEN_NONCONFORMITY, CLOSED_NONCONFORMITY, and NEVER_NONCONFORMITY.",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "nonconformityOptionsOperator",
        value = "Either AND or OR. Defaults to OR."
                + "Indicates whether a listing must have met all nonconformityOptions "
                + "specified or may have met any one or more of the nonconformityOptions",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "hasHadComplianceActivity",
        value = "True or False if a listing has ever had compliance activity (Surveillance or Direct Review).", required = false,
        dataType = "boolean", paramType = "query"),
        @ApiImplicitParam(name = "developer", value = "The full name of a developer.", required = false,
        dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "product", value = "The full name of a product.", required = false,
        dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "version", value = "The full name of a version.", required = false,
        dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "practiceType",
        value = "A practice type (either Ambulatory or Inpatient). Valid only for 2014 listings.",
        required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "certificationDateStart",
        value = "To return only listings certified after this date. Required format is "
                + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "certificationDateEnd",
        value = "To return only listings certified before this date. Required format is "
                + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "pageNumber",
        value = "Zero-based page number used in concert with pageSize. Defaults to 0.", required = false,
        dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "pageSize",
        value = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "orderBy",
        value = "What to order by. Options are one of the following: CERTIFICATION_DATE, CHPL_ID, "
                + "DEVELOPER, PRODUCT, VERSION, EDITION, or STATUS. Defaults to PRODUCT.",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "sortDescending",
        value = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
        required = false, dataType = "boolean", paramType = "query")
    })
    @RequestMapping(value = "/search/beta", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody SearchResponse search(
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
            @RequestParam(value = "certificationStatuses", required = false,
            defaultValue = "") String certificationStatusesDelimited,
            @RequestParam(value = "certificationEditions", required = false,
            defaultValue = "") String certificationEditionsDelimited,
            @RequestParam(value = "certificationCriteriaIds", required = false,
            defaultValue = "") String certificationCriteriaIdsDelimited,
            @RequestParam(value = "certificationCriteriaOperator", required = false,
            defaultValue = "OR") String certificationCriteriaOperatorStr,
            @RequestParam(value = "cqms", required = false, defaultValue = "") String cqmsDelimited,
            @RequestParam(value = "cqmsOperator", required = false,
            defaultValue = "OR") String cqmsOperatorStr,
            @RequestParam(value = "certificationBodies", required = false,
            defaultValue = "") String certificationBodiesDelimited,
            @RequestParam(value = "hasHadComplianceActivity", required = false,
            defaultValue = "") Boolean hasHadComplianceActivity,
            @RequestParam(value = "nonconformityOptions", required = false,
            defaultValue = "") String nonconformityOptionsDelimited,
            @RequestParam(value = "nonconformityOptionsOperator", required = false,
            defaultValue = "OR") String nonconformityOptionsOperator,
            @RequestParam(value = "developer", required = false, defaultValue = "") String developer,
            @RequestParam(value = "product", required = false, defaultValue = "") String product,
            @RequestParam(value = "version", required = false, defaultValue = "") String version,
            @RequestParam(value = "practiceType", required = false, defaultValue = "") String practiceType,
            @RequestParam(value = "certificationDateStart", required = false,
            defaultValue = "") String certificationDateStart,
            @RequestParam(value = "certificationDateEnd", required = false,
            defaultValue = "") String certificationDateEnd,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(value = "orderBy", required = false, defaultValue = "product") String orderBy,
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
                    throws InvalidArgumentsException, ValidationException {

        SearchRequest searchRequest = SearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .certificationStatuses(convertToSetWithDelimeter(certificationStatusesDelimited, ","))
                .certificationEditions(convertToSetWithDelimeter(certificationEditionsDelimited, ","))
                .certificationCriteriaIdStrings(convertToSetWithDelimeter(certificationCriteriaIdsDelimited, ","))
                .certificationCriteriaOperatorString(certificationCriteriaOperatorStr)
                .cqms(convertToSetWithDelimeter(cqmsDelimited, ","))
                .cqmsOperatorString(cqmsOperatorStr)
                .certificationBodies(convertToSetWithDelimeter(certificationBodiesDelimited, ","))
                .complianceActivity(ComplianceSearchFilter.builder()
                        .hasHadComplianceActivity(hasHadComplianceActivity)
                        .nonconformityOptionsStrings(convertToSetWithDelimeter(nonconformityOptionsDelimited, ","))
                        .nonconformityOptionsOperatorString(nonconformityOptionsOperator)
                        .build())
                .developer(developer)
                .product(product)
                .version(version)
                .practiceType(practiceType)
                .certificationDateStart(certificationDateStart)
                .certificationDateEnd(certificationDateEnd)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return searchService.search(searchRequest);

    }

    @ApiOperation(value = "Search the CHPL with an HTTP POST Request.",
            notes = "Search the CHPL by specifycing multiple fields of the data to search. "
                    + "If paging fields are not specified, the first 20 records are returned by default.")
    @RequestMapping(value = "/search/beta", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody SearchResponse search(@RequestBody SearchRequest searchRequest)
            throws ValidationException {
        return searchService.search(searchRequest);
    }

    private Set<String> convertToSetWithDelimeter(String delimitedString, String delimeter) {
        if (StringUtils.isEmpty(delimitedString)) {
            return new LinkedHashSet<String>();
        }
        return Stream.of(delimitedString.split(delimeter))
                .map(value -> value.trim())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
