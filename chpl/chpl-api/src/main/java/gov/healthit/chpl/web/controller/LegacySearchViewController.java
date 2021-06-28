package gov.healthit.chpl.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.search.SearchRequestLegacy;
import gov.healthit.chpl.domain.search.SearchResponseLegacy;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.search.CertifiedProductSearchManager;
import gov.healthit.chpl.search.domain.NonConformitySearchOptions;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "search", description = "Allows searching of the CHPL.")
@RestController
@Loggable
@Log4j2
@Deprecated
public class LegacySearchViewController {
    private static final int MAX_PAGE_SIZE = 100;

    private MessageSource messageSource;
    private DimensionalDataManager dimensionalDataManager;
    private CertifiedProductSearchManager certifiedProductSearchManager;

    @Autowired
    public LegacySearchViewController(MessageSource messageSource,
            DimensionalDataManager dimensionalDataManager,
            CertifiedProductSearchManager certifiedProductSearchManager) {
        this.messageSource = messageSource;
        this.dimensionalDataManager = dimensionalDataManager;
        this.certifiedProductSearchManager = certifiedProductSearchManager;
    }

    @SuppressWarnings({"checkstyle:methodlength", "checkstyle:parameternumber"})
    @Operation(summary = "Search the CHPL",
        description = "If paging parameters are not specified, the first 20 records are returned by default. "
            + "All parameters are optional. "
            + "Any parameter that can accept multiple things (i.e. certificationStatuses) expects "
            + "a comma-delimited list of those things (i.e. certificationStatuses = Active,Suspended). "
            + "Date parameters are required to be in the format "
            + SearchRequestLegacy.CERTIFICATION_DATE_SEARCH_FORMAT + ". ",
            deprecated = true)
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody SearchResponseLegacy searchGet(
            @Parameter(description = "CHPL ID, Developer (or previous developer) Name, Product Name, ONC-ACB Certification ID",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "searchTerm")
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
            @Parameter(description = "A comma-separated list of certification statuses (ex: \"Active,Retired,Withdrawn by Developer\"). Results may match any of the provided statuses.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationStatuses")
            @RequestParam(value = "certificationStatuses", required = false,
            defaultValue = "") String certificationStatusesDelimited,
            @Parameter(description = "A comma-separated list of certification edition years (ex: \"2014,2015\" finds listings with either edition 2014 or 2015). Results may match any of the provided edition years.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationEditions")
            @RequestParam(value = "certificationEditions", required = false,
            defaultValue = "") String certificationEditionsDelimited,
            @Parameter(description = "A comma-separated list of certification criteria to be queried together (ex: \"170.314 (a)(1),170.314 (a)(2)\" finds listings attesting to either 170.314 (a)(1) or 170.314 (a(2)).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteria")
            @RequestParam(value = "certificationCriteria", required = false,
            defaultValue = "") String certificationCriteriaDelimited,
            @Parameter(description = "Either AND or OR. Defaults to OR. "
                    + "Indicates whether a listing must have all certificationCriteria or "
                    + "may have any one or more of the certificationCriteria.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationCriteriaOperator")
            @RequestParam(value = "certificationCriteriaOperator", required = false,
            defaultValue = "OR") String certificationCriteriaOperatorStr,
            @Parameter(description = "A comma-separated list of cqms to be queried together (ex: \"CMS2,CMS9\" "
                    + "finds listings with either CMS2 or CMS9).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "cqms")
            @RequestParam(value = "cqms", required = false, defaultValue = "") String cqmsDelimited,
            @Parameter(description = "Either AND or OR. Defaults to OR. "
                    + "Indicates whether a listing must have all cqms or may have any one or more of the cqms.",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "cqmsOperator")
            @RequestParam(value = "cqmsOperator", required = false,
            defaultValue = "OR") String cqmsOperatorStr,
            @Parameter(description = "A comma-separated list of certification body names to be 'or'ed together "
                    + "(ex: \"Drummond,ICSA\" finds listings belonging to either Drummond or ICSA).",
                    allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationBodies")
            @RequestParam(value = "certificationBodies", required = false,
            defaultValue = "") String certificationBodiesDelimited,
            @Parameter(description = "True or False if a listing has ever had surveillance.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "hasHadSurveillance")
            @RequestParam(value = "hasHadSurveillance", required = false,
            defaultValue = "") String hasHadSurveillanceStr,
            @Parameter(description = "A comma-separated list of nonconformity search options. Valid options are "
                    + "OPEN_NONCONFORMITY, CLOSED_NONCONFORMITY, and NEVER_NONCONFORMITY.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "nonconformityOptions")
            @RequestParam(value = "nonconformityOptions", required = false,
            defaultValue = "") String nonconformityOptionsDelimited,
            @Parameter(description = "Either AND or OR. Defaults to OR."
                    + "Indicates whether a listing must have met all nonconformityOptions "
                    + "specified or may have met any one or more of the nonconformityOptions",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "nonconformityOptionsOperator")
            @RequestParam(value = "nonconformityOptionsOperator", required = false,
            defaultValue = "OR") String nonconformityOptionsOperator,
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
            @Parameter(description = "To return only listings certified after this date. Required format is "
                    + SearchRequestLegacy.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationDateStart")
            @RequestParam(value = "certificationDateStart", required = false,
            defaultValue = "") String certificationDateStart,
            @Parameter(description = "To return only listings certified before this date. Required format is "
                    + SearchRequestLegacy.CERTIFICATION_DATE_SEARCH_FORMAT,
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "certificationDateEnd")
            @RequestParam(value = "certificationDateEnd", required = false, defaultValue = "") String certificationDateEnd,
            @Parameter(description = "Zero-based page number used in concert with pageSize. Defaults to 0.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageNumber")
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @Parameter(description = "Number of results to return used in concert with pageNumber. "
                    + "Defaults to 20. Maximum allowed page size is 100.",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "pageSize")
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
            @Parameter(description = "What to order by. Options are one of the following: "
                    + SearchRequestLegacy.ORDER_BY_DEVELOPER + ", " + SearchRequestLegacy.ORDER_BY_PRODUCT + ", "
                    + SearchRequestLegacy.ORDER_BY_VERSION + ", " + SearchRequestLegacy.ORDER_BY_CERTIFICATION_EDITION + ", "
                    + ", or " + SearchRequestLegacy.ORDER_BY_CERTIFICATION_BODY + ", "
                    + ". Defaults to " + SearchRequestLegacy.ORDER_BY_PRODUCT + ".",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "orderBy")
            @RequestParam(value = "orderBy", required = false, defaultValue = "product") String orderBy,
            @Parameter(description = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
                allowEmptyValue = true, in = ParameterIn.QUERY, name = "sortDescending")
            @RequestParam(value = "sortDescending", required = false, defaultValue = "false") Boolean sortDescending)
                    throws InvalidArgumentsException, EntityRetrievalException {

        SearchRequestLegacy searchRequest = new SearchRequestLegacy();
        if (searchTerm != null) {
            searchRequest.setSearchTerm(searchTerm.trim());
        }

        if (certificationStatusesDelimited != null) {
            String certificationStatusesDelimitedTrimmed = certificationStatusesDelimited.trim();
            if (!StringUtils.isEmpty(certificationStatusesDelimitedTrimmed)) {
                String[] certificationStatusArr = certificationStatusesDelimitedTrimmed.split(",");
                if (certificationStatusArr.length > 0) {
                    Set<String> certificationStatuses = new HashSet<String>();
                    Set<KeyValueModel> availableCertificationStatuses = dimensionalDataManager.getCertificationStatuses();

                    for (int i = 0; i < certificationStatusArr.length; i++) {
                        String certStatusParam = certificationStatusArr[i].trim();
                        validateCertificationStatus(certStatusParam, availableCertificationStatuses);
                        certificationStatuses.add(certStatusParam);
                    }
                    searchRequest.setCertificationStatuses(certificationStatuses);
                }
            }
        }

        if (certificationEditionsDelimited != null) {
            String certificationEditionsDelimitedTrimmed = certificationEditionsDelimited.trim();
            if (!StringUtils.isEmpty(certificationEditionsDelimitedTrimmed)) {
                String[] certificationEditionsArr = certificationEditionsDelimitedTrimmed.split(",");
                if (certificationEditionsArr.length > 0) {
                    Set<String> certificationEditions = new HashSet<String>();
                    Set<KeyValueModel> availableCertificationEditions = dimensionalDataManager.getEditionNames(false);

                    for (int i = 0; i < certificationEditionsArr.length; i++) {
                        String certEditionParam = certificationEditionsArr[i].trim();
                        validateCertificationEdition(certEditionParam, availableCertificationEditions);
                        certificationEditions.add(certEditionParam);
                    }

                    searchRequest.setCertificationEditions(certificationEditions);
                }
            }
        }

        if (certificationCriteriaDelimited != null) {
            String certificationCriteriaDelimitedTrimmed = certificationCriteriaDelimited.trim();
            if (!StringUtils.isEmpty(certificationCriteriaDelimitedTrimmed)) {
                String[] certificationCriteriaArr = certificationCriteriaDelimitedTrimmed.split(",");
                if (certificationCriteriaArr.length > 0) {
                    Set<String> certificationCriterion = new HashSet<String>();
                    Set<CriteriaSpecificDescriptiveModel> availableCriterion = dimensionalDataManager
                            .getCertificationCriterionNumbers();

                    for (int i = 0; i < certificationCriteriaArr.length; i++) {
                        String certCriteriaParam = certificationCriteriaArr[i].trim();
                        validateCertificationCriteria(certCriteriaParam, availableCriterion);
                        certificationCriterion.add(certCriteriaParam);
                    }
                    searchRequest.setCertificationCriteria(certificationCriterion);
                    if (!StringUtils.isEmpty(certificationCriteriaOperatorStr)) {
                        String certificationCriteriaOperatorStrTrimmed = certificationCriteriaOperatorStr.trim();
                        SearchSetOperator certificationCriteriaOperator =
                                validateSearchSetOperator(certificationCriteriaOperatorStrTrimmed);
                        searchRequest.setCertificationCriteriaOperator(certificationCriteriaOperator);
                    }
                }
            }
        }

        if (cqmsDelimited != null) {
            String cqmsDelimitedTrimmed = cqmsDelimited.trim();
            if (!StringUtils.isEmpty(cqmsDelimitedTrimmed)) {
                String[] cqmsArr = cqmsDelimitedTrimmed.split(",");
                if (cqmsArr.length > 0) {
                    Set<String> cqms = new HashSet<String>();
                    Set<DescriptiveModel> availableCqms = dimensionalDataManager.getCQMCriterionNumbers(false);

                    for (int i = 0; i < cqmsArr.length; i++) {
                        String cqmParam = cqmsArr[i].trim();
                        validateCqm(cqmParam, availableCqms);
                        cqms.add(cqmParam.trim());
                    }
                    searchRequest.setCqms(cqms);

                    if (!StringUtils.isEmpty(cqmsOperatorStr)) {
                        String cqmsOperatorStrTrimmed = cqmsOperatorStr.trim();
                        SearchSetOperator cqmOperator = validateSearchSetOperator(cqmsOperatorStrTrimmed);
                        searchRequest.setCqmsOperator(cqmOperator);
                    }
                }
            }
        }

        if (certificationBodiesDelimited != null) {
            String certificationBodiesDelimitedTrimmed = certificationBodiesDelimited.trim();
            if (!StringUtils.isEmpty(certificationBodiesDelimitedTrimmed)) {
                String[] certificationBodiesArr = certificationBodiesDelimitedTrimmed.split(",");
                if (certificationBodiesArr.length > 0) {
                    Set<String> certBodies = new HashSet<String>();
                    Set<CertificationBody> availableCertBodies = dimensionalDataManager.getCertBodyNames();

                    for (int i = 0; i < certificationBodiesArr.length; i++) {
                        String certBodyParam = certificationBodiesArr[i].trim();
                        validateCertificationBody(certBodyParam, availableCertBodies);
                        certBodies.add(certBodyParam);
                    }
                    searchRequest.setCertificationBodies(certBodies);
                }
            }
        }

        if (!StringUtils.isEmpty(hasHadSurveillanceStr)) {
            if (!hasHadSurveillanceStr.equalsIgnoreCase(Boolean.TRUE.toString())
                    && !hasHadSurveillanceStr.equalsIgnoreCase(Boolean.FALSE.toString())) {
                String err = String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("search.hasHadSurveillance.invalid"),
                        LocaleContextHolder.getLocale()),
                        hasHadSurveillanceStr);
                throw new InvalidArgumentsException(err);
            }
            Boolean hasHadSurveillance = Boolean.parseBoolean(hasHadSurveillanceStr);
            searchRequest.getSurveillance().setHasHadSurveillance(hasHadSurveillance);
        }

        if (!StringUtils.isEmpty(nonconformityOptionsDelimited)) {
            String nonconformityOptionsDelimitedTrimmed = nonconformityOptionsDelimited.trim();
            String[] nonconformityOptionsArr = nonconformityOptionsDelimitedTrimmed.split(",");
            if (nonconformityOptionsArr.length > 0) {
                Set<NonConformitySearchOptions> nonconformitySearchOptions = new HashSet<NonConformitySearchOptions>();
                for (int i = 0; i < nonconformityOptionsArr.length; i++) {
                    String nonconformityOptionParam = nonconformityOptionsArr[i].trim();
                    try {
                        NonConformitySearchOptions ncOpt = NonConformitySearchOptions.valueOf(nonconformityOptionParam);
                        if (ncOpt != null) {
                            nonconformitySearchOptions.add(ncOpt);
                        } else {
                            String err = String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("search.nonconformitySearchOption.invalid"),
                                    LocaleContextHolder.getLocale()),
                                    nonconformityOptionParam, NonConformitySearchOptions.CLOSED_NONCONFORMITY.name()
                                    + ", " + NonConformitySearchOptions.NEVER_NONCONFORMITY.name()
                                    + ", or " + NonConformitySearchOptions.OPEN_NONCONFORMITY.name());
                            throw new InvalidArgumentsException(err);
                        }
                    } catch (Exception ex) {
                        String err = String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("search.nonconformitySearchOption.invalid"),
                                LocaleContextHolder.getLocale()),
                                nonconformityOptionParam, NonConformitySearchOptions.CLOSED_NONCONFORMITY.name()
                                + ", " + NonConformitySearchOptions.NEVER_NONCONFORMITY.name()
                                + ", or " + NonConformitySearchOptions.OPEN_NONCONFORMITY.name());
                        throw new InvalidArgumentsException(err);
                    }
                }
                searchRequest.getSurveillance().setNonconformityOptions(nonconformitySearchOptions);

                if (!StringUtils.isEmpty(nonconformityOptionsOperator)) {
                    String nonconformityOptionsOperatorTrimmed = nonconformityOptionsOperator.trim();
                    SearchSetOperator ncOperator = validateSearchSetOperator(nonconformityOptionsOperatorTrimmed);
                    searchRequest.getSurveillance().setNonconformityOptionsOperator(ncOperator);
                }
            }
        }

