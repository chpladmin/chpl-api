package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchBasicDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.IdListContainer;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.PendingCertifiedProductMetadata;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMetadataDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.DeprecatedUploadTemplateException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.CertifiedProductUploadManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.PendingValidator;
import gov.healthit.chpl.validation.listing.Validator;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import gov.healthit.chpl.web.controller.results.CQMResultDetailResults;
import gov.healthit.chpl.web.controller.results.CertificationResults;
import gov.healthit.chpl.web.controller.results.PendingCertifiedProductResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Certified Product Controller.
 */
@Api(value = "certified-products")
@RestController
@RequestMapping("/certified_products")
public class CertifiedProductController {
    private static Logger LOGGER = LogManager.getLogger(CertifiedProductController.class);

    @Value("${uploadErrorEmailRecipients}")
    private String uploadErrorEmailRecipients;

    @Value("${uploadErrorEmailSubject}")
    private String uploadErrorEmailSubject;

    private CertifiedProductUploadManager uploadManager;
    private CertifiedProductDetailsManager cpdManager;
    private CertifiedProductManager cpManager;
    private ResourcePermissions resourcePermissions;
    private PendingCertifiedProductManager pcpManager;
    private ActivityManager activityManager;
    private ListingValidatorFactory validatorFactory;
    private Environment env;
    private ErrorMessageUtil msgUtil;
    private FileUtils fileUtils;
    private ChplProductNumberUtil chplProductNumberUtil;
    private DeveloperManager developerManager;

    @Autowired
    public CertifiedProductController(CertifiedProductUploadManager uploadManager,
            CertifiedProductDetailsManager cpdManager, CertifiedProductManager cpManager,
            ResourcePermissions resourcePermissions, PendingCertifiedProductManager pcpManager,
            ActivityManager activityManager, ListingValidatorFactory validatorFactory,
            Environment env, ErrorMessageUtil msgUtil, FileUtils fileUtils,
            ChplProductNumberUtil chplProductNumberUtil, DeveloperManager developerManager) {
        this.uploadManager = uploadManager;
        this.cpdManager = cpdManager;
        this.cpManager = cpManager;
        this.resourcePermissions = resourcePermissions;
        this.pcpManager = pcpManager;
        this.activityManager = activityManager;
        this.validatorFactory = validatorFactory;
        this.env = env;
        this.msgUtil = msgUtil;
        this.fileUtils = fileUtils;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.developerManager = developerManager;
    }

    /**
     * List all certified products.
     * @param versionId if entered, filters list to only listings under given version
     * @param editable if true, returns only those user has ability to edit
     * @return list of certified products
     * @throws EntityRetrievalException if unable to retrieve entity
     */
    @ApiOperation(value = "List all certified products",
            notes = "Default behavior is to return all certified products in the system. "
                    + " The required 'versionId' parameter filters the certified products to those"
                    + " assigned to that version. The 'editable' parameter will return only those"
                    + " certified products that the logged in user has permission to edit as "
                    + " determined by ACB roles and authorities. Not all information about "
                    + " every certified product is returned. Call the /details service for more information.")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertifiedProduct> getCertifiedProductsByVersion(
            @RequestParam(required = true) Long versionId,
            @RequestParam(required = false, defaultValue = "false") boolean editable)
                    throws EntityRetrievalException {
        List<CertifiedProduct> certifiedProductList = null;

        if (editable) {
            certifiedProductList = cpManager.getByVersionWithEditPermission(versionId);
        } else {
            certifiedProductList = cpManager.getByVersion(versionId);
        }

        return certifiedProductList;
    }

