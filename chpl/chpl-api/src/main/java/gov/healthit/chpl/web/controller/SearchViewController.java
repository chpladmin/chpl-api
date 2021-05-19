package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.DimensionalData;
import gov.healthit.chpl.domain.FuzzyChoices;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.domain.SearchOption;
import gov.healthit.chpl.domain.SearchableDimensionalData;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.domain.UploadTemplateVersion;
import gov.healthit.chpl.domain.search.NonconformitySearchOptions;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.domain.search.SearchSetOperator;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementOptions;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementOptionsDeprecated;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.ComplaintManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.manager.FilterManager;
import gov.healthit.chpl.manager.FuzzyChoicesManager;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.svap.manager.SvapManager;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import gov.healthit.chpl.web.controller.results.CertificationCriterionResults;
import gov.healthit.chpl.web.controller.results.DecertifiedDeveloperResults;
import gov.healthit.chpl.web.controller.results.SvapResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Api
@RestController
@Loggable
@Log4j2
public class SearchViewController {
    private static final int MAX_PAGE_SIZE = 100;

    private Environment env;
    private MessageSource messageSource;
    private DimensionalDataManager dimensionalDataManager;
    private FuzzyChoicesManager fuzzyChoicesManager;
    private CertifiedProductSearchManager certifiedProductSearchManager;
    private DeveloperManager developerManager;
    private FilterManager filterManager;
    private ComplaintManager complaintManager;
    private SurveillanceReportManager survReportManager;
    private ChangeRequestManager changeRequestManager;
    private FF4j ff4j;
    private FileUtils fileUtils;
    private SvapManager svapManager;

    @Autowired
    public SearchViewController(Environment env,
            MessageSource messageSource,
            DimensionalDataManager dimensionalDataManager,
            FuzzyChoicesManager fuzzyChoicesManager,
            CertifiedProductSearchManager certifiedProductSearchManager,
            @Lazy DeveloperManager developerManager,
            FilterManager filterManager,
            ComplaintManager complaintManager,
            SurveillanceReportManager survReportManager,
            ChangeRequestManager changeRequestManager,
            FF4j ff4j,
            FileUtils fileUtils,
            SvapManager svapManager) {
        this.env = env;
        this.messageSource = messageSource;
        this.dimensionalDataManager = dimensionalDataManager;
        this.fuzzyChoicesManager = fuzzyChoicesManager;
        this.certifiedProductSearchManager = certifiedProductSearchManager;
        this.developerManager = developerManager;
        this.filterManager = filterManager;
        this.complaintManager = complaintManager;
        this.survReportManager = survReportManager;
        this.changeRequestManager = changeRequestManager;
        this.ff4j = ff4j;
        this.fileUtils = fileUtils;
        this.svapManager = svapManager;
    }

