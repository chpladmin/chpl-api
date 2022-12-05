package gov.healthit.chpl.web.controller;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ComplianceSearchFilter;
import gov.healthit.chpl.search.domain.ListingSearchResponse;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "search", description = "Search all CHPL listing data.")
@RestController
@RequestMapping("/search")
public class SearchController {
    private ListingSearchService searchService;

    @Autowired
    public SearchController(ListingSearchService searchService) {
        this.searchService = searchService;
    }

    @Operation(summary = "Search the CHPL with an HTTP POST Request.",
            description = "This endpoint will always use the oldest, valid version of the "
                    + "POST /search/vX endpoint.  The current version being used is v2.  For the "
                    + "current documentation, see POST /search/v2.",
            security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ListingSearchResponse search(@RequestBody SearchRequest searchRequest)
            throws ValidationException {
        return searchV2(searchRequest);
    }
    
    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search the CHPL",
            description = "This endpoint will always use the oldest, valid version of the "
                    + "/search/vX endpoint.  The current version being used is v2.  For the "
                    + "current documentation, see /search/v2.",
            security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(method = RequestMethod.GET, produces = {"application/json; charset=utf-8", "application/xml"})
    public @ResponseBody ListingSearchResponse search(
        @Parameter(description = "CHPL ID, Developer (or previous developer) Name, Product Name, ONC-ACB Certification ID",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
        @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of certification statuses (ex: \"Active,Retired,Withdrawn by Developer\"). Results may match any of the provided statuses.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationStatuses")
        @RequestParam(value = "certificationStatuses", required = false, defaultValue = "") String certificationStatusesDelimited,
        @Parameter(description = "A comma-separated list of certification edition years (ex: \"2014,2015\" finds listings with either edition 2014 or 2015). Allowable values are 2011, 2014, 2015. Results may match any of the provided edition years.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationEditions", deprecated = true)
        @RequestParam(value = "certificationEditions", required = false, defaultValue = "") @Deprecated String certificationEditionsDelimited,
        @Parameter(description = "A comma-separated list of derived certification editions (ex: \"2015,2015 Cures Update\" finds listings that are either 2015 or 2015 Cures Update). Allowable values are 2011, 2014, 2015, and \"2015 Cures Update\". Results may match any of the provided derived editions.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "derivedCertificationEditions")
        @RequestParam(value = "derivedCertificationEditions", required = false, defaultValue = "") String derivedCertificationEditionsDelimited,
        @Parameter(description = "A comma-separated list of certification criteria IDs to be queried together (ex: \"1,2\" finds listings attesting to 170.315 (a)(1) or 170.315 (a)(2)).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaIds")
        @RequestParam(value = "certificationCriteriaIds", required = false, defaultValue = "") String certificationCriteriaIdsDelimited,
        @Parameter(description = "Either AND or OR. Defaults to OR. "
                + "Indicates whether a listing must have all certificationCriteriaIds or "
                + "may have any one or more of the certificationCriteriaIds.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaOperator")
        @RequestParam(value = "certificationCriteriaOperator", required = false, defaultValue = "OR") String certificationCriteriaOperatorStr,
        @Parameter(description = "A comma-separated list of cqms to be queried together (ex: \"CMS2,CMS9\" "
                + "finds listings with either CMS2 or CMS9).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "cqms")
        @RequestParam(value = "cqms", required = false, defaultValue = "") String cqmsDelimited,
        @Parameter(description = "Either AND or OR. Defaults to OR. "
                + "Indicates whether a listing must have all cqms or may have any one or more of the cqms.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "cqmsOperator")
        @RequestParam(value = "cqmsOperator", required = false, defaultValue = "OR") String cqmsOperatorStr,
        @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds listings belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
        @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
        @Parameter(description = "True or False if a listing has ever had surveillance or direct reviews.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "hasHadComplianceActivity")
        @RequestParam(value = "hasHadComplianceActivity", required = false, defaultValue = "") Boolean hasHadComplianceActivity,
        @Parameter(description = "A comma-separated list of non-conformity search options applied across surveillance and direct review activity. "
                + "Valid options are OPEN_NONCONFORMITY, CLOSED_NONCONFORMITY, NEVER_NONCONFORMITY,"
                + "NOT_OPEN_NONCONFORMITY, NOT_CLOSED_NONCONFORMITY, and NOT_NEVER_NONCONFORMITY.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "nonConformityOptions")
        @RequestParam(value = "nonConformityOptions", required = false, defaultValue = "") String nonConformityOptionsDelimited,
        @Parameter(description = "Either AND or OR. Defaults to OR."
                + "Indicates whether a listing must have met all nonConformityOptions "
                + "specified or may have met any one or more of the nonConformityOptions",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "nonConformityOptionsOperator")
        @RequestParam(value = "nonConformityOptionsOperator", required = false, defaultValue = "OR") String nonConformityOptionsOperator,
        @Parameter(description = "A comma-separated list of Real World Testing search options. "
                + "Valid options are HAS_PLANS_URL, HAS_RESULTS_URL, NO_PLANS_URL, NO_RESULTS_URL",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "rwtOptions")
        @RequestParam(value = "rwtOptions", required = false, defaultValue = "") String rwtOptionsDelimited,
        @Parameter(description = "Either AND or OR. Defaults to OR."
                + "Indicates whether a listing must have met all rwtOptions specified or may have met any one or more of the rwtOptions",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "rwtOperator")
        @RequestParam(value = "rwtOperator", required = false, defaultValue = "OR") String rwtOperator,
        @Parameter(description = "The full name of a developer.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "developer")
        @RequestParam(value = "developer", required = false, defaultValue = "") String developer,
        @Parameter(description = "The full name of a product.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "product")
        @RequestParam(value = "product", required = false, defaultValue = "") String product,
        @Parameter(description = "The full name of a version.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "version")
        @RequestParam(value = "version", required = false, defaultValue = "") String version,
        @Parameter(description = "A practice type (either Ambulatory or Inpatient). Valid only for 2014 listings.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "practiceType")
        @RequestParam(value = "practiceType", required = false, defaultValue = "") String practiceType,
        @Parameter(description = "To return only listings certified after this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationDateStart")
        @RequestParam(value = "certificationDateStart", required = false, defaultValue = "") String certificationDateStart,
        @Parameter(description = "To return only listings certified before this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationDateEnd")
        @RequestParam(value = "certificationDateEnd", required = false, defaultValue = "") String certificationDateEnd,
        @Parameter(description = "To return only listings decertified after this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
        @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
        @Parameter(description = "To return only listings decertified before this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateEnd")
        @RequestParam(value = "decertificationDateEnd", required = false, defaultValue = "") String decertificationDateEnd,
        @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
        @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
        @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @Parameter(description = "What to order by. Options are one of the following: CERTIFICATION_DATE, CHPL_ID, "
                + "DEVELOPER, PRODUCT, VERSION, EDITION, or STATUS. Defaults to PRODUCT.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
        @RequestParam(value = "orderBy", required = false, defaultValue = "product") String orderBy,
        @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
        @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        return searchV2(searchTerm, certificationStatusesDelimited, certificationEditionsDelimited, derivedCertificationEditionsDelimited,
                certificationCriteriaIdsDelimited, certificationCriteriaOperatorStr, cqmsDelimited, cqmsOperatorStr, certificationBodiesDelimited,
                hasHadComplianceActivity, nonConformityOptionsDelimited, nonConformityOptionsOperator, rwtOptionsDelimited, rwtOperator, developer,
                product, version, practiceType, certificationDateStart, certificationDateEnd, decertificationDateStart, decertificationDateEnd,
                pageNumber, pageSize, orderBy, sortDescending);
    }

    /*****************************************************************
      TODO: If/when this endpoint becomes deprecated, we should create a new
      annotation that is applied to /search to indicate that the
      request/response interfaces for the endpoint will change when we
      remove /search/v2 and point /search to the newer version.  There
      should be an accompanying job that sends an email to users of the
      endpoint indicating that there breaking change regarding interface.
    *****************************************************************/
    @Operation(summary = "Search the CHPL with an HTTP POST Request.",
            description = "Search the CHPL by specifycing multiple fields of the data to search. "
                    + "If paging fields are not specified, the first 20 records are returned by default.",
            security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "/v2", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ListingSearchResponse searchV2(@RequestBody SearchRequest searchRequest)
            throws ValidationException {
        return searchService.findListings(searchRequest);
    }

    /*****************************************************************
      TODO: If/when this endpoint becomes deprecated, we should create a new
      annotation that is applied to /search to indicate that the
      request/response interfaces for the endpoint will change when we
      remove /search/v2 and point /search to the newer version.  There
      should be an accompanying job that sends an email to users of the
      endpoint indicating that there breaking change regarding interface.
    *****************************************************************/
    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search the CHPL",
            description = "If paging parameters are not specified, the first 20 records are returned by default. "
                    + "All parameters are optional. "
                    + "Any parameter that can accept multiple things (i.e. certificationStatuses) expects "
                    + "a comma-delimited list of those things (i.e. certificationStatuses = Active,Suspended). "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT + ". ",
            security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "/v2", method = RequestMethod.GET, produces = {"application/json; charset=utf-8", "application/xml"})
    public @ResponseBody ListingSearchResponse searchV2(
        @Parameter(description = "CHPL ID, Developer (or previous developer) Name, Product Name, ONC-ACB Certification ID",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
        @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of certification statuses (ex: \"Active,Retired,Withdrawn by Developer\"). Results may match any of the provided statuses.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationStatuses")
        @RequestParam(value = "certificationStatuses", required = false, defaultValue = "") String certificationStatusesDelimited,
        @Parameter(description = "A comma-separated list of certification edition years (ex: \"2014,2015\" finds listings with either edition 2014 or 2015). Allowable values are 2011, 2014, 2015. Results may match any of the provided edition years.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationEditions", deprecated = true)
        @RequestParam(value = "certificationEditions", required = false, defaultValue = "")
        @Deprecated String certificationEditionsDelimited,
        @Parameter(description = "A comma-separated list of derived certification editions (ex: \"2015,2015 Cures Update\" finds listings that are either 2015 or 2015 Cures Update). Allowable values are 2011, 2014, 2015, and \"2015 Cures Update\". Results may match any of the provided derived editions.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "derivedCertificationEditions")
        @RequestParam(value = "derivedCertificationEditions", required = false, defaultValue = "") String derivedCertificationEditionsDelimited,
        @Parameter(description = "A comma-separated list of certification criteria IDs to be queried together (ex: \"1,2\" finds listings attesting to 170.315 (a)(1) or 170.315 (a)(2)).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaIds")
        @RequestParam(value = "certificationCriteriaIds", required = false, defaultValue = "") String certificationCriteriaIdsDelimited,
        @Parameter(description = "Either AND or OR. Defaults to OR. "
                + "Indicates whether a listing must have all certificationCriteriaIds or "
                + "may have any one or more of the certificationCriteriaIds.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaOperator")
        @RequestParam(value = "certificationCriteriaOperator", required = false, defaultValue = "OR") String certificationCriteriaOperatorStr,
        @Parameter(description = "A comma-separated list of cqms to be queried together (ex: \"CMS2,CMS9\" "
                + "finds listings with either CMS2 or CMS9).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "cqms")
        @RequestParam(value = "cqms", required = false, defaultValue = "") String cqmsDelimited,
        @Parameter(description = "Either AND or OR. Defaults to OR. "
                + "Indicates whether a listing must have all cqms or may have any one or more of the cqms.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "cqmsOperator")
        @RequestParam(value = "cqmsOperator", required = false, defaultValue = "OR") String cqmsOperatorStr,
        @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                + "(ex: \"Drummond,ICSA\" finds listings belonging to either Drummond or ICSA).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
        @RequestParam(value = "certificationBodies", required = false, defaultValue = "") String certificationBodiesDelimited,
        @Parameter(description = "True or False if a listing has ever had surveillance or direct reviews.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "hasHadComplianceActivity")
        @RequestParam(value = "hasHadComplianceActivity", required = false, defaultValue = "") Boolean hasHadComplianceActivity,
        @Parameter(description = "A comma-separated list of non-conformity search options applied across surveillance and direct review activity. "
                + "Valid options are OPEN_NONCONFORMITY, CLOSED_NONCONFORMITY, NEVER_NONCONFORMITY,"
                + "NOT_OPEN_NONCONFORMITY, NOT_CLOSED_NONCONFORMITY, and NOT_NEVER_NONCONFORMITY.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "nonConformityOptions")
        @RequestParam(value = "nonConformityOptions", required = false, defaultValue = "") String nonConformityOptionsDelimited,
        @Parameter(description = "Either AND or OR. Defaults to OR."
                + "Indicates whether a listing must have met all nonConformityOptions "
                + "specified or may have met any one or more of the nonConformityOptions",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "nonConformityOptionsOperator")
        @RequestParam(value = "nonConformityOptionsOperator", required = false, defaultValue = "OR") String nonConformityOptionsOperator,
        @Parameter(description = "A comma-separated list of Real World Testing search options. "
                + "Valid options are HAS_PLANS_URL, HAS_RESULTS_URL, NO_PLANS_URL, NO_RESULTS_URL",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "rwtOptions")
        @RequestParam(value = "rwtOptions", required = false, defaultValue = "") String rwtOptionsDelimited,
        @Parameter(description = "Either AND or OR. Defaults to OR."
                + "Indicates whether a listing must have met all rwtOptions specified or may have met any one or more of the rwtOptions",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "rwtOperator")
        @RequestParam(value = "rwtOperator", required = false, defaultValue = "OR") String rwtOperator,
        @Parameter(description = "The full name of a developer.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "developer")
        @RequestParam(value = "developer", required = false, defaultValue = "") String developer,
        @Parameter(description = "The full name of a product.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "product")
        @RequestParam(value = "product", required = false, defaultValue = "") String product,
        @Parameter(description = "The full name of a version.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "version")
        @RequestParam(value = "version", required = false, defaultValue = "") String version,
        @Parameter(description = "A practice type (either Ambulatory or Inpatient). Valid only for 2014 listings.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "practiceType")
        @RequestParam(value = "practiceType", required = false, defaultValue = "") String practiceType,
        @Parameter(description = "To return only listings certified after this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationDateStart")
        @RequestParam(value = "certificationDateStart", required = false, defaultValue = "") String certificationDateStart,
        @Parameter(description = "To return only listings certified before this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationDateEnd")
        @RequestParam(value = "certificationDateEnd", required = false, defaultValue = "") String certificationDateEnd,
        @Parameter(description = "To return only listings decertified after this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateStart")
        @RequestParam(value = "decertificationDateStart", required = false, defaultValue = "") String decertificationDateStart,
        @Parameter(description = "To return only listings decertified before this date. Required format is " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "decertificationDateEnd")
        @RequestParam(value = "decertificationDateEnd", required = false, defaultValue = "") String decertificationDateEnd,
        @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
        @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
        @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @Parameter(description = "What to order by. Options are one of the following: CERTIFICATION_DATE, CHPL_ID, "
                + "DEVELOPER, PRODUCT, VERSION, EDITION, or STATUS. Defaults to PRODUCT.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
        @RequestParam(value = "orderBy", required = false, defaultValue = "product") String orderBy,
        @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
        @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        SearchRequest searchRequest = SearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .certificationStatuses(convertToSetWithDelimeter(certificationStatusesDelimited, ","))
                .derivedCertificationEditions(convertToSetWithDelimeter(derivedCertificationEditionsDelimited, ","))
                .certificationEditions(convertToSetWithDelimeter(certificationEditionsDelimited, ","))
                .certificationCriteriaIdStrings(convertToSetWithDelimeter(certificationCriteriaIdsDelimited, ","))
                .certificationCriteriaOperatorString(certificationCriteriaOperatorStr)
                .cqms(convertToSetWithDelimeter(cqmsDelimited, ","))
                .cqmsOperatorString(cqmsOperatorStr)
                .certificationBodies(convertToSetWithDelimeter(certificationBodiesDelimited, ","))
                .complianceActivity(ComplianceSearchFilter.builder()
                        .hasHadComplianceActivity(hasHadComplianceActivity)
                        .nonConformityOptionsStrings(convertToSetWithDelimeter(nonConformityOptionsDelimited, ","))
                        .nonConformityOptionsOperatorString(nonConformityOptionsOperator)
                        .build())
                .rwtOptionsStrings(convertToSetWithDelimeter(rwtOptionsDelimited, ","))
                .rwtOperatorString(rwtOperator)
                .developer(developer)
                .product(product)
                .version(version)
                .practiceType(practiceType)
                .certificationDateStart(certificationDateStart)
                .certificationDateEnd(certificationDateEnd)
                .decertificationDateStart(decertificationDateStart)
                .decertificationDateEnd(decertificationDateEnd)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return searchService.findListings(searchRequest);
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