    /**
     * Get all details for a specified certified product.
     * @param certifiedProductId database id of listing
     * @return Listing Details domain object
     * @throws EntityRetrievalException if cannot retrieve Listing
     */
    @ApiOperation(value = "Get all details for a specified certified product.",
            notes = "Returns all information in the CHPL related to the specified certified product.")
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/details",
    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductById(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {

        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetails(certifiedProductId);
        certifiedProduct = validateCertifiedProduct(certifiedProduct);

        return certifiedProduct;
    }

    /**
     * Get certified product details for a listing based on unique CHPL ID.
     * @param year two-digit year (14 or 15)
     * @param testingLab two-digit ATL code
     * @param certBody two-digit ACB code
     * @param vendorCode assigned developer code
     * @param productCode user-defined product code
     * @param versionCode user-defined version code
     * @param icsCode two-digit ICS code
     * @param addlSoftwareCode single-digit additional software code (0 or 1)
     * @param certDateCode certified date code (YYMMDD format)
     * @return details for the listing with the unique CHPL ID specified
     * @throws EntityRetrievalException if a listing with the unique CHPL ID cannot be found.
     */
    @SuppressWarnings({"checkstyle:parameternumber"})
    @ApiOperation(value = "Get all details for a specified certified product.",
    notes = "Returns all information in the CHPL related to the specified certified product.  "
            + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
            + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number.  A valid call "
            + "to this service would look like "
            + "/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/details")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
            + "{addlSoftwareCode}.{certDateCode}/details",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductByChplProductNumber(
            @PathVariable("year") String year,
            @PathVariable("testingLab") String testingLab,
            @PathVariable("certBody") String certBody,
            @PathVariable("vendorCode") String vendorCode,
            @PathVariable("productCode") String productCode,
            @PathVariable("versionCode") String versionCode,
            @PathVariable("icsCode") String icsCode,
            @PathVariable("addlSoftwareCode") String addlSoftwareCode,
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException  {

        String chplProductNumber =
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                        versionCode, icsCode, addlSoftwareCode, certDateCode);

        CertifiedProductSearchDetails certifiedProduct =
                cpdManager.getCertifiedProductDetailsByChplProductNumber(chplProductNumber);

        Validator validator = validatorFactory.getValidator(certifiedProduct);
        if (validator != null) {
            validator.validate(certifiedProduct);
        }

        return certifiedProduct;
    }

    @ApiOperation(value = "Get all details for a specified certified product.",
            notes = "Returns all information in the CHPL related to the specified certified product.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call "
                    + "to this service would look like /certified_products/CHP-999999.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}/details",
    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductByChplProductNumber2(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);

        CertifiedProductSearchDetails certifiedProduct =
                cpdManager.getCertifiedProductDetailsByChplProductNumber(chplProductNumber);

        certifiedProduct = validateCertifiedProduct(certifiedProduct);

        return certifiedProduct;
    }

    @ApiOperation(value = "Get all basic information for a specified certified product.  Does not include "
            + "the CQM results and certification results.",
            notes = "Returns basic information in the CHPL related to the specified certified product.  "
                    + "The results will not include the CQM results and certification results.")
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}",
    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertifiedProductSearchBasicDetails getCertifiedProductByIdBasic(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {

        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetailsBasic(certifiedProductId);

        return mapCertifiedProductDetailsToBasic.apply(certifiedProduct);
    }

    /**
     * Get "basic" information for a listing which includes all details
     * except for certification results and cqm results.
     * @param year two-digit year (14 or 15)
     * @param testingLab two-digit ATL code
     * @param certBody two-digit ACB code
     * @param vendorCode assigned developer code
     * @param productCode user-defined product code
     * @param versionCode user-defined version code
     * @param icsCode two-digit ICS code
     * @param addlSoftwareCode single-digit additional software code (0 or 1)
     * @param certDateCode certified date code (YYMMDD format)
     * @return the basic information about the listing identified by the unique CHPL ID specified
     * @throws EntityRetrievalException if the listing cannot be found
     */
    @SuppressWarnings({"checkstyle:parameternumber"})
    @ApiOperation(value = "Get all basic information for a specified certified product.  Does not include "
            + "the CQM results and certification results.",
            notes = "Returns basic information in the CHPL related to the specified certified product.  "
                    + "The results will not include the CQM results and certification results.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
                    + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number.  A valid "
                    + "call to this service would look like /certified_products/YY.99.99.9999.XXXX.99.99.9."
                    + "YYMMDD.")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
            + "{addlSoftwareCode}.{certDateCode}",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody
    CertifiedProductSearchBasicDetails getCertifiedProductByChplProductNumberBasic(
            @PathVariable("year") String year,
            @PathVariable("testingLab") String testingLab,
            @PathVariable("certBody") String certBody,
            @PathVariable("vendorCode") String vendorCode,
            @PathVariable("productCode") String productCode,
            @PathVariable("versionCode") String versionCode,
            @PathVariable("icsCode") String icsCode,
            @PathVariable("addlSoftwareCode") String addlSoftwareCode,
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException  {

        String chplProductNumber =
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                        versionCode, icsCode, addlSoftwareCode, certDateCode);

        CertifiedProductSearchDetails certifiedProduct =
                cpdManager.getCertifiedProductDetailsBasicByChplProductNumber(chplProductNumber);


        return mapCertifiedProductDetailsToBasic.apply(certifiedProduct);
    }

    @ApiOperation(value = "Get all basic information for a specified certified product.  Does not include "
            + "the CQM results and certification results.",
            notes = "Returns basic information in the CHPL related to the specified certified product.  "
                    + "The results will not include the CQM results and certification results.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to "
                    + "this service would look like /certified_products/CHP-999999.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}",
    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertifiedProductSearchBasicDetails getCertifiedProductByChplProductNumberBasic2(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);

        CertifiedProductSearchDetails certifiedProduct =
                cpdManager.getCertifiedProductDetailsBasicByChplProductNumber(chplProductNumber);

        return mapCertifiedProductDetailsToBasic.apply(certifiedProduct);
    }

    @ApiOperation(value = "Get all of the CQM results for a specified certified product.",
            notes = "Returns all of the CQM results in the CHPL related to the specified certified product.")
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/cqm_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CQMResultDetailResults getCqmsByCertifiedProductId(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {

        CQMResultDetailResults results =
                new CQMResultDetailResults(cpdManager.getCertifiedProductCqms(certifiedProductId));

        return results;
    }

    @ApiOperation(value = "Get all of the CQM results for a specified certified product.",
            notes = "Returns all of the CQM results in the CHPL related to the specified certified product.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
                    + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number.  A valid call to "
                    + "this service would look like /certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/"
                    + "cqm_results.")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
            + "{addlSoftwareCode}.{certDateCode}/cqm_results", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CQMResultDetailResults getCqmsByCertifiedProductId(
            @PathVariable("year") String year,
            @PathVariable("testingLab") String testingLab,
            @PathVariable("certBody") String certBody,
            @PathVariable("vendorCode") String vendorCode,
            @PathVariable("productCode") String productCode,
            @PathVariable("versionCode") String versionCode,
            @PathVariable("icsCode") String icsCode,
            @PathVariable("addlSoftwareCode") String addlSoftwareCode,
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException  {

        String chplProductNumber =
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                        versionCode, icsCode, addlSoftwareCode, certDateCode);
        CQMResultDetailResults results =
                new CQMResultDetailResults(cpdManager.getCertifiedProductCqms(chplProductNumber));

        return results;
    }

    @ApiOperation(value = "Get all of the CQM results for a specified certified product based on a legacy "
            + "CHPL Product Number.",
            notes = "\"Returns all of the CQM results in the CHPL related to the specified certified product.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call "
                    + "to this service would look like /certified_products/CHP-999999/cqm_results.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}/cqm_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CQMResultDetailResults getCqmsByCertifiedProductId(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException  {

        String chplProductNumber =  chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        CQMResultDetailResults results =
                new CQMResultDetailResults(cpdManager.getCertifiedProductCqms(chplProductNumber));

        return results;
    }

    @ApiOperation(value = "Get all of the certification results for a specified certified product.",
            notes = "Returns all of the certifiection results in the CHPL related to the specified certified product.")
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/certification_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertificationResults getCertificationResultsByCertifiedProductId(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {

        CertificationResults results =
                new CertificationResults(cpdManager.getCertifiedProductCertificationResults(certifiedProductId));

        return results;
    }

    /**
     * Download all SED details that are certified to 170.315(g)(3).
     * @return an HTTP response
     * @throws EntityRetrievalException if cannot retrieve entity
     * @throws IOException if IO Exception
     */
    @ApiOperation(value = "Get all of the certification results for a specified certified "
            + "product based on a CHPL Product Number.",
            notes = "Returns all of the certification results in the CHPL related to the specified certified product.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
                    + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number. "
                    + "A valid call to this service would look like "
                    + "/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/certification_results.")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}"
            + ".{certDateCode}/certification_results", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertificationResults getCertificationResultsByCertifiedProductId(
            @PathVariable("year") String year,
            @PathVariable("testingLab") String testingLab,
            @PathVariable("certBody") String certBody,
            @PathVariable("vendorCode") String vendorCode,
            @PathVariable("productCode") String productCode,
            @PathVariable("versionCode") String versionCode,
            @PathVariable("icsCode") String icsCode,
            @PathVariable("addlSoftwareCode") String addlSoftwareCode,
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException  {

        String chplProductNumber =
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                        versionCode, icsCode, addlSoftwareCode, certDateCode);

        CertificationResults results =
                new CertificationResults(cpdManager.getCertifiedProductCertificationResults(chplProductNumber));

        return results;
    }

    @ApiOperation(value = "Get all of the certification results for a specified certified product based on a legacy "
            + "CHPL Product Number.",
            notes = "Returns all of the certification results in the CHPL related to the specified certified product.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to this "
                    + "service would look like /certified_products/CHP-999999/certification_results.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}/certification_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertificationResults getCertificationResultsByCertifiedProductId(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException  {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);

        CertificationResults results =
                new CertificationResults(cpdManager.getCertifiedProductCertificationResults(chplProductNumber));

        return results;
    }

    /**
     * Download all SED details that are certified to 170.315(g)(3).
     * @param response http response
     * @throws EntityRetrievalException if cannot retrieve entity
     * @throws IOException if IO Exception
     */
    @ApiOperation(value = "Download all SED details that are certified to 170.315(g)(3).",
            notes = "Download a specific file that is generated overnight.")
    @RequestMapping(value = "/sed_details", method = RequestMethod.GET)
    public void streamSEDDetailsDocumentContents(HttpServletResponse response)
            throws EntityRetrievalException, IOException {
        File downloadFile = fileUtils.getNewestFileMatchingName("^" + env.getProperty("SEDDownloadName") + "-.+\\.csv$");
        fileUtils.streamFileAsResponse(downloadFile, "text/csv", response);
    }

    /**
     * Get the ICS family tree for the specified certified product.
     * @param certifiedProductId specified product
     * @return list of ICS Family Tree nodes
     * @throws EntityRetrievalException if cannot retrieve entity
     */
    @ApiOperation(value = "Get the ICS family tree for the specified certified product.",
            notes = "Returns all member of the family tree connected to the specified certified product.")
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/ics_relationships", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody List<IcsFamilyTreeNode> getIcsFamilyTreeById(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {
        List<IcsFamilyTreeNode> familyTree = cpManager.getIcsFamilyTree(certifiedProductId);

        return familyTree;
    }

    @ApiOperation(value = "Get the ICS family tree for the specified certified product based on a CHPL Product Number.",
            notes = "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
                    + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number.  A valid call to this "
                    + "service would look like /certified_products/YY.99.99.9999.XXXX.99.99.9."
                    + "YYMMDD/ics_relationships.")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
            + "{addlSoftwareCode}.{certDateCode}/ics_relationships",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody List<IcsFamilyTreeNode> getIcsFamilyTreeByChplProductNumber(
            @PathVariable("year") String year,
            @PathVariable("testingLab") String testingLab,
            @PathVariable("certBody") String certBody,
            @PathVariable("vendorCode") String vendorCode,
            @PathVariable("productCode") String productCode,
            @PathVariable("versionCode") String versionCode,
            @PathVariable("icsCode") String icsCode,
            @PathVariable("addlSoftwareCode") String addlSoftwareCode,
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException  {

        String chplProductNumber =
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                        versionCode, icsCode, addlSoftwareCode, certDateCode);

        List<IcsFamilyTreeNode> familyTree = cpManager.getIcsFamilyTree(chplProductNumber);

        return familyTree;
    }

    @ApiOperation(value = "Get the ICS family tree for the specified certified product based on a legacy CHPL Product Number",
            notes = "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to this "
                    + "service would look like /certified_products/CHP-999999/ics_relationships.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}/ics_relationships", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody List<IcsFamilyTreeNode> getIcsFamilyTreeByChplProductNumber(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException  {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        List<IcsFamilyTreeNode> familyTree = cpManager.getIcsFamilyTree(chplProductNumber);

        return familyTree;
    }

    @ApiOperation(value = "Update an existing certified product.",
            notes = "Updates the certified product after first validating the request. If a different "
                    + "ACB is passed in as part of the request, an ownership change will take place.  "
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and have administrative "
                    + "authority on the ACB that certified the product.")
    @RequestMapping(value = "/{certifiedProductId}", method = RequestMethod.PUT,
    produces = "application/json; charset=utf-8")
    public ResponseEntity<CertifiedProductSearchDetails> updateCertifiedProduct(
            @RequestBody(required = true) ListingUpdateRequest updateRequest)
                    throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
                    JsonProcessingException, IOException, ValidationException, MissingReasonException {


        return update(updateRequest);
    }

    private ResponseEntity<CertifiedProductSearchDetails> update(ListingUpdateRequest updateRequest)
            throws EntityCreationException, EntityRetrievalException, InvalidArgumentsException,
            JsonProcessingException, IOException, ValidationException, MissingReasonException {

        CertifiedProductSearchDetails updatedListing = updateRequest.getListing();

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(updatedListing.getId());
        Long acbId = Long.parseLong(existingListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString());

        // if the ACB owner is changed this is a separate action with different security
        Long newAcbId = Long
                .valueOf(updatedListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString());
        if (acbId.longValue() != newAcbId.longValue()) {
            cpManager.changeOwnership(updatedListing.getId(), newAcbId);
            CertifiedProductSearchDetails changedProduct = cpdManager.getCertifiedProductDetails(updatedListing.getId());
            activityManager.addActivity(
                    ActivityConcept.CERTIFIED_PRODUCT,
                    existingListing.getId(),
                    "Changed ACB ownership.",
                    existingListing,
                    changedProduct);
            existingListing = changedProduct;
        }

        cpManager.update(updateRequest);

        CertifiedProductSearchDetails changedProduct = cpdManager.getCertifiedProductDetails(updatedListing.getId());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        if (!changedProduct.getChplProductNumber().equals(existingListing.getChplProductNumber())) {
            responseHeaders.set("CHPL-Id-Changed", existingListing.getChplProductNumber());
        }
        return new ResponseEntity<CertifiedProductSearchDetails>(changedProduct, responseHeaders, HttpStatus.OK);
    }

    /**
     * Get metadata for all pending listing that the user has access to.
     * @return list of pending listing metadata.
     * @throws AccessDeniedException if user doesn't have access
     */
    @ApiOperation(value = "Get metadata for all pending listings the user has access to.",
            notes = "Pending listings are created via CSV file upload and are left in the 'pending' state "
                    + " until validated and confirmed.  Security Restrictions: ROLE_ADMIN, ROLE_ACB and have "
                    + "administrative authority on the ACB that uploaded the product.")
    @RequestMapping(value = "/pending/metadata", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<PendingCertifiedProductMetadata> getPendingCertifiedProductMetadata()
            throws AccessDeniedException {

        List<PendingCertifiedProductMetadataDTO> metadataDtos = pcpManager.getAllPendingCertifiedProductMetadata();

        List<PendingCertifiedProductMetadata> result = new ArrayList<PendingCertifiedProductMetadata>();
        for (PendingCertifiedProductMetadataDTO metadataDto : metadataDtos) {
            result.add(new PendingCertifiedProductMetadata(metadataDto));
        }
        return result;
    }

    /**
     * Get all pending Certified Products.
     * @return list of pending Listings
     * @throws EntityRetrievalException if cannot retrieve entity
     * @throws AccessDeniedException if user doesn't have access
     */
    @Deprecated
    @ApiOperation(value = "DEPRECATED. List pending certified products.",
    notes = "Pending certified products are created via CSV file upload and are left in the 'pending' state "
            + " until validated and approved.  Security Restrictions: ROLE_ADMIN, ROLE_ACB and have "
            + "administrative authority on the ACB that uploaded the product.")
    @RequestMapping(value = "/pending", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody PendingCertifiedProductResults getPendingCertifiedProducts()
            throws EntityRetrievalException, AccessDeniedException {

        List<PendingCertifiedProductDTO> pcps = new ArrayList<PendingCertifiedProductDTO>();
        if (resourcePermissions.isUserRoleAdmin()) {
            pcps = pcpManager.getAllPendingCertifiedProducts();
        } else if (resourcePermissions.isUserRoleAcbAdmin()) {
            List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
            for (CertificationBodyDTO acb : allowedAcbs) {
                pcps.addAll(pcpManager.getPendingCertifiedProducts(acb.getId()));
            }
        } else {
            throw new AccessDeniedException(msgUtil.getMessage("access.denied"));
        }

        List<PendingCertifiedProductDetails> result = new ArrayList<PendingCertifiedProductDetails>();
        for (PendingCertifiedProductDTO product : pcps) {
            PendingCertifiedProductDetails pcpDetails = new PendingCertifiedProductDetails(product);
            pcpManager.addAllVersionsToCmsCriterion(pcpDetails);
            pcpManager.addAllMeasuresToCertificationCriteria(pcpDetails);
            pcpManager.addAvailableTestFunctionalities(pcpDetails);
            result.add(pcpDetails);
        }
        PendingCertifiedProductResults results = new PendingCertifiedProductResults();
        results.getPendingCertifiedProducts().addAll(result);
        return results;
    }

    /**
     * Get a specific pending Listing.
     * @param pcpId the listing's id
     * @return the pending listing
     * @throws EntityRetrievalException if entity could not be retrieved
     * @throws EntityNotFoundException if entity could not be found
     * @throws AccessDeniedException if user does not have access to listing
     * @throws ObjectMissingValidationException if validation is missing
     */
    @ApiOperation(value = "List a specific pending certified product.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ACB and administrative authority "
                    + "on the ACB for each pending certified product is required.")
    @RequestMapping(value = "/pending/{pcpId}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody PendingCertifiedProductDetails getPendingCertifiedProductById(
            @PathVariable("pcpId") Long pcpId) throws EntityRetrievalException, EntityNotFoundException,
    AccessDeniedException, ObjectMissingValidationException {
        PendingCertifiedProductDetails details = pcpManager.getById(pcpId);
        if (details == null) {
            throw new EntityNotFoundException(msgUtil.getMessage("pendingListing.notFound"));
        } else {
            //make sure the user has permissions on the pending listings acb
            //will throw access denied if they do not have the permissions
            Long pendingListingAcbId =
                    new Long(details.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString());
            resourcePermissions.getAcbIfPermissionById(pendingListingAcbId);
        }
        return details;
    }

    @ApiOperation(value = "Reject a pending certified product.",
            notes = "Essentially deletes a pending certified product. Security Restrictions: ROLE_ADMIN or have ROLE_ACB "
                    + "and administrative authority on the ACB for each pending certified product is required.")
    @RequestMapping(value = "/pending/{pcpId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody String rejectPendingCertifiedProduct(@PathVariable("pcpId") Long pcpId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, ObjectMissingValidationException {
        pcpManager.deletePendingCertifiedProduct(pcpId);
        return "{\"success\" : true}";
    }

    @ApiOperation(value = "Reject several pending certified products.",
            notes = "Marks a list of pending certified products as deleted. ROLE_ADMIN or ROLE_ACB "
                    + " and administrative authority on the ACB for each pending certified product is required.")
    @RequestMapping(value = "/pending", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody String rejectPendingCertifiedProducts(@RequestBody IdListContainer idList)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, InvalidArgumentsException, ObjectsMissingValidationException {

        if (idList == null || idList.getIds() == null || idList.getIds().size() == 0) {
            throw new InvalidArgumentsException("At least one id must be provided for rejection.");
        }

        ObjectsMissingValidationException possibleExceptions = new ObjectsMissingValidationException();
        for (Long pcpId : idList.getIds()) {
            try {
                pcpManager.deletePendingCertifiedProduct(pcpId);
            } catch (ObjectMissingValidationException ex) {
                possibleExceptions.getExceptions().add(ex);
            }
        }

        if (possibleExceptions.getExceptions() != null && possibleExceptions.getExceptions().size() > 0) {
            throw possibleExceptions;
        }
        return "{\"success\" : true}";
    }

    //TODO - We might want to take a look at reworking this.  Maybe should be a PUT and the parameters
    //should be re-evaluated
    @ApiOperation(value = "Confirm a pending certified product.",
            notes = "Creates a new certified product in the system based on all of the information "
                    + "passed in on the request. This information may differ from what was previously "
                    + "entered for the pending certified product during upload. It will first be validated "
                    + "to check for errors, then a new certified product is created, and the old pending certified"
                    + "product will be removed. Security Restrictions:  ROLE_ADMIN or have ROLE_ACB and "
                    + "administrative authority on the ACB for each pending certified product is required.")
    @RequestMapping(value = "/pending/{pcpId}/confirm", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    public synchronized ResponseEntity<CertifiedProductSearchDetails> confirmPendingCertifiedProduct(
            @RequestBody(required = true) PendingCertifiedProductDetails pendingCp)
                    throws InvalidArgumentsException, ValidationException,
                    EntityCreationException, EntityRetrievalException,
                    ObjectMissingValidationException, IOException {

        return addPendingCertifiedProduct(pendingCp);
    }

    private synchronized ResponseEntity<CertifiedProductSearchDetails> addPendingCertifiedProduct(
            PendingCertifiedProductDetails pendingCp) throws InvalidArgumentsException, ValidationException,
    EntityCreationException, EntityRetrievalException, ObjectMissingValidationException,
    IOException {

        String acbIdStr = pendingCp.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString();
        if (StringUtils.isEmpty(acbIdStr)) {
            throw new InvalidArgumentsException("An ACB ID must be supplied in the request body");
        }
        Long acbId = Long.valueOf(acbIdStr);
        if (pcpManager.isPendingListingAvailableForUpdate(acbId, pendingCp.getId())) {
            PendingCertifiedProductDTO pcpDto = new PendingCertifiedProductDTO(pendingCp);
            PendingValidator validator = validatorFactory.getValidator(pcpDto);
            if (validator != null) {
                validator.validate(pcpDto, false);
            }
            if (pcpDto.getErrorMessages() != null && pcpDto.getErrorMessages().size() > 0) {
                throw new ValidationException(pcpDto.getErrorMessages(), pcpDto.getWarningMessages());
            }

            developerManager.validateDeveloperInSystemIfExists(pendingCp);

            CertifiedProductDTO createdProduct = cpManager.createFromPending(pcpDto);
            pcpManager.confirm(acbId, pendingCp.getId());
            CertifiedProductSearchDetails result = cpdManager.getCertifiedProductDetails(createdProduct.getId());
            activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, result.getId(),
                    "Created a certified product", null, result);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
            return new ResponseEntity<CertifiedProductSearchDetails>(result, responseHeaders, HttpStatus.OK);
        }
        return null;
    }

    /**
     * Upload a file with certified products.
     * @param file the file
     * @return the list of pending listings
     * @throws ValidationException if validation fails
     * @throws MaxUploadSizeExceededException if the file is too large
     */
    @ApiOperation(value = "Upload a file with certified products",
            notes = "Accepts a CSV file with very specific fields to create pending certified products. "
                    + "Security Restrictions: ROLE_ADMIN or user uploading the file must have ROLE_ACB "
                    + "and administrative authority on the ACB(s) specified in the file.")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity<PendingCertifiedProductResults> upload(@RequestParam("file") MultipartFile file)
            throws ValidationException, JsonProcessingException, InvalidArgumentsException, MaxUploadSizeExceededException {
        List<PendingCertifiedProductEntity> listingsToAdd = new ArrayList<PendingCertifiedProductEntity>();
        Set<String> errorMessages = new HashSet<String>();
        HttpHeaders responseHeaders = new HttpHeaders();
        try {
            listingsToAdd = uploadManager.parseListingsFromFile(file);
        } catch (DeprecatedUploadTemplateException dep) {
            responseHeaders.set(
                    HttpHeaders.WARNING, "299 - \"Deprecated upload template\"");
        } catch (ValidationException e) {
            errorMessages.addAll(e.getErrorMessages());
        } catch (InvalidArgumentsException | JsonProcessingException e) {
            errorMessages.add(e.getMessage());
        }
        if (errorMessages.size() > 0) {
            throw new ValidationException(errorMessages);
        }

        List<PendingCertifiedProductDetails> uploadedListings = new ArrayList<PendingCertifiedProductDetails>();
        for (PendingCertifiedProductEntity listingToAdd : listingsToAdd) {
            try {
                PendingCertifiedProductDTO pendingCpDto = pcpManager.createOrReplace(listingToAdd);
                PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(pendingCpDto);
                uploadedListings.add(details);
            } catch (EntityCreationException | EntityRetrievalException ex) {
                String error = "Error creating pending certified product " + listingToAdd.getUniqueId()
                + ". Error was: " + ex.getMessage();
                LOGGER.error(error);
                //send an email that something weird happened
                sendUploadError(file, ex);
                throw new ValidationException(error);
            }
        }

        PendingCertifiedProductResults results = new PendingCertifiedProductResults();
        results.getPendingCertifiedProducts().addAll(uploadedListings);
        return new ResponseEntity<PendingCertifiedProductResults>(results, responseHeaders, HttpStatus.OK);
    }

    /**
     * Creates an email message to the configured recipients
     * with configured subject and uses the stack trace as the
     * email body. Creates a temporary file that is the uploaded
     * CSV and attaches it to the email.
     * @param file
     * @param ex
     */
    private void sendUploadError(MultipartFile file, Exception ex) {
        if (StringUtils.isEmpty(uploadErrorEmailRecipients)) {
            return;
        }
        List<String> recipients = Arrays.asList(uploadErrorEmailRecipients.split(","));

        //figure out the filename for the attachment
        String originalFilename = file.getOriginalFilename();
        int indexOfExtension = originalFilename.indexOf(".");
        String filenameWithoutExtension = file.getOriginalFilename();
        if (indexOfExtension >= 0) {
            filenameWithoutExtension
            = originalFilename.substring(0, indexOfExtension);
        }
        String extension = ".csv";
        if (indexOfExtension >= 0) {
            extension = originalFilename.substring(indexOfExtension);
        }

        //attach the file the user tried to upload
        File temp = null;
        List<File> attachments = null;
        try {
            temp = File.createTempFile(filenameWithoutExtension, extension);
            file.transferTo(temp);
            attachments = new ArrayList<File>();
            attachments.add(temp);
        } catch (IOException io) {
            LOGGER.error("Could not create temporary file for attachment: " + io.getMessage(), io);
        }

        //create the email body
        String htmlBody = "<p>Upload attempted at " + new Date()
                + "<br/>Uploaded by " + AuthUtil.getUsername() + "</p>";
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        htmlBody += "<pre>" + writer.toString() + "</pre>";

        //build and send the email
        try {
            EmailBuilder emailBuilder = new EmailBuilder(env);
            emailBuilder.recipients(recipients)
            .subject(uploadErrorEmailSubject)
            .fileAttachments(attachments)
            .htmlMessage(htmlBody)
            .sendEmail();
        } catch (MessagingException msgEx) {
            LOGGER.error("Could not send email about failed listing upload.", msgEx);
        }
    }

    private CertifiedProductSearchDetails validateCertifiedProduct(
            CertifiedProductSearchDetails certifiedProduct) {
        Validator validator = validatorFactory.getValidator(certifiedProduct);
        if (validator != null) {
            validator.validate(certifiedProduct);
        }

        return certifiedProduct;
    }

    private static Function<CertifiedProductSearchDetails, CertifiedProductSearchBasicDetails> mapCertifiedProductDetailsToBasic = (CertifiedProductSearchDetails e)-> {
        CertifiedProductSearchBasicDetails certifiedProductSearchBasicDetails = new CertifiedProductSearchBasicDetails();
        certifiedProductSearchBasicDetails.setAcbCertificationId(e.getAcbCertificationId());
        certifiedProductSearchBasicDetails.setAccessibilityCertified(e.getAccessibilityCertified());
        certifiedProductSearchBasicDetails.setAccessibilityStandards(e.getAccessibilityStandards());
        certifiedProductSearchBasicDetails.setCertificationDate(e.getCertificationDate());
        certifiedProductSearchBasicDetails.setCertificationEdition(e.getCertificationEdition());
        certifiedProductSearchBasicDetails.setCertificationEvents(e.getCertificationEvents());
        certifiedProductSearchBasicDetails.setCertificationStatus(e.getCertificationStatus());
        certifiedProductSearchBasicDetails.setCertifyingBody(e.getCertifyingBody());
        certifiedProductSearchBasicDetails.setChplProductNumber(e.getChplProductNumber());
        certifiedProductSearchBasicDetails.setClassificationType(e.getClassificationType());
        certifiedProductSearchBasicDetails.setCountCerts(e.getCountCerts());
        certifiedProductSearchBasicDetails.setCountClosedNonconformities(e.getCountClosedNonconformities());
        certifiedProductSearchBasicDetails.setCountClosedSurveillance(e.getCountClosedSurveillance());
        certifiedProductSearchBasicDetails.setCountCqms(e.getCountCqms());
        certifiedProductSearchBasicDetails.setCountOpenNonconformities(e.getCountOpenNonconformities());
        certifiedProductSearchBasicDetails.setCountOpenSurveillance(e.getCountOpenSurveillance());
        certifiedProductSearchBasicDetails.setCountSurveillance(e.getCountSurveillance());
        certifiedProductSearchBasicDetails.setDecertificationDate(e.getDecertificationDate());
        certifiedProductSearchBasicDetails.setDeveloper(e.getDeveloper());
        certifiedProductSearchBasicDetails.setIcs(e.getIcs());
        certifiedProductSearchBasicDetails.setId(e.getId());
        certifiedProductSearchBasicDetails.setLastModifiedDate(e.getLastModifiedDate());
        certifiedProductSearchBasicDetails.setMeaningfulUseUserHistory(e.getMeaningfulUseUserHistory());
        certifiedProductSearchBasicDetails.setOtherAcb(e.getOtherAcb());
        certifiedProductSearchBasicDetails.setPracticeType(e.getPracticeType());
        certifiedProductSearchBasicDetails.setProduct(e.getProduct());
        certifiedProductSearchBasicDetails.setProductAdditionalSoftware(e.getProductAdditionalSoftware());
        certifiedProductSearchBasicDetails.setQmsStandards(e.getQmsStandards());
        certifiedProductSearchBasicDetails.setReportFileLocation(e.getReportFileLocation());
        certifiedProductSearchBasicDetails.setSed(e.getSed());
        certifiedProductSearchBasicDetails.setSedIntendedUserDescription(e.getSedIntendedUserDescription());
        certifiedProductSearchBasicDetails.setSedReportFileLocation(e.getSedReportFileLocation());
        certifiedProductSearchBasicDetails.setSedTestingEndDate(e.getSedTestingEndDate());
        certifiedProductSearchBasicDetails.setSurveillance(e.getSurveillance());
        certifiedProductSearchBasicDetails.setTargetedUsers(e.getTargetedUsers());
        certifiedProductSearchBasicDetails.setTestingLabs(e.getTestingLabs());
        certifiedProductSearchBasicDetails.setTransparencyAttestation(e.getTransparencyAttestation());
        certifiedProductSearchBasicDetails.setTransparencyAttestationUrl(e.getTransparencyAttestationUrl());
        certifiedProductSearchBasicDetails.setVersion(e.getVersion());
        return certifiedProductSearchBasicDetails;
    };
}
