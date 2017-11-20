package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.domain.PopulateSearchOptions;
import gov.healthit.chpl.domain.SearchOption;
import gov.healthit.chpl.domain.SurveillanceRequirementOptions;
import gov.healthit.chpl.domain.TestFunctionality;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.domain.UploadTemplateVersion;
import gov.healthit.chpl.domain.notification.NotificationType;
import gov.healthit.chpl.domain.search.NonconformitySearchOptions;
import gov.healthit.chpl.domain.search.SearchRequest;
import gov.healthit.chpl.domain.search.SearchResponse;
import gov.healthit.chpl.domain.search.SearchSetOperator;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.SearchMenuManager;
import gov.healthit.chpl.web.controller.results.DecertifiedDeveloperResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api
@RestController
public class SearchViewController {

    @Autowired
    Environment env;
    @Autowired
    MessageSource messageSource;

    @Autowired
    private SearchMenuManager searchMenuManager;

    @Autowired
    private CertifiedProductSearchManager certifiedProductSearchManager;

    @Autowired
    private DeveloperManager developerManager;

    private static final Logger LOGGER = LogManager.getLogger(SearchViewController.class);

    @ApiOperation(value = "Download the entire CHPL as XML.",
            notes = "Once per day, the entire certified product listing is written out to an XML "
                    + "file on the CHPL servers. This method allows any user to download that XML file. "
                    + "It is formatted in such a way that users may import it into Microsoft Excel or any other XML "
                    + "tool of their choosing.")
    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/xml")
    public void download(@RequestParam(value = "edition", required = false) String edition,
            @RequestParam(value = "format", defaultValue = "xml", required = false) String format,
            @RequestParam(value = "definition", defaultValue = "false", required = false) Boolean isDefinition,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        String downloadFileLocation = env.getProperty("downloadFolderPath");
        File downloadFile = new File(downloadFileLocation);
        if (!downloadFile.exists() || !downloadFile.canRead()) {
            response.getWriter()
                    .write(String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("resources.noReadPermission"),
                                    LocaleContextHolder.getLocale()),
                            downloadFileLocation));
            return;
        }

        if (downloadFile.isDirectory()) {
            // find the most recent file in the directory and use that
            File[] children = downloadFile.listFiles();
            if (children == null || children.length == 0) {
                response.getWriter()
                        .write(String.format(
                                messageSource.getMessage(new DefaultMessageSourceResolvable("resources.noFilesExist"),
                                        LocaleContextHolder.getLocale()),
                                downloadFileLocation));
                return;
            } else {
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
                }

                if (isDefinition != null && isDefinition.booleanValue() == true) {
                    String absolutePath = null;
                    if (format.equals("xml")) {
                        String schemaFilename = env.getProperty("schemaXmlName");
                        absolutePath = downloadFile.getAbsolutePath() + File.separator + schemaFilename;
                    } else if (edition.equals("2014")) {
                        String schemaFilename = env.getProperty("schemaCsv2014Name");
                        absolutePath = downloadFile.getAbsolutePath() + File.separator + schemaFilename;
                    } else if (edition.equals("2015")) {
                        String schemaFilename = env.getProperty("schemaCsv2015Name");
                        absolutePath = downloadFile.getAbsolutePath() + File.separator + schemaFilename;
                    }

                    if (!StringUtils.isEmpty(absolutePath)) {
                        downloadFile = new File(absolutePath);
                        if (!downloadFile.exists()) {
                            response.getWriter()
                                    .write(String.format(messageSource.getMessage(
                                            new DefaultMessageSourceResolvable("resources.schemaFileNotFound"),
                                            LocaleContextHolder.getLocale()), absolutePath));
                            return;
                        }
                    }
                } else {
                    File newestFileWithFormat = null;
                    for (int i = 0; i < children.length; i++) {

                        if (children[i].getName().matches("^chpl-" + edition + "-.+\\." + format + "$")) {
                            if (newestFileWithFormat == null) {
                                newestFileWithFormat = children[i];
                            } else {
                                if (children[i].lastModified() > newestFileWithFormat.lastModified()) {
                                    newestFileWithFormat = children[i];
                                }
                            }
                        }
                    }
                    if (newestFileWithFormat != null) {
                        downloadFile = newestFileWithFormat;
                    } else {
                        response.getWriter()
                                .write(String.format(messageSource.getMessage(
                                        new DefaultMessageSourceResolvable(
                                                "resources.fileWithEditionAndFormatNotFound"),
                                        LocaleContextHolder.getLocale()), edition, format));
                        return;
                    }
                }
            }
        }

        LOGGER.info("Downloading " + downloadFile.getName());

        FileInputStream inputStream = new FileInputStream(downloadFile);

        // set content attributes for the response
        response.setContentType("application/xml");
        response.setContentLength((int) downloadFile.length());

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
        response.setHeader(headerKey, headerValue);

        // get output stream of the response
        OutputStream outStream = response.getOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = -1;

        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();
    }

    @ApiOperation(value = "Search the CHPL",
            notes = "If paging parameters are not specified, the first 20 records are returned by default. "
                    + "All parameters are optional. "
                    + "Any parameter that can accept multiple things (i.e. certificationStatuses) expects a comma-delimited list of those things (i.e. certificationStatuses = Active,Suspended). "
                    + "Date parameters are required to be in the format "
                    + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT + ". ")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchTerm",
                    value = "CHPL ID, Developer (or previous developer) Name, Product Name, ONC-ACB Certification ID", required = false,
                    dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "certificationStatuses",
                    value = "A comma-separated list of certification statuses "
                            + "(ex: \"Active,Retired,Withdrawn by Developer\")).",
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "certificationEditions",
                    value = "A comma-separated list of certification editions to be 'or'ed together (ex: \"2014,2015\" finds listings with either edition 2014 or 2015).",
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "certificationCriteria",
                    value = "A comma-separated list of certification criteria to be queried together (ex: \"170.314 (a)(1),170.314 (a)(2)\" finds listings attesting to either 170.314 (a)(1) or 170.314 (a(2)).",
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "certificationCriteriaOperator",
                    value = "Either AND or OR. Defaults to OR. " + 
                            "Indicates whether a listing must have all certificationCriteria or may have any one or more of the certificationCriteria.",
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "cqms",
                    value = "A comma-separated list of cqms to be queried together (ex: \"CMS2,CMS9\" finds listings with either CMS2 or CMS9).",
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "cqmsCriteriaOperator",
                value = "Either AND or OR. Defaults to OR. " +
                        "Indicates whether a listing must have all cqms or may have any one or more of the cqms.",
                required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "certificationBodies",
                    value = "A comma-separated list of certification body names to be 'or'ed together (ex: \"Drummond,ICSA\" finds listings belonging to either Drummond or ICSA).",
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "nonconformityOptions",
                    value = "A comma-separated list of nonconformity search options. Valid options are "
                            + "OPEN_NONCONFORMITY, CLOSED_NONCONFORMITY, and NEVER_NONCONFORMITY.",
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "nonconformityOptionsOperator",
                value = "Either AND or OR. Defaults to OR." + 
                        "Indicates whether a listing must have met all nonconformityOptions specified or may have met any one or more of the nonconformityOptions",
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
                    value = "Number of results to return used in concert with pageNumber. Defaults to 20. Maximum allowed page size is 100.",
                    required = false, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "orderBy",
                    value = "What to order by. Options are one of the following: " +
                            SearchRequest.ORDER_BY_DEVELOPER + ", " + SearchRequest.ORDER_BY_PRODUCT + ", " +
                            SearchRequest.ORDER_BY_VERSION + ", " + SearchRequest.ORDER_BY_CERTIFICATION_EDITION + ", " +
                            ", or " + SearchRequest.ORDER_BY_CERTIFICATION_BODY + ", " + 
                            ". Defaults to " + SearchRequest.ORDER_BY_PRODUCT + ".",
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
            certificationStatusesDelimited = certificationStatusesDelimited.trim();
            if (!StringUtils.isEmpty(certificationStatusesDelimited)) {
                String[] certificationStatusArr = certificationStatusesDelimited.split(",");
                if (certificationStatusArr != null && certificationStatusArr.length > 0) {
                    Set<String> certificationStatuses = new HashSet<String>();
                    Set<KeyValueModel> availableCertificationStatuses = searchMenuManager.getCertificationStatuses();

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
            certificationEditionsDelimited = certificationEditionsDelimited.trim();
            if (!StringUtils.isEmpty(certificationEditionsDelimited)) {
                String[] certificationEditionsArr = certificationEditionsDelimited.split(",");
                if (certificationEditionsArr != null && certificationEditionsArr.length > 0) {
                    Set<String> certificationEditions = new HashSet<String>();
                    Set<KeyValueModel> availableCertificationEditions = searchMenuManager.getEditionNames(false);

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
            certificationCriteriaDelimited = certificationCriteriaDelimited.trim();
            if (!StringUtils.isEmpty(certificationCriteriaDelimited)) {
                String[] certificationCriteriaArr = certificationCriteriaDelimited.split(",");
                if (certificationCriteriaArr != null && certificationCriteriaArr.length > 0) {
                    Set<String> certificationCriterion = new HashSet<String>();
                    Set<DescriptiveModel> availableCriterion = searchMenuManager
                            .getCertificationCriterionNumbers(false);

                    for (int i = 0; i < certificationCriteriaArr.length; i++) {
                        String certCriteriaParam = certificationCriteriaArr[i].trim();
                        validateCertificationCriteria(certCriteriaParam, availableCriterion);
                        certificationCriterion.add(certCriteriaParam);
                    }
                    searchRequest.setCertificationCriteria(certificationCriterion);
                    
                    if(!StringUtils.isEmpty(certificationCriteriaOperatorStr)) {
                        certificationCriteriaOperatorStr = certificationCriteriaOperatorStr.trim();
                        SearchSetOperator certificationCriteriaOperator = validateSearchSetOperator(certificationCriteriaOperatorStr);
                        searchRequest.setCertificationCriteriaOperator(certificationCriteriaOperator);
                    }
                }
            }
        }

        if (cqmsDelimited != null) {
            cqmsDelimited = cqmsDelimited.trim();
            if (!StringUtils.isEmpty(cqmsDelimited)) {
                String[] cqmsArr = cqmsDelimited.split(",");
                if (cqmsArr != null && cqmsArr.length > 0) {
                    Set<String> cqms = new HashSet<String>();
                    Set<DescriptiveModel> availableCqms = searchMenuManager.getCQMCriterionNumbers(false);

                    for (int i = 0; i < cqmsArr.length; i++) {
                        String cqmParam = cqmsArr[i].trim();
                        validateCqm(cqmParam, availableCqms);
                        cqms.add(cqmParam.trim());
                    }
                    searchRequest.setCqms(cqms);
                    
                    if(!StringUtils.isEmpty(cqmsOperatorStr)) {
                        cqmsOperatorStr = cqmsOperatorStr.trim();
                        SearchSetOperator cqmOperator = validateSearchSetOperator(cqmsOperatorStr);
                        searchRequest.setCqmsOperator(cqmOperator);
                    }
                }
            }
        }

        if (certificationBodiesDelimited != null) {
            certificationBodiesDelimited = certificationBodiesDelimited.trim();
            if (!StringUtils.isEmpty(certificationBodiesDelimited)) {
                String[] certificationBodiesArr = certificationBodiesDelimited.split(",");
                if (certificationBodiesArr != null && certificationBodiesArr.length > 0) {
                    Set<String> certBodies = new HashSet<String>();
                    Set<KeyValueModel> availableCertBodies = searchMenuManager.getCertBodyNames(true);

                    for (int i = 0; i < certificationBodiesArr.length; i++) {
                        String certBodyParam = certificationBodiesArr[i].trim();
                        validateCertificationBody(certBodyParam, availableCertBodies);
                        certBodies.add(certBodyParam);
                    }
                    searchRequest.setCertificationBodies(certBodies);
                }
            }
        }

        if(!StringUtils.isEmpty(hasHadSurveillanceStr)) {
            if(!hasHadSurveillanceStr.equalsIgnoreCase(Boolean.TRUE.toString()) && 
               !hasHadSurveillanceStr.equalsIgnoreCase(Boolean.FALSE.toString())) {
                String err = String.format(
                        messageSource.getMessage(new DefaultMessageSourceResolvable("search.hasHadSurveillance.invalid"),
                                LocaleContextHolder.getLocale()),
                        hasHadSurveillanceStr);
                throw new InvalidArgumentsException(err);
            }
            Boolean hasHadSurveillance = Boolean.parseBoolean(hasHadSurveillanceStr);
            searchRequest.getSurveillance().setHasHadSurveillance(hasHadSurveillance);
        }
        
        if (!StringUtils.isEmpty(nonconformityOptionsDelimited)) {
            nonconformityOptionsDelimited = nonconformityOptionsDelimited.trim();
            String[] nonconformityOptionsArr = nonconformityOptionsDelimited.split(",");
            if (nonconformityOptionsArr != null && nonconformityOptionsArr.length > 0) {
                Set<NonconformitySearchOptions> nonconformitySearchOptions = new HashSet<NonconformitySearchOptions>();
                for (int i = 0; i < nonconformityOptionsArr.length; i++) {
                    String nonconformityOptionParam = nonconformityOptionsArr[i].trim();
                    try {
                        NonconformitySearchOptions ncOpt = NonconformitySearchOptions.valueOf(nonconformityOptionParam);
                        if (ncOpt != null) {
                            nonconformitySearchOptions.add(ncOpt);
                        } else {
                            String err = String.format(
                                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.nonconformitySearchOption.invalid"),
                                            LocaleContextHolder.getLocale()),
                                    nonconformityOptionParam, NonconformitySearchOptions.CLOSED_NONCONFORMITY.name() 
                                    + ", " + NonconformitySearchOptions.NEVER_NONCONFORMITY.name()
                                    + ", or " + NonconformitySearchOptions.OPEN_NONCONFORMITY.name());
                            throw new InvalidArgumentsException(err);
                        }
                    } catch (Exception ex) {
                        String err = String.format(
                                messageSource.getMessage(new DefaultMessageSourceResolvable("search.nonconformitySearchOption.invalid"),
                                        LocaleContextHolder.getLocale()),
                                nonconformityOptionParam, NonconformitySearchOptions.CLOSED_NONCONFORMITY.name() 
                                + ", " + NonconformitySearchOptions.NEVER_NONCONFORMITY.name()
                                + ", or " + NonconformitySearchOptions.OPEN_NONCONFORMITY.name());
                        throw new InvalidArgumentsException(err);
                    }
                }
                searchRequest.getSurveillance().setNonconformityOptions(nonconformitySearchOptions);
                
                if(!StringUtils.isEmpty(nonconformityOptionsOperator)) {
                    nonconformityOptionsOperator = nonconformityOptionsOperator.trim();
                    SearchSetOperator ncOperator = validateSearchSetOperator(nonconformityOptionsOperator);
                    searchRequest.getSurveillance().setNonconformityOptionsOperator(ncOperator);
                }
            }
        }

        if (developer != null) {
            developer = developer.trim();
            if (!StringUtils.isEmpty(developer)) {
                searchRequest.setDeveloper(developer.trim());
            }
        }

        if (product != null) {
            product = product.trim();
            if (!StringUtils.isEmpty(product)) {
                searchRequest.setProduct(product.trim());
            }
        }

        if (version != null) {
            version = version.trim();
            if (!StringUtils.isEmpty(version)) {
                searchRequest.setVersion(version.trim());
            }
        }

        if (practiceType != null) {
            practiceType = practiceType.trim();
            if (!StringUtils.isEmpty(practiceType)) {
                validatePracticeTypeParameter(practiceType);
                searchRequest.setPracticeType(practiceType);
            }
        }

        if (!StringUtils.isEmpty(certificationDateStart)) {
            certificationDateStart = certificationDateStart.trim();
            if (!StringUtils.isEmpty(certificationDateStart)) {
                validateCertificationDateParameter(certificationDateStart);
                searchRequest.setCertificationDateStart(certificationDateStart);
            }
        }
        
        if (!StringUtils.isEmpty(certificationDateEnd)) {
            certificationDateEnd = certificationDateEnd.trim();
            if (!StringUtils.isEmpty(certificationDateEnd)) {
                validateCertificationDateParameter(certificationDateEnd);
                searchRequest.setCertificationDateEnd(certificationDateEnd);
            }
        }
        validatePageSize(pageSize);
        orderBy = orderBy.trim();
        validateOrderBy(orderBy);
        
        searchRequest.setPageNumber(pageNumber);
        searchRequest.setPageSize(pageSize);
        searchRequest.setOrderBy(orderBy);
        searchRequest.setSortDescending(sortDescending);
        
        //trim everything
        searchRequest.cleanAllParameters();
        return certifiedProductSearchManager.search(searchRequest);

    }

    @ApiOperation(value = "Search the CHPL with an HTTP POST Request.",
            notes = "Search the CHPL by specifycing multiple fields of the data to search. "
                    + "If paging fields are not specified, the first 20 records are returned by default.")
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchResponse searchPost(@RequestBody SearchRequest searchRequest)
            throws InvalidArgumentsException, EntityRetrievalException {
        //trim everything
        searchRequest.cleanAllParameters();
        
        if(searchRequest.getCertificationStatuses() != null && searchRequest.getCertificationStatuses().size() > 0) {
            Set<KeyValueModel> availableCertificationStatuses = searchMenuManager.getCertificationStatuses();
            for(String certStatusName : searchRequest.getCertificationStatuses()) {
                validateCertificationStatus(certStatusName, availableCertificationStatuses);
            }
        }
        
        if(searchRequest.getCertificationEditions() != null && searchRequest.getCertificationEditions().size() > 0) {
            Set<KeyValueModel> availableCertificationEditions = searchMenuManager.getEditionNames(false);
            for(String certEditionName : searchRequest.getCertificationEditions()) {
                validateCertificationEdition(certEditionName, availableCertificationEditions);
            }
        }
        
        if(searchRequest.getCertificationCriteria() != null && searchRequest.getCertificationCriteria().size() > 0) {
            Set<DescriptiveModel> availableCriterion = searchMenuManager
                    .getCertificationCriterionNumbers(false);
            for(String criteria : searchRequest.getCertificationCriteria()) {
                validateCertificationCriteria(criteria, availableCriterion);
            }
        }
        
        if(searchRequest.getCqms() != null && searchRequest.getCqms().size() > 0) {
            Set<DescriptiveModel> availableCqms = searchMenuManager.getCQMCriterionNumbers(false);
            for(String cqm : searchRequest.getCqms()) {
                validateCqm(cqm, availableCqms);
            }
        }
        
        if(searchRequest.getCertificationBodies() != null && searchRequest.getCertificationBodies().size() > 0) {
            Set<KeyValueModel> availableCertBodies = searchMenuManager.getCertBodyNames(true);
            for(String certBody : searchRequest.getCertificationBodies()) {
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
            Set<KeyValueModel> availableCertBodies) throws InvalidArgumentsException {
        boolean found = false;
        for (KeyValueModel currAvailableCertBody : availableCertBodies) {
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
            Set<DescriptiveModel> availableCriterion) throws InvalidArgumentsException {
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
    
    private SearchSetOperator validateSearchSetOperator(String searchSetOperator) throws InvalidArgumentsException {
        SearchSetOperator result = null;
        try {
            SearchSetOperator operatorEnum = SearchSetOperator.valueOf(searchSetOperator);
            if(operatorEnum != null) {
                result = operatorEnum;
            } else {
                String err = String.format(
                        messageSource.getMessage(new DefaultMessageSourceResolvable("search.searchOperator.invalid"),
                                LocaleContextHolder.getLocale()),
                        searchSetOperator, SearchSetOperator.OR + " or " + SearchSetOperator.AND);
                throw new InvalidArgumentsException(err);
            }
        } catch(Exception ex) {
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
            Set<KeyValueModel> availablePracticeTypes = searchMenuManager.getPracticeTypeNames();
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
                } catch (final ParseException ex) {
                    String err = String.format(
                            messageSource.getMessage(new DefaultMessageSourceResolvable("search.certificationDate.invalid"),
                                    LocaleContextHolder.getLocale()),
                            dateStr, SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
                    LOGGER.error(err);
                    throw new InvalidArgumentsException(err);
                }
            }
        }
    }
    
    private void validatePageSize(Integer pageSize) throws InvalidArgumentsException {
        if(pageSize > 100) {
            String err = String.format(
                    messageSource.getMessage(new DefaultMessageSourceResolvable("search.pageSize.invalid"),
                            LocaleContextHolder.getLocale()),
                    SearchRequest.MAX_PAGE_SIZE);
            throw new InvalidArgumentsException(err);
        }
    }
    
    private void validateOrderBy(String orderBy) throws InvalidArgumentsException {
        if(!orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_CERTIFICATION_BODY) && 
            !orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_CERTIFICATION_EDITION) && 
            !orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_DEVELOPER) && 
            !orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_PRODUCT) && 
            !orderBy.equalsIgnoreCase(SearchRequest.ORDER_BY_VERSION)) {
            String err = String.format(
                messageSource.getMessage(new DefaultMessageSourceResolvable("search.orderBy.invalid"),
                        LocaleContextHolder.getLocale()),
                orderBy, SearchRequest.ORDER_BY_CERTIFICATION_BODY + ", " + 
                    SearchRequest.ORDER_BY_CERTIFICATION_EDITION + ", " + 
                    SearchRequest.ORDER_BY_DEVELOPER + ", " + 
                    SearchRequest.ORDER_BY_PRODUCT + ", or " +
                    SearchRequest.ORDER_BY_VERSION);
            throw new InvalidArgumentsException(err);
        }
    }
    
    @Secured({
            Authority.ROLE_ADMIN, Authority.ROLE_ACB_ADMIN
    })
    @ApiOperation(value = "Get all possible types of jobs that can be created in the system.")
    @RequestMapping(value = "/data/job_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<KeyValueModel> getJobTypes() {
        return searchMenuManager.getJobTypes();
    }

    @Secured({
            Authority.ROLE_ADMIN, Authority.ROLE_ACB_ADMIN
    })
    @ApiOperation(value = "Get all possible types of notifications that a user can sign up for.")
    @RequestMapping(value = "/data/notification_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<NotificationType> getNotificationTypes() {
        return searchMenuManager.getNotificationTypes();
    }

    @ApiOperation(value = "Get all possible classifications in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/classification_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<KeyValueModel> getClassificationNames() {
        return searchMenuManager.getClassificationNames();
    }

    @ApiOperation(value = "Get all possible certificaiton editions in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/certification_editions", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<KeyValueModel> getEditionNames() {
        return searchMenuManager.getEditionNames(false);
    }

    @ApiOperation(value = "Get all possible certification statuses in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/certification_statuses", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<KeyValueModel> getCertificationStatuses() {
        return searchMenuManager.getCertificationStatuses();
    }

    @ApiOperation(value = "Get all possible practice types in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/practice_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<KeyValueModel> getPracticeTypeNames() {
        return searchMenuManager.getPracticeTypeNames();
    }

    @ApiOperation(value = "Get all possible product names in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/products", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<KeyValueModelStatuses> getProductNames() {
        return searchMenuManager.getProductNames();
    }

    @ApiOperation(value = "Get all possible developer names in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/developers", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<KeyValueModelStatuses> getDeveloperNames() {
        return searchMenuManager.getDeveloperNames();
    }

    @ApiOperation(value = "Get all possible ACBs in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/certification_bodies", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Set<KeyValueModel> getCertBodyNames() {
        return searchMenuManager.getCertBodyNames(false);
    }

    @ApiOperation(value = "Get all possible education types in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/education_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getEducationTypes() {
        Set<KeyValueModel> data = searchMenuManager.getEducationTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test participant age ranges in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/age_ranges", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getAgeRanges() {
        Set<KeyValueModel> data = searchMenuManager.getAgeRanges();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test functionality options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/test_functionality", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getTestFunctionality() {
        Set<TestFunctionality> data = searchMenuManager.getTestFunctionality();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test tool options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/test_tools", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getTestTools() {
        Set<KeyValueModel> data = searchMenuManager.getTestTools();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible test standard options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/test_standards", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getTestStandards() {
        Set<TestStandard> data = searchMenuManager.getTestStandards();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible qms standard options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/qms_standards", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getQmsStandards() {
        Set<KeyValueModel> data = searchMenuManager.getQmsStandards();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible targeted user options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/targeted_users", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getTargetedUsers() {
        Set<KeyValueModel> data = searchMenuManager.getTargetedUesrs();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible UCD process options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/ucd_processes", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getUcdProcesses() {
        Set<KeyValueModel> data = searchMenuManager.getUcdProcesses();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible accessibility standard options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/accessibility_standards", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getAccessibilityStandards() {
        Set<KeyValueModel> data = searchMenuManager.getAccessibilityStandards();
        SearchOption result = new SearchOption();
        result.setExpandable(true);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible macra measure options in the CHPL",
            notes = "This is useful for knowing what values one might possibly search for.")
    @RequestMapping(value = "/data/macra_measures", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getMacraMeasures() {
        Set<CriteriaSpecificDescriptiveModel> data = searchMenuManager.getMacraMeasures();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible developer status options in the CHPL")
    @RequestMapping(value = "/data/developer_statuses", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getDeveloperStatuses() {
        Set<KeyValueModel> data = searchMenuManager.getDeveloperStatuses();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible surveillance type options in the CHPL")
    @RequestMapping(value = "/data/surveillance_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getSurveillanceTypes() {
        Set<KeyValueModel> data = searchMenuManager.getSurveillanceTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible surveillance result type options in the CHPL")
    @RequestMapping(value = "/data/surveillance_result_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getSurveillanceResultTypes() {
        Set<KeyValueModel> data = searchMenuManager.getSurveillanceResultTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible surveillance requirement type options in the CHPL")
    @RequestMapping(value = "/data/surveillance_requirement_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getSurveillanceRequirementTypes() {
        Set<KeyValueModel> data = searchMenuManager.getSurveillanceRequirementTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible surveillance requirement options in the CHPL")
    @RequestMapping(value = "/data/surveillance_requirements", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SurveillanceRequirementOptions getSurveillanceRequirementOptions() {
        SurveillanceRequirementOptions data = searchMenuManager.getSurveillanceRequirementOptions();
        return data;
    }

    @ApiOperation(value = "Get all possible nonconformity status type options in the CHPL")
    @RequestMapping(value = "/data/nonconformity_status_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getNonconformityStatusTypes() {
        Set<KeyValueModel> data = searchMenuManager.getNonconformityStatusTypes();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all possible nonconformity type options in the CHPL")
    @RequestMapping(value = "/data/nonconformity_types", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getNonconformityTypeOptions() {
        Set<KeyValueModel> data = searchMenuManager.getNonconformityTypeOptions();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all available pending listing upload template versions.")
    @RequestMapping(value = "/data/upload_template_versions", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody SearchOption getUploadTemplateVersions() {
        Set<UploadTemplateVersion> data = searchMenuManager.getUploadTemplateVersions();
        SearchOption result = new SearchOption();
        result.setExpandable(false);
        result.setData(data);
        return result;
    }

    @ApiOperation(value = "Get all search options in the CHPL",
            notes = "This returns all of the other /data/{something} results in one single response.")
    @RequestMapping(value = "/data/search_options", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody PopulateSearchOptions getPopulateSearchData(
            @RequestParam(value = "simple", required = false) Boolean simple,
            @RequestParam(value = "showDeleted", required = false, defaultValue = "false") Boolean showDeleted)
            throws EntityRetrievalException {
        if (simple == null) {
            simple = false;
        }

        PopulateSearchOptions searchOptions = new PopulateSearchOptions();
        searchOptions.setCertBodyNames(searchMenuManager.getCertBodyNames(showDeleted));
        searchOptions.setEditions(searchMenuManager.getEditionNames(simple));
        searchOptions.setCertificationStatuses(searchMenuManager.getCertificationStatuses());
        searchOptions.setPracticeTypeNames(searchMenuManager.getPracticeTypeNames());
        searchOptions.setProductClassifications(searchMenuManager.getClassificationNames());
        searchOptions.setProductNames(searchMenuManager.getProductNames());
        searchOptions.setDeveloperNames(searchMenuManager.getDeveloperNames());
        searchOptions.setCqmCriterionNumbers(searchMenuManager.getCQMCriterionNumbers(simple));
        searchOptions.setCertificationCriterionNumbers(searchMenuManager.getCertificationCriterionNumbers(simple));

        return searchOptions;
    }

    @ApiOperation(value = "Get all developer decertifications in the CHPL",
            notes = "This returns all decertified developers.")
    @RequestMapping(value = "/decertifications/developers", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody DecertifiedDeveloperResults getDecertifiedDevelopers() throws EntityRetrievalException {
        DecertifiedDeveloperResults ddr = developerManager.getDecertifiedDevelopers();
        return ddr;
    }
}
