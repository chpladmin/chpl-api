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

import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.subscription.search.SearchRequest;
import gov.healthit.chpl.subscription.search.SubscriptionSearchResponse;
import gov.healthit.chpl.subscription.search.SubscriptionSearchService;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "search-subscriptions", description = "Allows searching for subscriptions.")
@RestController
@RequestMapping("/subscriptions/search")
@Log4j2
public class SearchSubscriptionsController {

    private SubscriptionSearchService subscriptionSearchService;

    @Autowired
    public SearchSubscriptionsController(SubscriptionSearchService subscriptionSearchService) {
        this.subscriptionSearchService = subscriptionSearchService;
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search subscriptions on the CHPL",
        description = "This endpoint will always use the oldest, valid version of the "
                + "/subscriptions/search/vX endpoint. The current version being used is v1. For the "
                + "current documentation, see /subscriptions/search/v1. This is available to ADMIN and ONC roles.",
        security = {
                @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
        })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody SubscriptionSearchResponse search(
        @Parameter(description = "Subscriber email or the name of the subscribed item (i.e. the CHPL product number, developer name, or product name)", allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
        @Parameter(description = "A comma-separated list of subscription subject names to be 'or'ed together "
                + "(ex: \"Certification Criterion Added,Certification Status Changed\" finds subscriptions with any of the supplied subjects).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "subscriptionSubjects")
            @RequestParam(value = "subscriptionSubjects", required = false, defaultValue = "") String subscriptionSubjectsDelimited,
        @Parameter(description = "A comma-separated list of subscription object types to be 'or'ed together "
                + "(ex: \"Listing,Developer\" finds subscriptions in with any of the supplied object types).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "subscriptionObjectTypes")
            @RequestParam(value = "subscriptionObjectTypes", required = false, defaultValue = "") String subscriptionObjectTypesDelimited,
        @Parameter(description = "A comma-separated list of subscriber roles to be 'or'ed together "
                + "(ex: \"Health IT Vendor,App Developer\" finds subscriptions for subscribers with any of the supplied roles).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "subscriberRoles")
            @RequestParam(value = "subscriberRoles", required = false, defaultValue = "") String subscriberRolesDelimited,
        @Parameter(description = "A comma-separated list of subscriber statuses to be 'or'ed together "
                + "(ex: \"Active,Pending\" finds subscriptions for subscribers with any of the supplied statuses).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "subscriberStatuses")
            @RequestParam(value = "subscriberStatuses", required = false, defaultValue = "") String subscriberStatusesDelimited,
        @Parameter(description = "To return only subscriptions created after this date. Required format is " + SearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "creationDateTimeStart")
            @RequestParam(value = "creationDateTimeStart", required = false, defaultValue = "") String creationDateTimeStart,
        @Parameter(description = "To return only subscriptions created before this date. Required format is " + SearchRequest.TIMESTAMP_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "creationDateTimeEnd")
            @RequestParam(value = "creationDateTimeEnd", required = false, defaultValue = "") String creationDateTimeEnd,
        @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
        @Parameter(description = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @Parameter(description = "What to order by. Options are one of the following: CREATION_DATE, "
                + "SUBSCRIBER_EMAIL, or SUBSCRIBER_ROLE. Defaults to CREATION_DATE.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "creation_date") String orderBy,
        @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        return searchV1(searchTerm, subscriptionSubjectsDelimited, subscriptionObjectTypesDelimited, subscriberRolesDelimited,
                subscriberStatusesDelimited, creationDateTimeStart, creationDateTimeEnd, pageNumber, pageSize, orderBy, sortDescending);
    }

    @SuppressWarnings({
        "checkstyle:methodlength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Search the set of subscriptions in the CHPL.",
            description = "If paging parameters are not specified, the first 20 records are returned by default. "
                    + "All parameters are optional. "
                    + "Any parameter that can accept multiple things (i.e. subscriberRoles) expects "
                    + "a comma-delimited list of those things (i.e. subscriberRoles = Active,Pending). "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.TIMESTAMP_SEARCH_FORMAT + ". This is available to ADMIN and ONC roles.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/v1", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody SubscriptionSearchResponse searchV1(
            @Parameter(description = "Subscriber email or the name of the subscribed item (i.e. the CHPL product number, developer name, or product name)", allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
                @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
            @Parameter(description = "A comma-separated list of subscription subject names to be 'or'ed together "
                    + "(ex: \"Certification Criterion Added,Certification Status Changed\" finds subscriptions with any of the supplied subjects).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "subscriptionSubjects")
                @RequestParam(value = "subscriptionSubjects", required = false, defaultValue = "") String subscriptionSubjectsDelimited,
            @Parameter(description = "A comma-separated list of subscription object types to be 'or'ed together "
                    + "(ex: \"Listing,Developer\" finds subscriptions in with any of the supplied object types).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "subscriptionObjectTypes")
                @RequestParam(value = "subscriptionObjectTypes", required = false, defaultValue = "") String subscriptionObjectTypesDelimited,
            @Parameter(description = "A comma-separated list of subscriber roles to be 'or'ed together "
                    + "(ex: \"Health IT Vendor,App Developer\" finds subscriptions for subscribers with any of the supplied roles).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "subscriberRoles")
                @RequestParam(value = "subscriberRoles", required = false, defaultValue = "") String subscriberRolesDelimited,
            @Parameter(description = "A comma-separated list of subscriber statuses to be 'or'ed together "
                    + "(ex: \"Active,Pending\" finds subscriptions for subscribers with any of the supplied statuses).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "subscriberStatuses")
                @RequestParam(value = "subscriberStatuses", required = false, defaultValue = "") String subscriberStatusesDelimited,
            @Parameter(description = "To return only subscriptions created after this date. Required format is " + SearchRequest.TIMESTAMP_SEARCH_FORMAT,
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "creationDateTimeStart")
                @RequestParam(value = "creationDateTimeStart", required = false, defaultValue = "") String creationDateTimeStart,
            @Parameter(description = "To return only subscriptions created before this date. Required format is " + SearchRequest.TIMESTAMP_SEARCH_FORMAT,
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "creationDateTimeEnd")
                @RequestParam(value = "creationDateTimeEnd", required = false, defaultValue = "") String creationDateTimeEnd,
            @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
                @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @Parameter(description = "Number of results to return used in concert with pageNumber. "
                    + "Defaults to 20. Maximum allowed page size is 100.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
                @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
            @Parameter(description = "What to order by. Options are one of the following: CREATION_DATE, "
                    + "SUBSCRIBER_EMAIL, or SUBSCRIBER_ROLE. Defaults to CREATION_DATE.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
                @RequestParam(value = "orderBy", required = false, defaultValue = "creation_date") String orderBy,
            @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
                @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
        throws InvalidArgumentsException, ValidationException {

        SearchRequest searchRequest = SearchRequest.builder()
                .searchTerm(searchTerm.trim())
                .subscriptionSubjects(convertToSetWithDelimeter(subscriptionSubjectsDelimited, ","))
                .subscriptionObjectTypes(convertToSetWithDelimeter(subscriptionObjectTypesDelimited, ","))
                .subscriberRoles(convertToSetWithDelimeter(subscriberRolesDelimited, ","))
                .subscriberStatuses(convertToSetWithDelimeter(subscriberStatusesDelimited, ","))
                .creationDateTimeStart(creationDateTimeStart)
                .creationDateTimeEnd(creationDateTimeEnd)
                .pageSize(pageSize)
                .pageNumber(pageNumber)
                .orderByString(orderBy)
                .sortDescending(sortDescending)
                .build();
        return subscriptionSearchService.findSubscriptions(searchRequest);
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