        if (developer != null) {
            String developerTrimmed = developer.trim();
            if (!StringUtils.isEmpty(developerTrimmed)) {
                searchRequest.setDeveloper(developerTrimmed);
            }
        }

        if (product != null) {
            String productTrimmed = product.trim();
            if (!StringUtils.isEmpty(productTrimmed)) {
                searchRequest.setProduct(productTrimmed);
            }
        }

        if (version != null) {
            String versionTrimmed = version.trim();
            if (!StringUtils.isEmpty(versionTrimmed)) {
                searchRequest.setVersion(versionTrimmed);
            }
        }

        if (practiceType != null) {
            String practiceTypeTrimmed = practiceType.trim();
            if (!StringUtils.isEmpty(practiceTypeTrimmed)) {
                validatePracticeTypeParameter(practiceTypeTrimmed);
                searchRequest.setPracticeType(practiceTypeTrimmed);
            }
        }

        if (!StringUtils.isEmpty(certificationDateStart)) {
            String certificationDateStartTrimmed = certificationDateStart.trim();
            if (!StringUtils.isEmpty(certificationDateStartTrimmed)) {
                validateCertificationDateParameter(certificationDateStartTrimmed);
                searchRequest.setCertificationDateStart(certificationDateStartTrimmed);
            }
        }

