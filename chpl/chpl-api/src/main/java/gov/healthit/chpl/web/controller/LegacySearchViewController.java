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
import gov.healthit.chpl.domain.search.NonconformitySearchOptions;
import gov.healthit.chpl.domain.search.SearchRequestLegacy;
import gov.healthit.chpl.domain.search.SearchResponseLegacy;
import gov.healthit.chpl.domain.search.SearchSetOperator;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.DimensionalDataManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Api
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

    /**
     * Search listings on the CHPL with a generic search term and/or filters.
     * @param searchTerm CHPL ID, Developer (or previous developer) Name, Product Name, ONC-ACB Certification ID
     * @param certificationStatusesDelimited statuses to filter by
     * @param certificationEditionsDelimited editions to filter by
     * @param certificationCriteriaDelimited criteria to filter by
     * @param certificationCriteriaOperatorStr and vs or for criteria filter
     * @param cqmsDelimited cqms to filter by
     * @param cqmsOperatorStr and vs or for cqm filter
     * @param certificationBodiesDelimited acbs to filter by
     * @param hasHadSurveillanceStr filter by whether listings have had surveillance
     * @param nonconformityOptionsDelimited filter by whether listings have open/closed/no nonconformities
     * @param nonconformityOptionsOperator and vs or for nonconformity filter
     * @param developer filter by developer name
     * @param product filter by product name
     * @param version filter by version name
     * @param practiceType filter by practice type name
     * @param certificationDateStart filter by when listing was certified to the CHPL
     * @param certificationDateEnd filter by when listing was certified to the CHPL
     * @param pageNumber which page of data to return
     * @param pageSize how many records to return per page
     * @param orderBy field to order data by
     * @param sortDescending sort order
     * @return listings matching the given parameters
     * @throws InvalidArgumentsException if one or more parameters is not specified properly or has an invalid value
     * @throws EntityRetrievalException if there was an error retrieving a listing
     */
    @Deprecated
    @SuppressWarnings({"checkstyle:methodlength", "checkstyle:parameternumber"})
    @ApiOperation(value = "Search the CHPL",
    notes = "DEPRECATED. If paging parameters are not specified, the first 20 records are returned by default. "
            + "All parameters are optional. "
            + "Any parameter that can accept multiple things (i.e. certificationStatuses) expects "
            + "a comma-delimited list of those things (i.e. certificationStatuses = Active,Suspended). "
            + "Date parameters are required to be in the format "
            + SearchRequestLegacy.CERTIFICATION_DATE_SEARCH_FORMAT + ". ")
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
        @ApiImplicitParam(name = "certificationCriteria",
        value = "A comma-separated list of certification criteria to be queried together "
                + "(ex: \"170.314 (a)(1),170.314 (a)(2)\" finds listings "
                + "attesting to either 170.314 (a)(1) or 170.314 (a(2)).",
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
        @ApiImplicitParam(name = "hasHadSurveillance",
        value = "True or False if a listing has ever had surveillance.", required = false,
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
                + SearchRequestLegacy.CERTIFICATION_DATE_SEARCH_FORMAT,
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "certificationDateEnd",
        value = "To return only listings certified before this date. Required format is "
                + SearchRequestLegacy.CERTIFICATION_DATE_SEARCH_FORMAT,
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "pageNumber",
        value = "Zero-based page number used in concert with pageSize. Defaults to 0.", required = false,
        dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "pageSize",
        value = "Number of results to return used in concert with pageNumber. "
                + "Defaults to 20. Maximum allowed page size is 100.",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "orderBy",
        value = "What to order by. Options are one of the following: "
                + SearchRequestLegacy.ORDER_BY_DEVELOPER + ", " + SearchRequestLegacy.ORDER_BY_PRODUCT + ", "
                + SearchRequestLegacy.ORDER_BY_VERSION + ", " + SearchRequestLegacy.ORDER_BY_CERTIFICATION_EDITION + ", "
                + ", or " + SearchRequestLegacy.ORDER_BY_CERTIFICATION_BODY + ", "
                + ". Defaults to " + SearchRequestLegacy.ORDER_BY_PRODUCT + ".",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "sortDescending",
        value = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
        required = false, dataType = "boolean", paramType = "query")
    })
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody SearchResponseLegacy searchGet(
            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
            @RequestParam(value = "certificationStatuses", required = false,
            defaultValue = "") String certificationStatusesDelimited,
            @RequestParam(value = "certificationEditions", required = false,
            defaultValue = "") String certificationEditionsDelimited,
            @RequestParam(value = "certificationCriteria", required = false,
            defaultValue = "") String certificationCriteriaDelimited,
            @RequestParam(value = "certificationCriteriaOperator", required = false,
            defaultValue = "OR") String certificationCriteriaOperatorStr,
            @RequestParam(value = "cqms", required = false, defaultValue = "") String cqmsDelimited,
            @RequestParam(value = "cqmsOperator", required = false,
            defaultValue = "OR") String cqmsOperatorStr,
            @RequestParam(value = "certificationBodies", required = false,
            defaultValue = "") String certificationBodiesDelimited,
            @RequestParam(value = "hasHadSurveillance", required = false,
            defaultValue = "") String hasHadSurveillanceStr,
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
                Set<NonconformitySearchOptions> nonconformitySearchOptions = new HashSet<NonconformitySearchOptions>();
                for (int i = 0; i < nonconformityOptionsArr.length; i++) {
                    String nonconformityOptionParam = nonconformityOptionsArr[i].trim();
                    try {
                        NonconformitySearchOptions ncOpt = NonconformitySearchOptions.valueOf(nonconformityOptionParam);
                        if (ncOpt != null) {
                            nonconformitySearchOptions.add(ncOpt);
                        } else {
                            String err = String.format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("search.nonconformitySearchOption.invalid"),
                                    LocaleContextHolder.getLocale()),
                                    nonconformityOptionParam, NonconformitySearchOptions.CLOSED_NONCONFORMITY.name()
                                    + ", " + NonconformitySearchOptions.NEVER_NONCONFORMITY.name()
                                    + ", or " + NonconformitySearchOptions.OPEN_NONCONFORMITY.name());
                            throw new InvalidArgumentsException(err);
                        }
                    } catch (Exception ex) {
                        String err = String.format(messageSource.getMessage(
                                new DefaultMessageSourceResolvable("search.nonconformitySearchOption.invalid"),
                                LocaleContextHolder.getLocale()),
                                nonconformityOptionParam, NonconformitySearchOptions.CLOSED_NONCONFORMITY.name()
                                + ", " + NonconformitySearchOptions.NEVER_NONCONFORMITY.name()
                                + ", or " + NonconformitySearchOptions.OPEN_NONCONFORMITY.name());
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

    /**
     * DEPRECATED. Search listings in the CHPL based on a set of filters.
     * @param searchRequest object containing all possible search parameters; not all are required.
     * @return listings matching the passed in search parameters
     * @throws InvalidArgumentsException if a search parameter has an invalid value
     * @throws EntityRetrievalException if there is an error retrieving a listing
     */
    @Deprecated
    @ApiOperation(value = "Search the CHPL with an HTTP POST Request.",
            notes = "Search the CHPL by specifycing multiple fields of the data to search. "
                    + "If paging fields are not specified, the first 20 records are returned by default.")
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