    /**
     * Streams a file back to the end user for the specified edition (2011,2014,2015,etc)
     * in the specified format (xml or csv). Optionally will send back the definition
     * files instead. The file that is sent back is generated via a quartz job on a
     * regular basis.
     * @param editionInput 2011, 2014, or 2015
     * @param formatInput csv or xml
     * @param isDefinition whether to send back the data file or definition file
     * @param request http request
     * @param response http response, used to stream back the file
     * @throws IOException if the file cannot be read
     */
    @ApiOperation(value = "Download the entire CHPL as XML.",
            notes = "Once per day, the entire certified product listing is "
                    + "written out to XML files on the CHPL servers, one for each "
                    + "certification edition. This method allows any user to download "
                    + "that XML file. It is formatted in such a way that users may import "
                    + "it into Microsoft Excel or any other XML tool of their choosing. To download "
                    + "any one of the XML files, append ‘&edition=year’ to the end of the query string "
                    + "(e.g., &edition=2015). A separate query is required to download each of the XML files.")
    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/xml")
    public void download(@RequestParam(value = "edition", required = false) String editionInput,
            @RequestParam(value = "format", defaultValue = "xml", required = false) String formatInput,
            @RequestParam(value = "definition", defaultValue = "false", required = false) Boolean isDefinition,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        //parse inputs
        String edition = editionInput;
        String format = formatInput;
        String responseType = "text/csv";
        String filenameToStream = null;

        if (!StringUtils.isEmpty(edition)) {
            // make sure it's a 4 character year
            edition = edition.trim();
            if (!edition.startsWith("20")) {
                edition = "20" + edition;
            }
        } else {
            edition = "all";
        }

        if (!StringUtils.isEmpty(format) && format.equalsIgnoreCase("csv")) {
            format = "csv";
        } else {
            format = "xml";
            responseType = "application/xml";
        }

        File toDownload = null;
        //if the user wants a definition file, find it
        if (isDefinition != null && isDefinition.booleanValue()) {
            if (format.equals("xml")) {
                toDownload = fileUtils.getDownloadFile(env.getProperty("schemaXmlName"));
            } else if (edition.equals("2014")) {
                toDownload = fileUtils.getDownloadFile(env.getProperty("schemaCsv2014Name"));
            } else if (edition.equals("2015")) {
                if (ff4j.check(FeatureList.RWT_ENABLED)) {
                    toDownload = fileUtils.getDownloadFile(env.getProperty("schemaCsv2015NameWithRWT"));
                } else {
                    toDownload = fileUtils.getDownloadFile(env.getProperty("schemaCsv2015Name"));
                }
                filenameToStream = env.getProperty("schemaCsv2015Name");
            }

            if (!toDownload.exists()) {
                response.getWriter()
                .write(String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable("resources.schemaFileNotFound"),
                        LocaleContextHolder.getLocale()), toDownload.getAbsolutePath()));
                return;
            }
        } else {
            File newestFileWithFormat = fileUtils.getNewestFileMatchingName("^chpl-" + edition + "-.+\\." + format + "$");
            if (newestFileWithFormat != null) {
                toDownload = newestFileWithFormat;
            } else {
                response.getWriter()
                .write(String.format(messageSource.getMessage(
                        new DefaultMessageSourceResolvable(
                                "resources.fileWithEditionAndFormatNotFound"),
                        LocaleContextHolder.getLocale()), edition, format));
                return;
            }
        }

        LOGGER.info("Downloading " + toDownload.getName());
        if (filenameToStream != null) {
            fileUtils.streamFileAsResponse(toDownload, responseType, response, filenameToStream);
        } else {
            fileUtils.streamFileAsResponse(toDownload, responseType, response);
        }
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
        value = "What to order by. Options are one of the following: "
                + SearchRequest.ORDER_BY_DEVELOPER + ", " + SearchRequest.ORDER_BY_PRODUCT + ", "
                + SearchRequest.ORDER_BY_VERSION + ", " + SearchRequest.ORDER_BY_CERTIFICATION_EDITION + ", "
                + ", or " + SearchRequest.ORDER_BY_CERTIFICATION_BODY + ", "
                + ". Defaults to " + SearchRequest.ORDER_BY_PRODUCT + ".",
                required = false, dataType = "string", paramType = "query"),
        @ApiImplicitParam(name = "sortDescending",
        value = "Use to specify the direction of the sort. Defaults to false (ascending sort).",
        required = false, dataType = "boolean", paramType = "query")
    })
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = {
            "application/json; charset=utf-8", "application/xml"
    })
    public @ResponseBody SearchResponse searchGet(
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

        SearchRequest searchRequest = new SearchRequest();
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
     * Search listings in the CHPL based on a set of filters.
     * @param searchRequest object containing all possible search parameters; not all are required.
     * @return listings matching the passed in search parameters
     * @throws InvalidArgumentsException if a search parameter has an invalid value
     * @throws EntityRetrievalException if there is an error retrieving a listing
     */
    @ApiOperation(value = "Search the CHPL with an HTTP POST Request.",
            notes = "Search the CHPL by specifycing multiple fields of the data to search. "
                    + "If paging fields are not specified, the first 20 records are returned by default.")
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody SearchResponse searchPost(@RequestBody SearchRequest searchRequest)
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
        SimpleDateFormat format = new SimpleDateFormat(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
        if (dateStr != null) {
            if (!StringUtils.isEmpty(dateStr)) {
                try {
                    format.parse(dateStr);
                } catch (ParseException ex) {
                    String err = String.format(
                            messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("search.certificationDate.invalid"),
                                    LocaleContextHolder.getLocale()),
                            dateStr, SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
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
                    SearchRequest.MAX_PAGE_SIZE);
            throw new InvalidArgumentsException(err);
        }
    }

    private void validateOrderBy(String orderBy) throws InvalidArgumentsException {
        if (!orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_CERTIFICATION_BODY)
                && !orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_CERTIFICATION_EDITION)
                && !orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_DEVELOPER)
                && !orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_PRODUCT)
                && !orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_VERSION)) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.orderBy.invalid"),
                            LocaleContextHolder.getLocale()),
                    orderBy, SearchRequest.ORDER_BY_CERTIFICATION_BODY + ", "
                            + SearchRequest.ORDER_BY_CERTIFICATION_EDITION + ", "
                            + SearchRequest.ORDER_BY_DEVELOPER + ", "
                            + SearchRequest.ORDER_BY_PRODUCT + ", or "
                            + SearchRequest.ORDER_BY_VERSION);
            throw new InvalidArgumentsException(err);
        }
    }

    @ApiOperation(value = "Get all fuzzy matching choices for the items that be fuzzy matched.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ONC")
    @RequestMapping(value = "/data/fuzzy_choices", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<FuzzyChoices> getFuzzyChoices()
            throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException {
        return fuzzyChoicesManager.getFuzzyChoices();
    }

    @ApiOperation(value = "Change existing fuzzy matching choices.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ONC")
    @RequestMapping(value = "/data/fuzzy_choices/{fuzzyChoiceId}", method = RequestMethod.PUT,
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public FuzzyChoices updateFuzzyChoicesForSearching(@RequestBody FuzzyChoices fuzzyChoices)
            throws InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException, IOException {

        return updateFuzzyChoices(fuzzyChoices);
    }

    private FuzzyChoices updateFuzzyChoices(FuzzyChoices fuzzyChoices)
            throws InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException, IOException {

        FuzzyChoicesDTO toUpdate = new FuzzyChoicesDTO();
        toUpdate.setId(fuzzyChoices.getId());
        toUpdate.setFuzzyType(FuzzyType.getValue(fuzzyChoices.getFuzzyType()));
        toUpdate.setChoices(fuzzyChoices.getChoices());

        FuzzyChoices result = fuzzyChoicesManager.updateFuzzyChoices(toUpdate);
        return result;
        //return new FuzzyChoices(result);
    }

    @ApiOperation(value = "Get a list of quarters for which a surveillance report can be created.")
    @RequestMapping(value = "/data/quarters", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getQuarters() {
        return dimensionalDataManager.getQuarters();
    }

    @ApiOperation(value = "Get a list of surveillance process types.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB.")
    @RequestMapping(value = "/data/surveillance-process-types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getSurveillanceProcessTypes() {
        return survReportManager.getSurveillanceProcessTypes();
    }

    @ApiOperation(value = "Get a list of surveillance outcomes.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB.")
    @RequestMapping(value = "/data/surveillance-outcomes", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getSurveillanceOutcomes() {
        return survReportManager.getSurveillanceOutcomes();
    }

    @ApiOperation(value = "Get all possible classifications in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/classification_types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getClassificationNames() {
        return dimensionalDataManager.getClassificationNames();
    }

    @ApiOperation(value = "Get all possible certificaiton editions in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/certification_editions", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getEditionNames() {
        return dimensionalDataManager.getEditionNames(false);
    }

    @ApiOperation(value = "Get all possible certification statuses in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/certification_statuses", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getCertificationStatuses() {
        return dimensionalDataManager.getCertificationStatuses();
    }

    @ApiOperation(value = "Get all possible practice types in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/practice_types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModel> getPracticeTypeNames() {
        return dimensionalDataManager.getPracticeTypeNames();
    }

    @ApiOperation(value = "Get all possible product names in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/products", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModelStatuses> getProductNames() {
        return dimensionalDataManager.getProducts();
    }

    @ApiOperation(value = "Get all possible developer names in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/developers", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<KeyValueModelStatuses> getDeveloperNames() {
        return dimensionalDataManager.getDevelopers();
    }

    @ApiOperation(value = "Get all possible ACBs in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/certification_bodies", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody Set<CertificationBody> getCertBodyNames() {
        return dimensionalDataManager.getCertBodyNames();
    }

    @ApiOperation(value = "Get all possible education types in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/education_types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getEducationTypes() {
        Set<KeyValueModel> data = dimensionalDataManager.getEducationTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test participant age ranges in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/age_ranges", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getAgeRanges() {
        Set<KeyValueModel> data = dimensionalDataManager.getAgeRanges();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test functionality options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/test_functionality", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTestFunctionality() {
        Set<TestFunctionality> data = dimensionalDataManager.getTestFunctionality();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test tool options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/test_tools", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTestTools() {
        Set<KeyValueModel> data = dimensionalDataManager.getTestTools();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test procedure options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/test_procedures", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTestProcedures() {
        Set<CriteriaSpecificDescriptiveModel> data = dimensionalDataManager.getTestProcedures();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test data options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/test_data", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTestData() {
        Set<CriteriaSpecificDescriptiveModel> data = dimensionalDataManager.getTestData();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test standard options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/test_standards", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTestStandards() {
        Set<TestStandard> data = dimensionalDataManager.getTestStandards();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible qms standard options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/qms_standards", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getQmsStandards() {
        Set<KeyValueModel> data = dimensionalDataManager.getQmsStandards();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible targeted user options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/targeted_users", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getTargetedUsers() {
        Set<KeyValueModel> data = dimensionalDataManager.getTargetedUesrs();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible UCD process options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/ucd_processes", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getUcdProcesses() {
        Set<KeyValueModel> data = dimensionalDataManager.getUcdProcesses();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible accessibility standard options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/accessibility_standards", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getAccessibilityStandards() {
        Set<KeyValueModel> data = dimensionalDataManager.getAccessibilityStandards();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible measure options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/measures", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getMeasures() {
        Set<Measure> data = dimensionalDataManager.getMeasures();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible types of measures in the CHPL, currently this is G1 and G2.",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/measure-types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getMeasureTypes() {
        Set<MeasureType> data = dimensionalDataManager.getMeasureTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible developer status options in the CHPL")
    @RequestMapping(value = "/data/developer_statuses", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getDeveloperStatuses() {
        Set<KeyValueModel> data = dimensionalDataManager.getDeveloperStatuses();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible surveillance type options in the CHPL")
    @RequestMapping(value = "/data/surveillance_types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getSurveillanceTypes() {
        Set<KeyValueModel> data = dimensionalDataManager.getSurveillanceTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible surveillance result type options in the CHPL")
    @RequestMapping(value = "/data/surveillance_result_types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getSurveillanceResultTypes() {
        Set<KeyValueModel> data = dimensionalDataManager.getSurveillanceResultTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible surveillance requirement type options in the CHPL")
    @RequestMapping(value = "/data/surveillance_requirement_types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getSurveillanceRequirementTypes() {
        Set<KeyValueModel> data = dimensionalDataManager.getSurveillanceRequirementTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get all possible surveillance requirement options in the CHPL")
    @RequestMapping(value = "/data/surveillance_requirements", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SurveillanceRequirementOptionsDeprecated getSurveillanceRequirementOptionsDeprecated() {
        SurveillanceRequirementOptionsDeprecated data =
                dimensionalDataManager.getSurveillanceRequirementOptionsDeprecated();
        return data;
    }

    @ApiOperation(value = "Get all possible surveillance requirement options in the CHPL")
    @RequestMapping(value = "/data/surveillance-requirements", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SurveillanceRequirementOptions getSurveillanceRequirementOptions() {
        SurveillanceRequirementOptions data =
                dimensionalDataManager.getSurveillanceRequirementOptions();
        return data;
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED - Get all possible nonconformity status type options in the CHPL")
    @RequestMapping(value = "/data/nonconformity_status_types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getNonconformityStatusTypes() {
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(new HashSet<KeyValueModel>());
        return result;
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Get all possible nonconformity type options in the CHPL")
    @RequestMapping(value = "/data/nonconformity_types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getNonconformityTypeOptionsDeprecated() {
        Set<KeyValueModel> data = dimensionalDataManager.getNonconformityTypeOptionsDeprecated();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible nonconformity type options in the CHPL")
    @RequestMapping(value = "/data/nonconformity-types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getNonconformityTypeOptions() {
        Set<CertificationCriterion> data = dimensionalDataManager.getNonconformityTypeOptions();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all available pending listing upload template versions.")
    @RequestMapping(value = "/data/upload_template_versions", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getUploadTemplateVersions() {
        Set<UploadTemplateVersion> data = dimensionalDataManager.getUploadTemplateVersions();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    /**
     * Returns all of the fields that have a finite set of values and may be used
     * as filers when searching for listings.
     * @param simple whether to include data relevant to 2011 listings (2011 edition and NQF numbers)
     * @return a map of all filterable values
     * @throws EntityRetrievalException if an item cannot be retrieved from the db
     */
    @Deprecated
    @ApiOperation(value = "DEPRECATED. Use /data/search-options instead. Get all search options in the CHPL",
    notes = "This returns all of the other /data/{something} results in one single response.")
    @RequestMapping(value = "/data/search_options", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchableDimensionalData getSearchOptionsDeprecated(
            @RequestParam(value = "simple", required = false, defaultValue = "false") Boolean simple)
                    throws EntityRetrievalException {

        return dimensionalDataManager.getSearchableDimensionalData(simple);
    }

    /**
     * Returns all of the fields that have a finite set of values and may be used
     * as filers when searching for listings.
     * @param simple whether to include data relevant to 2011 listings (2011 edition and NQF numbers)
     * @return a map of all filterable values
     * @throws EntityRetrievalException if an item cannot be retrieved from the db
     */
    @ApiOperation(value = "Get all search options in the CHPL",
            notes = "This returns all of the other /data/{something} results in one single response.")
    @RequestMapping(value = "/data/search-options", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody DimensionalData getSearchOptions(
            @RequestParam(value = "simple", required = false, defaultValue = "false") Boolean simple)
                    throws EntityRetrievalException {
        return dimensionalDataManager.getDimensionalData(simple);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Use /collections/decertified-developers instead. "
            + "Get all developer decertifications in the CHPL",
            notes = "This returns all decertified developers.")
    @RequestMapping(value = "/decertifications/developers", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody DecertifiedDeveloperResults getDecertifiedDevelopers() throws EntityRetrievalException {
        DecertifiedDeveloperResults ddr = new DecertifiedDeveloperResults();
        List<DecertifiedDeveloperResult> results = developerManager.getDecertifiedDevelopers();
        ddr.setDecertifiedDeveloperResults(results);
        return ddr;
    }

    @ApiOperation(value = "Get all available filter type.")
    @RequestMapping(value = "/data/filter_types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getFilterTypes() {
        Set<KeyValueModel> data = filterManager.getFilterTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible complainant types in the CHPL")
    @RequestMapping(value = "/data/complainant-types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getComplainantTypes() {
        Set<KeyValueModel> data = complaintManager.getComplainantTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible certification criteria in the CHPL")
    @RequestMapping(value = "/data/certification-criteria", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertificationCriterionResults getCertificationCriteria() {
        Set<CertificationCriterion> criteria = dimensionalDataManager.getCertificationCriterion();
        CertificationCriterionResults result = new CertificationCriterionResults();
        for (CertificationCriterion criterion : criteria) {
            result.getCriteria().add(criterion);
        }
        return result;
    }

    @ApiOperation(value = "Get all possible change request types in the CHPL")
    @RequestMapping(value = "/data/change-request-types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getChangeRequestTypes() {
        Set<KeyValueModel> data = changeRequestManager.getChangeRequestTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible change request status types in the CHPL")
    @RequestMapping(value = "/data/change-request-status-types", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SearchOption getChangeRequestStatusTypes() {
        Set<KeyValueModel> data = changeRequestManager.getChangeRequestStatusTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible SVAP and associated criteria in the CHPL")
    @RequestMapping(value = "/data/svap", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody SvapResults getSvapCriteriaMaps() throws EntityRetrievalException {
        return new SvapResults(svapManager.getAllSvapCriteriaMaps());
    }
}