        if (!StringUtils.isEmpty(certificationDateEnd)) {
            String certificationDateEndTrimmed = certificationDateEnd.trim();
            if (!StringUtils.isEmpty(certificationDateEndTrimmed)) {
                validateCertificationDateParameter(certificationDateEndTrimmed);
                searchRequest.setCertificationDateEnd(certificationDateEndTrimmed);
            }
        }
        validatePageSize(pageSize);
        String orderByTrimmed = orderBy.trim();
        validateOrderBy(orderByTrimmed);

        searchRequest.setPageNumber(pageNumber);
        searchRequest.setPageSize(pageSize);
        searchRequest.setOrderBy(orderByTrimmed);
        searchRequest.setSortDescending(sortDescending);

        //trim everything
        searchRequest.cleanAllParameters();
        return certifiedProductSearchManager.search(searchRequest);

    }

    @Deprecated
    @Operation(summary = "Search the CHPL with an HTTP POST Request.",
            description = "Search the CHPL by specifycing multiple fields of the data to search. "
                    + "If paging fields are not specified, the first 20 records are returned by default.",
             deprecated = true)
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody SearchResponseLegacy searchPostLegacy(@RequestBody SearchRequestLegacy searchRequest)
            throws InvalidArgumentsException, EntityRetrievalException {
        //trim everything
        searchRequest.cleanAllParameters();

        if (searchRequest.getCertificationStatuses() != null && searchRequest.getCertificationStatuses().size() > 0) {
            Set<KeyValueModel> availableCertificationStatuses = dimensionalDataManager.getCertificationStatuses();
            for (String certStatusName : searchRequest.getCertificationStatuses()) {
                validateCertificationStatus(certStatusName, availableCertificationStatuses);
            }
        }

        if (searchRequest.getCertificationEditions() != null && searchRequest.getCertificationEditions().size() > 0) {
            Set<KeyValueModel> availableCertificationEditions = dimensionalDataManager.getEditionNames(false);
            for (String certEditionName : searchRequest.getCertificationEditions()) {
                validateCertificationEdition(certEditionName, availableCertificationEditions);
            }
        }

        if (searchRequest.getCertificationCriteria() != null && searchRequest.getCertificationCriteria().size() > 0) {
            Set<CriteriaSpecificDescriptiveModel> availableCriterion = dimensionalDataManager
                    .getCertificationCriterionNumbers();
            for (String criteria : searchRequest.getCertificationCriteria()) {
                validateCertificationCriteria(criteria, availableCriterion);
            }
        }

        if (searchRequest.getCqms() != null && searchRequest.getCqms().size() > 0) {
            Set<DescriptiveModel> availableCqms = dimensionalDataManager.getCQMCriterionNumbers(false);
            for (String cqm : searchRequest.getCqms()) {
                validateCqm(cqm, availableCqms);
            }
        }

        if (searchRequest.getCertificationBodies() != null && searchRequest.getCertificationBodies().size() > 0) {
            Set<CertificationBody> availableCertBodies = dimensionalDataManager.getCertBodyNames();
            for (String certBody : searchRequest.getCertificationBodies()) {
                validateCertificationBody(certBody, availableCertBodies);
            }
        }

        validatePracticeTypeParameter(searchRequest.getPracticeType());
        validateCertificationDateParameter(searchRequest.getCertificationDateStart());
        validateCertificationDateParameter(searchRequest.getCertificationDateEnd());
        validatePageSize(searchRequest.getPageSize());
        validateOrderBy(searchRequest.getOrderBy());
        return certifiedProductSearchManager.search(searchRequest);
    }

    private void validateCertificationBody(String certBodyParam,
            Set<CertificationBody> availableCertBodies) throws InvalidArgumentsException {
        boolean found = false;
        for (CertificationBody currAvailableCertBody : availableCertBodies) {
            if (currAvailableCertBody.getName().equalsIgnoreCase(certBodyParam)) {
                found = true;
            }
        }
        if (!found) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.certificationBodies.invalid"),
                            LocaleContextHolder.getLocale()),
                    certBodyParam);
            LOGGER.error(err);
            throw new InvalidArgumentsException(err);
        }
    }

    private void validateCqm(String cqmParam,
            Set<DescriptiveModel> availableCqms) throws InvalidArgumentsException {
        boolean found = false;
        for (DescriptiveModel currAvailableCqm : availableCqms) {
            if (currAvailableCqm.getName().equalsIgnoreCase(cqmParam)) {
                found = true;
            }
        }
        if (!found) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.cqms.invalid"),
                            LocaleContextHolder.getLocale()),
                    cqmParam);
            LOGGER.error(err);
            throw new InvalidArgumentsException(err);
        }
    }

    private void validateCertificationCriteria(String certCriteriaParam,
            Set<? extends DescriptiveModel> availableCriterion) throws InvalidArgumentsException {
        boolean found = false;
        for (DescriptiveModel currAvailableCriteria : availableCriterion) {
            if (currAvailableCriteria.getName().equalsIgnoreCase(certCriteriaParam)) {
                found = true;
            }
        }
        if (!found) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.certificationCriteria.invalid"),
                            LocaleContextHolder.getLocale()),
                    certCriteriaParam);
            LOGGER.error(err);
            throw new InvalidArgumentsException(err);
        }
    }

    private void validateCertificationEdition(String certEditionParam,
            Set<KeyValueModel> availableCertificationEditions) throws InvalidArgumentsException {
        boolean found = false;
        for (KeyValueModel currAvailableEdition : availableCertificationEditions) {
            if (currAvailableEdition.getName().equalsIgnoreCase(certEditionParam)) {
                found = true;
            }
        }
        if (!found) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.certificationEdition.invalid"),
                            LocaleContextHolder.getLocale()),
                    certEditionParam);
            LOGGER.error(err);
            throw new InvalidArgumentsException(err);
        }
    }

    private void validateCertificationStatus(String certStatusParam,
            Set<KeyValueModel> availableCertificationStatuses) throws InvalidArgumentsException {
        boolean found = false;
        for (KeyValueModel currAvailableCertStatus : availableCertificationStatuses) {
            if (currAvailableCertStatus.getName().equalsIgnoreCase(certStatusParam)) {
                found = true;
            }
        }
        if (!found) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.certificationStatuses.invalid"),
                            LocaleContextHolder.getLocale()),
                    certStatusParam);
            LOGGER.error(err);
            throw new InvalidArgumentsException(err);
        }
    }

    private SearchSetOperator validateSearchSetOperator(String searchSetOperator)
            throws InvalidArgumentsException {
        SearchSetOperator result = null;
        try {
            SearchSetOperator operatorEnum = SearchSetOperator.valueOf(searchSetOperator);
            if (operatorEnum != null) {
                result = operatorEnum;
            } else {
                String err = String.format(
                        messageSource.getMessage(new DefaultMessageSourceResolvable("search.searchOperator.invalid"),
                                LocaleContextHolder.getLocale()),
                        searchSetOperator, SearchSetOperator.OR + " or " + SearchSetOperator.AND);
                throw new InvalidArgumentsException(err);
            }
        } catch (Exception ex) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.searchOperator.invalid"),
                            LocaleContextHolder.getLocale()),
                    searchSetOperator, SearchSetOperator.OR + " or " + SearchSetOperator.AND);
            throw new InvalidArgumentsException(err);
        }
        return result;
    }

    private void validatePracticeTypeParameter(String practiceType) throws InvalidArgumentsException {
        if (!StringUtils.isEmpty(practiceType)) {
            Set<KeyValueModel> availablePracticeTypes = dimensionalDataManager.getPracticeTypeNames();
            boolean found = false;
            for (KeyValueModel currAvailablePracticeType : availablePracticeTypes) {
                if (currAvailablePracticeType.getName().equalsIgnoreCase(practiceType)) {
                    found = true;
                }
            }

            if (!found) {
                String err = String.format(
                        messageSource.getMessage(new DefaultMessageSourceResolvable("search.practiceType.invalid"),
                                LocaleContextHolder.getLocale()),
                        practiceType);
                LOGGER.error(err);
                throw new InvalidArgumentsException(err);
            }
        }
    }

    private void validateCertificationDateParameter(String dateStr) throws InvalidArgumentsException {
        SimpleDateFormat format = new SimpleDateFormat(SearchRequestLegacy.CERTIFICATION_DATE_SEARCH_FORMAT);
        if (dateStr != null) {
            if (!StringUtils.isEmpty(dateStr)) {
                try {
                    format.parse(dateStr);
                } catch (ParseException ex) {
                    String err = String.format(
                            messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("search.certificationDate.invalid"),
                                    LocaleContextHolder.getLocale()),
                            dateStr, SearchRequestLegacy.CERTIFICATION_DATE_SEARCH_FORMAT);
                    LOGGER.error(err);
                    throw new InvalidArgumentsException(err);
                }
            }
        }
    }

    private void validatePageSize(Integer pageSize) throws InvalidArgumentsException {
        if (pageSize > MAX_PAGE_SIZE) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.pageSize.invalid"),
                            LocaleContextHolder.getLocale()),
                    SearchRequestLegacy.MAX_PAGE_SIZE);
            throw new InvalidArgumentsException(err);
        }
    }

    private void validateOrderBy(String orderBy) throws InvalidArgumentsException {
        if (!orderBy.equalsIgnoreCase(SearchRequestLegacy.ORDER_BY_CERTIFICATION_BODY)
                && !orderBy.equalsIgnoreCase(SearchRequestLegacy.ORDER_BY_CERTIFICATION_EDITION)
                && !orderBy.equalsIgnoreCase(SearchRequestLegacy.ORDER_BY_DEVELOPER)
                && !orderBy.equalsIgnoreCase(SearchRequestLegacy.ORDER_BY_PRODUCT)
                && !orderBy.equalsIgnoreCase(SearchRequestLegacy.ORDER_BY_VERSION)) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.orderBy.invalid"),
                            LocaleContextHolder.getLocale()),
                    orderBy, SearchRequestLegacy.ORDER_BY_CERTIFICATION_BODY + ", "
                            + SearchRequestLegacy.ORDER_BY_CERTIFICATION_EDITION + ", "
                            + SearchRequestLegacy.ORDER_BY_DEVELOPER + ", "
                            + SearchRequestLegacy.ORDER_BY_PRODUCT + ", or "
                            + SearchRequestLegacy.ORDER_BY_VERSION);
            throw new InvalidArgumentsException(err);
        }
    }
}
