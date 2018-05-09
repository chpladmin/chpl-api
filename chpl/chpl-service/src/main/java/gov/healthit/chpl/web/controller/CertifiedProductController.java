package gov.healthit.chpl.web.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.IdListContainer;
import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.upload.certifiedProduct.CertifiedProductUploadHandler;
import gov.healthit.chpl.upload.certifiedProduct.CertifiedProductUploadHandlerFactory;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.validation.certifiedProduct.CertifiedProductValidator;
import gov.healthit.chpl.validation.certifiedProduct.CertifiedProductValidatorFactory;
import gov.healthit.chpl.web.controller.exception.MissingReasonException;
import gov.healthit.chpl.web.controller.exception.ObjectMissingValidationException;
import gov.healthit.chpl.web.controller.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.web.controller.exception.ValidationException;
import gov.healthit.chpl.web.controller.results.CQMResultDetailResults;
import gov.healthit.chpl.web.controller.results.CertificationResults;
import gov.healthit.chpl.web.controller.results.PendingCertifiedProductResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Certified Product Controller.
 * @author alarned
 *
 */
@Api(value = "certified-products")
@RestController
@RequestMapping("/certified_products")
public class CertifiedProductController {

    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductController.class);

    @Autowired
    private CertifiedProductUploadHandlerFactory uploadHandlerFactory;

    @Autowired
    private CertifiedProductDetailsManager cpdManager;
    
    @Autowired
    private CertifiedProductManager cpManager;
    
    @Autowired
    private PendingCertifiedProductManager pcpManager;
    
    @Autowired
    private CertificationBodyManager acbManager;
    
    @Autowired
    private ActivityManager activityManager;
    
    @Autowired
    private CertifiedProductValidatorFactory validatorFactory;
    
    @Autowired
    private MeaningfulUseController meaningfulUseController;
    
    @Autowired
    private Environment env;
    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private ChplProductNumberUtil chplProductNumberUtil;

    /**
     * List all certified products.
     * @param versionId if entered, filters list to only listings under given version
     * @param editable if true, returns only those user has ability to edit
     * @return list of certified products
     * @throws EntityRetrievalException if unable to retrieve entity
     */
    @ApiOperation(value = "List all certified products",
            notes = "Default behavior is to return all certified products in the system. "
                    + " The optional 'versionId' parameter filters the certified products to those"
                    + " assigned to that version. The 'editable' parameter will return only those"
                    + " certified products that the logged in user has permission to edit as "
                    + " determined by ACB roles and authorities. Not all information about "
                    + " every certified product is returned. Call the /details service for more information.")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertifiedProduct> getCertifiedProductsByVersion(
            @RequestParam(required = true) final Long versionId,
            @RequestParam(required = false, defaultValue = "false") final boolean editable)
                    throws EntityRetrievalException {
        List<CertifiedProductDetailsDTO> certifiedProductList = null;

        if (editable) {
            certifiedProductList = cpManager.getByVersionWithEditPermission(versionId);
        } else {
            certifiedProductList = cpManager.getByVersion(versionId);
        }

        List<CertifiedProduct> products = new ArrayList<CertifiedProduct>();
        if (certifiedProductList != null && certifiedProductList.size() > 0) {
            for (CertifiedProductDetailsDTO dto : certifiedProductList) {
                CertifiedProduct result = new CertifiedProduct(dto);
                products.add(result);
            }
        }
        return products;
    }

    /**
     * Get all details for a specified certified product.
     * @param certifiedProductId database id of listing
     * @return Listing Details domain object
     * @throws EntityRetrievalException if cannot retrieve Listing
     */
    @ApiOperation(value = "Get all details for a specified certified product.",
            notes = "Returns all information in the CHPL related to the specified certified product.")
    @RequestMapping(value = "/{certifiedProductId:^([+-]?[1-9]\\d*|0)$}/details", 
                    method = RequestMethod.GET,
                    produces = "application/json; charset=utf-8")
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductById(
            @PathVariable("certifiedProductId") final Long certifiedProductId) throws EntityRetrievalException {
        
        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetails(Long.valueOf(certifiedProductId));
        certifiedProduct = validateCertifiedProduct(certifiedProduct);
        
        return certifiedProduct;
    }
    
    @ApiOperation(value = "Get all details for a specified certified product.",
            notes = "Returns all information in the CHPL related to the specified certified product.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode} " 
                    + "represents a valid CHPL Product Number.  A valid call to this service would look like "
                    + "/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/details")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}/details",
                    method = RequestMethod.GET,
                    produces = "application/json; charset=utf-8")
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductByChplProductNumber(
            @PathVariable("year") final String year,
            @PathVariable("testingLab") final String testingLab,
            @PathVariable("certBody") final String certBody,
            @PathVariable("vendorCode") final String vendorCode,
            @PathVariable("productCode") final String productCode,
            @PathVariable("versionCode") final String versionCode,
            @PathVariable("icsCode") final String icsCode,
            @PathVariable("addlSoftwareCode") final String addlSoftwareCode,
            @PathVariable("certDateCode") final String certDateCode) throws EntityRetrievalException  {
        
        String chplProductNumber = 
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode, 
                        versionCode, icsCode, addlSoftwareCode, certDateCode);
        
        CertifiedProductSearchDetails certifiedProduct = 
                cpdManager.getCertifiedProductDetailsByChplProductNumber(chplProductNumber.toString());
        
        CertifiedProductValidator validator = validatorFactory.getValidator(certifiedProduct);  
        if (validator != null) {
            validator.validate(certifiedProduct);
        }

        return certifiedProduct;
    }
    
    @ApiOperation(value = "Get all details for a specified certified product.",
            notes = "Returns all information in the CHPL related to the specified certified product.  "                    
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to this service would "
                    + "look like /certified_products/CHP-999999.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}/details",
                    method = RequestMethod.GET,
                    produces = "application/json; charset=utf-8")
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductByChplProductNumber2(
            @PathVariable("chplPrefix") final String chplPrefix,
            @PathVariable("identifier") final String identifier) throws EntityRetrievalException {
        
        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        
        CertifiedProductSearchDetails certifiedProduct = 
                cpdManager.getCertifiedProductDetailsByChplProductNumber(chplProductNumber.toString());
        
        certifiedProduct = validateCertifiedProduct(certifiedProduct);

        return certifiedProduct;
    }
    
    
    @ApiOperation(value = "Get all basic information for a specified certified product.  Does not include "
                            + "the CQM results and certification results.",
            notes = "Returns basic information in the CHPL related to the specified certified product.  "
                    + "The results will not include the CQM results and certification results.")
    @RequestMapping(value = "/{certifiedProductId:^([+-]?[1-9]\\d*|0)$}", 
                    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductByIdBasic(
            @PathVariable("certifiedProductId") final Long certifiedProductId) throws EntityRetrievalException {

        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetailsBasic(certifiedProductId);
        certifiedProduct = validateCertifiedProduct(certifiedProduct);

        return certifiedProduct;
    }

    @ApiOperation(value = "Get all basic information for a specified certified product.  Does not include "
                        + "the CQM results and certification results.",
            notes = "Returns basic information in the CHPL related to the specified certified product.  "
                        + "The results will not include the CQM results and certification results.  "
                        + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode} " 
                        + "represents a valid CHPL Product Number.  A valid call to this service would look like "
                        + "/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD.")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}",
                    method = RequestMethod.GET,
                    produces = "application/json; charset=utf-8")
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductByChplProductNumberBasic(
            @PathVariable("year") final String year,
            @PathVariable("testingLab") final String testingLab,
            @PathVariable("certBody") final String certBody,
            @PathVariable("vendorCode") final String vendorCode,
            @PathVariable("productCode") final String productCode,
            @PathVariable("versionCode") final String versionCode,
            @PathVariable("icsCode") final String icsCode,
            @PathVariable("addlSoftwareCode") final String addlSoftwareCode,
            @PathVariable("certDateCode") final String certDateCode) throws EntityRetrievalException  {
        
        
        String chplProductNumber = 
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode, 
                        versionCode, icsCode, addlSoftwareCode, certDateCode);
        
        CertifiedProductSearchDetails certifiedProduct = 
                cpdManager.getCertifiedProductDetailsBasicByChplProductNumber(chplProductNumber.toString());
        
        certifiedProduct = validateCertifiedProduct(certifiedProduct);

        return certifiedProduct;
    }
    
    @ApiOperation(value = "Get all basic information for a specified certified product.  Does not include "
                        + "the CQM results and certification results.",
            notes = "Returns basic information in the CHPL related to the specified certified product.  "
                    + "The results will not include the CQM results and certification results.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to this service would "
                    + "look like /certified_products/CHP-999999.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}",
                    method = RequestMethod.GET,
                    produces = "application/json; charset=utf-8")
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductByChplProductNumberBasic2(
            @PathVariable("chplPrefix") final String chplPrefix,
            @PathVariable("identifier") final String identifier) throws EntityRetrievalException {
        
        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        
        CertifiedProductSearchDetails certifiedProduct = 
                cpdManager.getCertifiedProductDetailsBasicByChplProductNumber(chplProductNumber.toString());
        
        certifiedProduct = validateCertifiedProduct(certifiedProduct);

        return certifiedProduct;
    }
        
    @ApiOperation(value = "Get all of the CQM results for a specified certified product.",
            notes = "Returns all of the CQM results in the CHPL related to the specified certified product.")
    @RequestMapping(value = "/{certifiedProductId:^([+-]?[1-9]\\d*|0)$}/cqm_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody CQMResultDetailResults getCqmsByCertifiedProductId(
            @PathVariable("certifiedProductId") final Long certifiedProductId) throws EntityRetrievalException {

        CQMResultDetailResults results =
                new CQMResultDetailResults(cpdManager.getCertifiedProductCqms(certifiedProductId));

        return results;
    }

    @ApiOperation(value = "Get all of the CQM results for a specified certified product.",
            notes = "Returns all of the CQM results in the CHPL related to the specified certified product.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode} " 
                    + "represents a valid CHPL Product Number.  A valid call to this service would look like "
                    + "/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/cqm_results.")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}/cqm_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody CQMResultDetailResults getCqmsByCertifiedProductId(
            @PathVariable("year") final String year,
            @PathVariable("testingLab") final String testingLab,
            @PathVariable("certBody") final String certBody,
            @PathVariable("vendorCode") final String vendorCode,
            @PathVariable("productCode") final String productCode,
            @PathVariable("versionCode") final String versionCode,
            @PathVariable("icsCode") final String icsCode,
            @PathVariable("addlSoftwareCode") final String addlSoftwareCode,
            @PathVariable("certDateCode") final String certDateCode) throws EntityRetrievalException  {

        String chplProductNumber = 
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode, 
                        versionCode, icsCode, addlSoftwareCode, certDateCode);
        CQMResultDetailResults results =
                new CQMResultDetailResults(cpdManager.getCertifiedProductCqms(chplProductNumber));

        return results;
    }

    @ApiOperation(value = "Get all of the CQM results for a specified certified product based on a legacy CHPL Product Number.",
            notes = "\"Returns all of the CQM results in the CHPL related to the specified certified product.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to this service would "
                    + "look like /certified_products/CHP-999999/cqm_results.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}/cqm_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody CQMResultDetailResults getCqmsByCertifiedProductId(
            @PathVariable("chplPrefix") final String chplPrefix,
            @PathVariable("identifier") final String identifier) throws EntityRetrievalException  {

        String chplProductNumber =  chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        CQMResultDetailResults results =
                new CQMResultDetailResults(cpdManager.getCertifiedProductCqms(chplProductNumber));

        return results;
    }
       
    @ApiOperation(value = "Get all of the certification results for a specified certified product.",
            notes = "Returns all of the certifiection results in the CHPL related to the specified certified product.")
    @RequestMapping(value = "/{certifiedProductId:^([+-]?[1-9]\\d*|0)$}/certification_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody CertificationResults getCertificationResultssByCertifiedProductId(
            @PathVariable("certifiedProductId") final Long certifiedProductId) throws EntityRetrievalException {

        CertificationResults results =
                new CertificationResults(cpdManager.getCertifiedProductCertificationResults(certifiedProductId));

        return results;
    }

  /**
     * Download all SED details that are certified to 170.315(g)(3).
     * @param response http response
     * @throws EntityRetrievalException if cannot retrieve entity
     * @throws IOException if IO Exception
     */
    @ApiOperation(value = "Get all of the certification results for a specified certified product based on a CHPL Product Number.",
            notes = "Returns all of the certifiection results in the CHPL related to the specified certified product.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode} " 
                    + "represents a valid CHPL Product Number.  A valid call to this service would look like "
                    + "/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/certification_results.")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}/certification_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody CertificationResults getCertificationResultssByCertifiedProductId(
            @PathVariable("year") final String year,
            @PathVariable("testingLab") final String testingLab,
            @PathVariable("certBody") final String certBody,
            @PathVariable("vendorCode") final String vendorCode,
            @PathVariable("productCode") final String productCode,
            @PathVariable("versionCode") final String versionCode,
            @PathVariable("icsCode") final String icsCode,
            @PathVariable("addlSoftwareCode") final String addlSoftwareCode,
            @PathVariable("certDateCode") final String certDateCode) throws EntityRetrievalException  {

        String chplProductNumber = 
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode, 
                        versionCode, icsCode, addlSoftwareCode, certDateCode);
        
        CertificationResults results =
                new CertificationResults(cpdManager.getCertifiedProductCertificationResults(chplProductNumber));

        return results;
    }

    @ApiOperation(value = "Get all of the certification results for a specified certified product based on a legacy CHPL Product Number.",
            notes = "Returns all of the certifiection results in the CHPL related to the specified certified product.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to this service would "
                    + "look like /certified_products/CHP-999999/certification_results.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}/certification_results", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody CertificationResults getCertificationResultssByCertifiedProductId(
            @PathVariable("chplPrefix") final String chplPrefix,
            @PathVariable("identifier") final String identifier) throws EntityRetrievalException  {

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
    public void streamSEDDetailsDocumentContents(final HttpServletResponse response)
            throws EntityRetrievalException, IOException {
        Path path = Paths.get(env.getProperty("downloadFolderPath"), env.getProperty("SEDDownloadName"));
        File downloadFile = new File(path.toUri());
        final int bufferSize = 1024;
        byte[] data = Files.readAllBytes(path);

        if (data != null && data.length > 0) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            // get MIME type of the file
            String mimeType = "text/csv";
            // set content attributes for the response
            response.setContentType(mimeType);
            response.setContentLength(data.length);

            // set headers for the response
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
            response.setHeader(headerKey, headerValue);

            // get output stream of the response
            OutputStream outStream = response.getOutputStream();

            byte[] buffer = new byte[bufferSize];
            int bytesRead = -1;

            // write bytes read from the input stream into the output stream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outStream.close();
        }
    }

    /**
     * Get the ICS family tree for the specified certified product.
     * @param certifiedProductId specified product
     * @return list of ICS Family Tree nodes
     * @throws EntityRetrievalException if cannot retrieve entity
     */
    @ApiOperation(value = "Get the ICS family tree for the specified certified product.",
            notes = "Returns all member of the family tree conected to the specified certified product.")
    @RequestMapping(value = "/{certifiedProductId:^([+-]?[1-9]\\d*|0)$}/ics_relationships", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody List<IcsFamilyTreeNode> getIcsFamilyTreeById(
            @PathVariable("certifiedProductId") final Long certifiedProductId) throws EntityRetrievalException {
        List<IcsFamilyTreeNode> familyTree = cpManager.getIcsFamilyTree(certifiedProductId);

        return familyTree;
    }

    @ApiOperation(value = "Get the ICS family tree for the specified certified product based on a CHPL Product Number.",
            notes = "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode} " 
                    + "represents a valid CHPL Product Number.  A valid call to this service would look like "
                    + "/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/ics_relationships.")
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}/ics_relationships", 
                    method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody List<IcsFamilyTreeNode> getIcsFamilyTreeByChplProductNumber(
            @PathVariable("year") final String year,
            @PathVariable("testingLab") final String testingLab,
            @PathVariable("certBody") final String certBody,
            @PathVariable("vendorCode") final String vendorCode,
            @PathVariable("productCode") final String productCode,
            @PathVariable("versionCode") final String versionCode,
            @PathVariable("icsCode") final String icsCode,
            @PathVariable("addlSoftwareCode") final String addlSoftwareCode,
            @PathVariable("certDateCode") final String certDateCode) throws EntityRetrievalException  {
        
        String chplProductNumber = 
                chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode, 
                        versionCode, icsCode, addlSoftwareCode, certDateCode); 
        
        List<IcsFamilyTreeNode> familyTree = cpManager.getIcsFamilyTree(chplProductNumber);

        return familyTree;
    }

    @ApiOperation(value = "Get the ICS family tree for the specified certified product based on a legacy CHPL Product Number",
            notes = "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to this service would "  
                    + "look like /certified_products/CHP-999999/ics_relationships.")
    @RequestMapping(value = "/{chplPrefix}-{identifier}/ics_relationships", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody List<IcsFamilyTreeNode> getIcsFamilyTreeByChplProductNumber(
            @PathVariable("chplPrefix") final String chplPrefix,
            @PathVariable("identifier") final String identifier) throws EntityRetrievalException  {
        
        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        List<IcsFamilyTreeNode> familyTree = cpManager.getIcsFamilyTree(chplProductNumber);

        return familyTree;
    }

	/**
     * Update an existing certified product.
     * @param updateRequest the update request
     * @return the updated listing
     * @throws EntityCreationException if cannot create entity
     * @throws EntityRetrievalException if cannot retrieve entity
     * @throws InvalidArgumentsException if invalid arguments
     * @throws JsonProcessingException if cannot parse JSON
     * @throws IOException if IO Exception
     * @throws ValidationException if invalid update
     * @throws MissingReasonException if missing reason for change
     */
    @ApiOperation(value = "Update an existing certified product.",
            notes = "Updates the certified product after first validating the request. The logged in"
                    + " user must have ROLE_ADMIN or ROLE_ACB and have administrative "
                    + " authority on the ACB that certified the product. If a different ACB is passed in"
                    + " as part of the request, an ownership change will take place and the logged in "
                    + " user must have ROLE_ADMIN.")
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity<CertifiedProductSearchDetails> updateCertifiedProduct(
            @RequestBody(required = true) final ListingUpdateRequest updateRequest) throws EntityCreationException,
    EntityRetrievalException, InvalidArgumentsException, JsonProcessingException,
    IOException, ValidationException, MissingReasonException {

        CertifiedProductSearchDetails updatedListing = updateRequest.getListing();

        // clean up what was sent in - some necessary IDs or other fields may be
        // missing
        Long newAcbId = new Long(updatedListing.getCertifyingBody().get("id").toString());
        cpManager.sanitizeUpdatedListingData(newAcbId, updatedListing);

        // validate
        CertifiedProductValidator validator = validatorFactory.getValidator(updatedListing);
        if (validator != null) {
            validator.validate(updatedListing);
        }

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(updatedListing.getId());

        // make sure the old and new certification statuses aren't ONC bans
        if (existingListing.getCurrentStatus() != null
                && updatedListing.getCurrentStatus() != null
                && !existingListing.getCurrentStatus().getStatus().getId()
                .equals(updatedListing.getCurrentStatus().getStatus().getId())) {
            // if the status is to or from suspended by onc make sure the user
            // has admin
            if ((existingListing.getCurrentStatus().getStatus().getName()
                    .equals(CertificationStatusType.SuspendedByOnc.toString())
                    || updatedListing.getCurrentStatus().getStatus().getName()
                    .equals(CertificationStatusType.SuspendedByOnc.toString())
                    || existingListing.getCurrentStatus().getStatus().getName()
                    .equals(CertificationStatusType.TerminatedByOnc.toString())
                    || updatedListing.getCurrentStatus().getStatus().getName()
                    .equals(CertificationStatusType.TerminatedByOnc.toString()))
                    && !Util.isUserRoleAdmin()) {
                updatedListing.getErrorMessages()
                .add("User " + Util.getUsername()
                + " does not have permission to change certification status of "
                + existingListing.getChplProductNumber() + " from "
                + existingListing.getCurrentStatus().getStatus().getName() + " to "
                + updatedListing.getCurrentStatus().getStatus().getName());
            }
        }

        // has the unique id changed? if so, make sure it is still unique
        if (!existingListing.getChplProductNumber().equals(updatedListing.getChplProductNumber())) {
            try {
                boolean isDup = cpManager.chplIdExists(updatedListing.getChplProductNumber());
                if (isDup) {
                    updatedListing.getErrorMessages()
                    .add(String
                            .format(messageSource.getMessage(
                                    new DefaultMessageSourceResolvable("listing.upload.changedNotUnique"),
                                    LocaleContextHolder.getLocale()), updatedListing.getChplProductNumber()));
                }
            } catch (final EntityRetrievalException ex) {
            }
        }

        if (updatedListing.getErrorMessages() != null && updatedListing.getErrorMessages().size() > 0) {
            throw new ValidationException(updatedListing.getErrorMessages(), updatedListing.getWarningMessages());
        }

        Long acbId = new Long(existingListing.getCertifyingBody().get("id").toString());

        // if the ACF owner is changed this is a separate action with different
        // security
        if (newAcbId != null && acbId.longValue() != newAcbId.longValue()) {
            cpManager.changeOwnership(updatedListing.getId(), newAcbId);
            CertifiedProductSearchDetails changedProduct = cpdManager
                    .getCertifiedProductDetails(updatedListing.getId());
            activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, existingListing.getId(),
                    "Changed ACB ownership.", existingListing, changedProduct);
            existingListing = changedProduct;
        }

        // update the listing
        cpManager.update(acbId, updateRequest, existingListing);

        // search for the product by id to get it with all the updates
        CertifiedProductSearchDetails changedProduct = cpdManager.getCertifiedProductDetails(updatedListing.getId());
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, existingListing.getId(),
                "Updated certified product " + changedProduct.getChplProductNumber() + ".", existingListing,
                changedProduct, updateRequest.getReason());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        if (!changedProduct.getChplProductNumber().equals(existingListing.getChplProductNumber())) {
            responseHeaders.set("CHPL-Id-Changed", existingListing.getChplProductNumber());
        }
        return new ResponseEntity<CertifiedProductSearchDetails>(changedProduct, responseHeaders, HttpStatus.OK);
    }

    /**
     * Get all pending Certified Products.
     * @return list of pending Listings
     * @throws EntityRetrievalException if cannot retrieve entity
     */
    @ApiOperation(value = "List pending certified products.",
            notes = "Pending certified products are created via CSV file upload and are left in the 'pending' state "
                    + " until validated and approved by an appropriate ACB administrator.")
    @RequestMapping(value = "/pending", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody PendingCertifiedProductResults getPendingCertifiedProducts() throws EntityRetrievalException {
        List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
        List<PendingCertifiedProductDTO> allProductDtos = new ArrayList<PendingCertifiedProductDTO>();

        if (acbs != null) {
            for (CertificationBodyDTO acb : acbs) {
                try {
                    List<PendingCertifiedProductDTO> pendingCpsByAcb = pcpManager
                            .getPendingCertifiedProductsByAcb(acb.getId());
                    allProductDtos.addAll(pendingCpsByAcb);
                } catch (final AccessDeniedException denied) {
                    LOGGER.warn("Access denied to pending certified products for acb " + acb.getName() + " and user "
                            + Util.getUsername());
                }
            }
        }

        List<PendingCertifiedProductDetails> result = new ArrayList<PendingCertifiedProductDetails>();
        for (PendingCertifiedProductDTO product : allProductDtos) {
            PendingCertifiedProductDetails pcpDetails = new PendingCertifiedProductDetails(product);
            pcpManager.addAllVersionsToCmsCriterion(pcpDetails);
            pcpManager.addAllMeasuresToCertificationCriteria(pcpDetails);
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
    @ApiOperation(value = "List a specific pending certified product.", notes = "")
    @RequestMapping(value = "/pending/{pcpId}", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody PendingCertifiedProductDetails getPendingCertifiedProductById(
            @PathVariable("pcpId") final Long pcpId) throws EntityRetrievalException, EntityNotFoundException,
    AccessDeniedException, ObjectMissingValidationException {
        List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
        PendingCertifiedProductDetails details = pcpManager.getById(acbs, pcpId);
        return details;
    }

    /**
     * Reject a pending Listing.
     * @param id the listing to reject
     * @return status of request
     * @throws EntityRetrievalException if entity can not be retrieved
     * @throws JsonProcessingException if JSON cannot be processed
     * @throws EntityCreationException if entity cannot be created
     * @throws EntityNotFoundException if entity not found
     * @throws AccessDeniedException if user does not have access
     * @throws ObjectMissingValidationException if missing validation
     */
    @ApiOperation(value = "Reject a pending certified product.",
            notes = "Essentially deletes a pending certified product. ROLE_ACB "
                    + " and administrative authority on the ACB is required.")
    @RequestMapping(value = "/pending/{pcpId}/reject", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    public @ResponseBody String rejectPendingCertifiedProduct(@PathVariable("pcpId") final Long id)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, ObjectMissingValidationException {
        List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
        pcpManager.deletePendingCertifiedProduct(acbs, id);
        return "{\"success\" : true}";
    }

    /**
     * Reject several pending listings.
     * @param idList list of ids
     * @return status of request
     * @throws EntityRetrievalException if entity can not be retrieved
     * @throws JsonProcessingException if JSON cannot be processed
     * @throws EntityCreationException if entity cannot be created
     * @throws EntityNotFoundException if entity not found
     * @throws AccessDeniedException if user does not have access
     * @throws InvalidArgumentsException if arguments are invalid
     * @throws ObjectsMissingValidationException if missing validation
     */
    @ApiOperation(value = "Reject several pending certified products.",
            notes = "Marks a list of pending certified products as deleted. ROLE_ACB "
                    + " and administrative authority on the ACB for each pending certified product is required.")
    @RequestMapping(value = "/pending/reject", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    public @ResponseBody String rejectPendingCertifiedProducts(@RequestBody final IdListContainer idList)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, InvalidArgumentsException, ObjectsMissingValidationException {
        if (idList == null || idList.getIds() == null || idList.getIds().size() == 0) {
            throw new InvalidArgumentsException("At least one id must be provided for rejection.");
        }

        ObjectsMissingValidationException possibleExceptions = new ObjectsMissingValidationException();
        List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
        for (Long id : idList.getIds()) {
            try {
                pcpManager.deletePendingCertifiedProduct(acbs, id);
            } catch (final ObjectMissingValidationException ex) {
                possibleExceptions.getExceptions().add(ex);
            }
        }

        if (possibleExceptions.getExceptions() != null && possibleExceptions.getExceptions().size() > 0) {
            throw possibleExceptions;
        }
        return "{\"success\" : true}";
    }

    /**
     * Confirm a pending listing.
     * @param pendingCp the listing's id
     * @return the created listing
     * @throws InvalidArgumentsException if arguments are invalid
     * @throws ValidationException if validation fails
     * @throws EntityCreationException if entity can not be created
     * @throws EntityRetrievalException if entity can not be retrieved
     * @throws ObjectMissingValidationException if object is missing validation
     * @throws IOException if IO Exception occurs
     */
    @ApiOperation(value = "Confirm a pending certified product.",
            notes = "Creates a new certified product in the system based on all of the information "
                    + " passed in on the request. This information may differ from what was previously "
                    + " entered for the pending certified product during upload. It will first be validated "
                    + " to check for errors, then a new certified product is created, and the old pending certified"
                    + " product will be removed. ROLE_ACB "
                    + " and administrative authority on the ACB is required.")
    @RequestMapping(value = "/pending/confirm", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    public synchronized ResponseEntity<CertifiedProductSearchDetails> confirmPendingCertifiedProduct(
            @RequestBody(required = true) final PendingCertifiedProductDetails pendingCp)
                    throws InvalidArgumentsException, ValidationException,
                    EntityCreationException, EntityRetrievalException,
                    ObjectMissingValidationException, IOException {
        String acbIdStr = pendingCp.getCertifyingBody().get("id").toString();
        if (StringUtils.isEmpty(acbIdStr)) {
            throw new InvalidArgumentsException("An ACB ID must be supplied in the request body");
        }
        Long acbId = new Long(acbIdStr);
        if (pcpManager.isPendingListingAvailableForUpdate(acbId, pendingCp.getId())) {
            PendingCertifiedProductDTO pcpDto = new PendingCertifiedProductDTO(pendingCp);
            CertifiedProductValidator validator = validatorFactory.getValidator(pcpDto);
            if (validator != null) {
                validator.validate(pcpDto);
            }
            if (pcpDto.getErrorMessages() != null && pcpDto.getErrorMessages().size() > 0) {
                throw new ValidationException(pcpDto.getErrorMessages(), pcpDto.getWarningMessages());
            }

            CertifiedProductDTO createdProduct = cpManager.createFromPending(acbId, pcpDto);
            pcpManager.confirm(acbId, pendingCp.getId());
            CertifiedProductSearchDetails result = cpdManager.getCertifiedProductDetails(createdProduct.getId());
            activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_CERTIFIED_PRODUCT, result.getId(),
                    "Created a certified product", null, result);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
            return new ResponseEntity<CertifiedProductSearchDetails>(result, responseHeaders, HttpStatus.OK);
        }
        return null;
    }

    /**
     * Upload a file to update number of MUU for each Listing.
     * @param file the file to upload
     * @return status of the action
     * @throws EntityCreationException if entity can not be created
     * @throws EntityRetrievalException if entity can not be retrieved
     * @throws ValidationException if validation fails
     * @throws MaxUploadSizeExceededException if file is too large
     */
    @ApiOperation(value = "DEPRECATED. Upload a file to update the number "
            + "of meaningful use users for each CHPL Product Number",
            notes = "Accepts a CSV file with chpl_product_number and num_meaningful_use_users "
                    + "to update the number of meaningful use users for each CHPL Product Number."
                    + " The user uploading the file must have ROLE_ADMIN or ROLE_ONC_STAFF ")
    @RequestMapping(value = "/meaningful_use_users/upload", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    @Deprecated
    public @ResponseBody ResponseEntity<Job> uploadMeaningfulUseUsers(@RequestParam("file") final MultipartFile file)
            throws EntityCreationException, EntityRetrievalException, ValidationException,
            MaxUploadSizeExceededException {
        return meaningfulUseController.uploadMeaningfulUseUsers(file);
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
                    + " The user uploading the file must have ROLE_ACB "
                    + " and administrative authority on the ACB(s) specified in the file.")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity<PendingCertifiedProductResults> upload(@RequestParam("file") final MultipartFile file)
            throws ValidationException, MaxUploadSizeExceededException {
        if (file.isEmpty()) {
            throw new ValidationException("You cannot upload an empty file!");
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException("File must be a CSV document.");
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        List<PendingCertifiedProductDetails> uploadedProducts = new ArrayList<PendingCertifiedProductDetails>();

        BufferedReader reader = null;
        CSVParser parser = null;
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            parser = new CSVParser(reader, CSVFormat.EXCEL);

            List<CSVRecord> records = parser.getRecords();
            if (records.size() <= 1) {
                throw new ValidationException(String
                        .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.upload.emptyRows"),
                                LocaleContextHolder.getLocale())));
            }

            Set<String> handlerErrors = new HashSet<String>();
            List<PendingCertifiedProductEntity> cpsToAdd = new ArrayList<PendingCertifiedProductEntity>();

            // parse the entire file into groups of records, one group per
            // product
            CSVRecord heading = null;
            Set<String> uniqueIdsFromFile = new HashSet<String>();
            Set<String> duplicateIdsFromFile = new HashSet<String>();
            List<CSVRecord> rows = new ArrayList<CSVRecord>();
            for (int i = 0; i < records.size(); i++) {
                CSVRecord currRecord = records.get(i);

                if (heading == null) {
                    heading = currRecord;
                } else {
                    if (!StringUtils.isEmpty(currRecord.get(0))) {
                        String currUniqueId = currRecord.get(0);
                        String currStatus = currRecord.get(1);

                        if (currStatus.equalsIgnoreCase("NEW")) {
                            if (!currUniqueId.contains("XXXX") && uniqueIdsFromFile.contains(currUniqueId)) {
                                handlerErrors.add("Multiple products with unique id " + currUniqueId
                                        + " were found in the file.");
                                duplicateIdsFromFile.add(currUniqueId);
                            } else {
                                uniqueIdsFromFile.add(currUniqueId);

                                // parse the previous recordset
                                if (rows.size() > 0) {
                                    try {
                                        CertifiedProductUploadHandler handler = uploadHandlerFactory.getHandler(heading,
                                                rows);
                                        if (handler.getUploadTemplateVersion() != null
                                                && handler.getUploadTemplateVersion().getDeprecated() == Boolean.TRUE) {
                                            responseHeaders.set(
                                                    HttpHeaders.WARNING, "299 - \"Deprecated upload template\"");
                                        }
                                        PendingCertifiedProductEntity pendingCp = handler.handle();
                                        cpsToAdd.add(pendingCp);
                                    } catch (final InvalidArgumentsException ex) {
                                        handlerErrors.add(ex.getMessage());
                                    }
                                }
                                rows.clear();
                            }
                        }

                        if (!duplicateIdsFromFile.contains(currUniqueId)) {
                            rows.add(currRecord);
                        }
                    }
                }

                // add the last object
                if (i == records.size() - 1 && !rows.isEmpty()) {
                    try {
                        CertifiedProductUploadHandler handler = uploadHandlerFactory.getHandler(heading, rows);
                        if (handler.getUploadTemplateVersion() != null
                                && handler.getUploadTemplateVersion().getDeprecated() == Boolean.TRUE) {
                            responseHeaders.set(HttpHeaders.WARNING, "299 - \"Deprecated upload template\"");
                        }
                        PendingCertifiedProductEntity pendingCp = handler.handle();
                        cpsToAdd.add(pendingCp);
                    } catch (final InvalidArgumentsException ex) {
                        handlerErrors.add(ex.getMessage());
                    } catch (final Exception ex) {
                        handlerErrors.add(ex.getMessage());
                    }
                }
            }

            if (handlerErrors.size() > 0) {
                throw new ValidationException(handlerErrors, null);
            }

            Set<String> allErrors = new HashSet<String>();
            for (PendingCertifiedProductEntity cpToAdd : cpsToAdd) {
                if (cpToAdd.getErrorMessages() != null && cpToAdd.getErrorMessages().size() > 0) {
                    allErrors.addAll(cpToAdd.getErrorMessages());
                }
            }
            if (allErrors.size() > 0) {
                throw new ValidationException(allErrors, null);
            } else {
                for (PendingCertifiedProductEntity cpToAdd : cpsToAdd) {
                    try {
                        PendingCertifiedProductDTO pendingCpDto = pcpManager
                                .createOrReplace(cpToAdd.getCertificationBodyId(), cpToAdd);
                        PendingCertifiedProductDetails details = new PendingCertifiedProductDetails(pendingCpDto);
                        uploadedProducts.add(details);
                    } catch (final EntityCreationException ex) {
                        String error = "Error creating pending certified product " + cpToAdd.getUniqueId()
                        + ". Error was: " + ex.getMessage();
                        LOGGER.error(error);
                        throw new ValidationException(error);
                    } catch (final EntityRetrievalException ex) {
                        LOGGER.error("Error retreiving pending certified product.", ex);
                    }
                }
            }
        } catch (final IOException ioEx) {
            LOGGER.error("Could not get input stream for uploaded file " + file.getName());
            throw new ValidationException(String
                    .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.upload.couldNotParse"),
                            LocaleContextHolder.getLocale()), file.getName()));
        } finally {
            try {
                parser.close();
            } catch (Exception ignore) {
            }
            try {
                reader.close();
            } catch (Exception ignore) {
            }
        }

        PendingCertifiedProductResults results = new PendingCertifiedProductResults();
        results.getPendingCertifiedProducts().addAll(uploadedProducts);
        return new ResponseEntity<PendingCertifiedProductResults>(results, responseHeaders, HttpStatus.OK);
    }
    
    private CertifiedProductSearchDetails validateCertifiedProduct(CertifiedProductSearchDetails certifiedProduct) {
        CertifiedProductValidator validator = validatorFactory.getValidator(certifiedProduct);  
        if (validator != null) {
            validator.validate(certifiedProduct);
        }
        
        return certifiedProduct;
    }
}
