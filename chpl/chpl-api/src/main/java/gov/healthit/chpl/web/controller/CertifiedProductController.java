package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.certifiedproduct.csv.ListingCsvWriter;
import gov.healthit.chpl.domain.CertifiedProductSearchBasicDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.CertifiedProductUpdateException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.listing.ics.IcsManager;
import gov.healthit.chpl.listing.ics.ListingIcsNode;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.Validator;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
import gov.healthit.chpl.web.controller.results.CQMResultDetailResults;
import gov.healthit.chpl.web.controller.results.CertificationResults;
import gov.healthit.chpl.web.controller.results.MeasureResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Tag(name = "certified-products", description = "Management of certified products.")
@RestController
@RequestMapping("/certified_products")
@Log4j2
public class CertifiedProductController {
    private static final String DOWNLOAD_FILE_FORMAT = "text/csv";

    private CertifiedProductDetailsManager cpdManager;
    private CertifiedProductManager cpManager;
    private IcsManager icsManager;
    private ListingCsvWriter listingCsvWriter;
    private ActivityManager activityManager;
    private ListingValidatorFactory validatorFactory;
    private ChplProductNumberUtil chplProductNumberUtil;
    private FileUtils fileUtils;

    @SuppressWarnings({
            "checkstyle:parameternumber"
    })
    @Autowired
    public CertifiedProductController(CertifiedProductDetailsManager cpdManager, CertifiedProductManager cpManager,
            IcsManager icsManager, ListingCsvWriter listingCsvWriter,
            ActivityManager activityManager, ListingValidatorFactory validatorFactory,
            ErrorMessageUtil msgUtil, ChplProductNumberUtil chplProductNumberUtil,
            FileUtils fileUtils) {
        this.cpdManager = cpdManager;
        this.cpManager = cpManager;
        this.icsManager = icsManager;
        this.listingCsvWriter = listingCsvWriter;
        this.activityManager = activityManager;
        this.validatorFactory = validatorFactory;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.fileUtils = fileUtils;
    }

    @Operation(summary = "Get all details for a specified certified product.",
            description = "Returns all information in the CHPL related to the specified certified product.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/details",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = CertifiedProductSearchDetails.class, friendlyUrl = "/certified_products/{certifiedProductId}/details")
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductById(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {

        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetails(certifiedProductId);
        if (certifiedProduct == null) {
            throw new EntityRetrievalException();
        }
        certifiedProduct = validateCertifiedProduct(certifiedProduct);
        return certifiedProduct;
    }

    @SuppressWarnings({
            "checkstyle:parameternumber"
    })
    @Operation(summary = "Get all details for a specified certified product.",
            description = "Returns all information in the CHPL related to the specified certified product.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
                    + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number.  A valid call "
                    + "to this service would look like "
                    + "/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/details",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
            + "{addlSoftwareCode}.{certDateCode}/details",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = CertifiedProductSearchDetails.class, friendlyUrl = "/certified_products/{chplProductNumber}/details")
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductByChplProductNumber(
            @PathVariable("year") String year,
            @PathVariable("testingLab") String testingLab,
            @PathVariable("certBody") String certBody,
            @PathVariable("vendorCode") String vendorCode,
            @PathVariable("productCode") String productCode,
            @PathVariable("versionCode") String versionCode,
            @PathVariable("icsCode") String icsCode,
            @PathVariable("addlSoftwareCode") String addlSoftwareCode,
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                versionCode, icsCode, addlSoftwareCode, certDateCode);

        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetailsByChplProductNumber(chplProductNumber);
        if (certifiedProduct == null) {
            throw new EntityRetrievalException();
        }

        Validator validator = validatorFactory.getValidator(certifiedProduct);
        if (validator != null) {
            validator.validate(certifiedProduct);
        }
        return certifiedProduct;
    }

    @Operation(summary = "Get all details for a specified certified product.",
            description = "Returns all information in the CHPL related to the specified certified product.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call "
                    + "to this service would look like /certified_products/CHP-999999.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{chplPrefix}-{identifier}/details",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = CertifiedProductSearchDetails.class, friendlyUrl = "/certified_products/{chplProductNumber}/details")
    public @ResponseBody CertifiedProductSearchDetails getCertifiedProductByChplProductNumber2(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetailsByChplProductNumber(chplProductNumber);
        if (certifiedProduct == null) {
            throw new EntityRetrievalException();
        }
        certifiedProduct = validateCertifiedProduct(certifiedProduct);
        return certifiedProduct;
    }

    @Operation(summary = "Get all details from a certified product in a CSV format suitable for uploading.",
            description = "Get all details from a certified product in a CSV format suitable for uploading.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/download",
            method = RequestMethod.GET,
            produces = DOWNLOAD_FILE_FORMAT)
    public void  downloadListingAsCsv(@PathVariable("certifiedProductId") Long certifiedProductId,
            HttpServletRequest request, HttpServletResponse response) throws EntityRetrievalException {

        CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetails(certifiedProductId);
        File tempFile = null;
        try {
            tempFile = listingCsvWriter.getAsCsv(listing);
            String filenameInResponse = listing.getChplProductNumber().replaceAll("\\.", "-") + ".csv";
            fileUtils.streamFileAsResponse(tempFile, DOWNLOAD_FILE_FORMAT, response, filenameInResponse);
        } catch (IOException ex) {
            LOGGER.error("Unable to create CSV file for listing ID + " + certifiedProductId, ex);
        }
    }

    @Operation(summary = "Get all basic information for a specified certified product.  Does not include "
            + "all collections that are in the 'certified_products/{identifier}/details' endpoint.",
            description = "Returns basic information in the CHPL related to the specified certified product.  "
                    + "Does not include all collections that are in the 'certified_products/{identifier}/details' endpoint.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertifiedProductSearchBasicDetails.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its data.",
                    content = @Content)
    })
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = CertifiedProductSearchBasicDetails.class, friendlyUrl = "/certified_products/{certifiedProductId}")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertifiedProductSearchBasicDetails getCertifiedProductByIdBasic(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {
        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetailsBasic(certifiedProductId);
        if (certifiedProduct == null) {
            throw new EntityRetrievalException();
        }
        return mapCertifiedProductDetailsToBasic.apply(certifiedProduct);
    }

    @SuppressWarnings({
            "checkstyle:parameternumber"
    })
    @Operation(summary = "Get all basic information for a specified certified product.  Does not include "
            + "all collections that are in the 'certified_products/{identifier}/details' endpoint.",
            description = "Returns basic information in the CHPL related to the specified certified product.  "
                    + "Does not include all collections that are in the 'certified_products/{identifier}/details' endpoint.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
                    + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number.  A valid "
                    + "call to this service would look like /certified_products/YY.99.99.9999.XXXX.99.99.9."
                    + "YYMMDD.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertifiedProductSearchBasicDetails.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its data.",
                    content = @Content)
    })
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
            + "{addlSoftwareCode}.{certDateCode}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = CertifiedProductSearchBasicDetails.class, friendlyUrl = "/certified_products/{chplProductNumber}")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertifiedProductSearchBasicDetails getCertifiedProductByChplProductNumberBasic(
            @PathVariable("year") String year,
            @PathVariable("testingLab") String testingLab,
            @PathVariable("certBody") String certBody,
            @PathVariable("vendorCode") String vendorCode,
            @PathVariable("productCode") String productCode,
            @PathVariable("versionCode") String versionCode,
            @PathVariable("icsCode") String icsCode,
            @PathVariable("addlSoftwareCode") String addlSoftwareCode,
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                versionCode, icsCode, addlSoftwareCode, certDateCode);

        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetailsBasicByChplProductNumber(chplProductNumber);
        if (certifiedProduct == null) {
            throw new EntityRetrievalException();
        }
        return mapCertifiedProductDetailsToBasic.apply(certifiedProduct);
    }

    @Operation(summary = "Get all basic information for a specified certified product.  Does not include "
            + "all collections that are in the 'certified_products/{identifier}/details' endpoint.",
            description = "Returns basic information in the CHPL related to the specified certified product.  "
                    + "Does not include all collections that are in the 'certified_products/{identifier}/details' endpoint.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to "
                    + "this service would look like /certified_products/CHP-999999.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertifiedProductSearchBasicDetails.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its data.",
                    content = @Content)
    })
    @RequestMapping(value = "/{chplPrefix}-{identifier}",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = CertifiedProductSearchBasicDetails.class, friendlyUrl = "/certified_products/{chplProductNumber}")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertifiedProductSearchBasicDetails getCertifiedProductByChplProductNumberBasic2(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        CertifiedProductSearchDetails certifiedProduct = cpdManager.getCertifiedProductDetailsBasicByChplProductNumber(chplProductNumber);
        if (certifiedProduct == null) {
            throw new EntityRetrievalException();
        }
        return mapCertifiedProductDetailsToBasic.apply(certifiedProduct);
    }

    @Operation(summary = "Get all of the CQM results for a specified certified product.",
            description = "Returns all of the CQM results in the CHPL related to the specified certified product.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CQMResultDetailResults.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its CQMs.",
                    content = @Content)
    })
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/cqm_results", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CQMResultDetailResults getCqmsByCertifiedProductId(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {

        CQMResultDetailResults results = new CQMResultDetailResults(cpdManager.getCertifiedProductCqms(certifiedProductId));

        return results;
    }

    @SuppressWarnings({
            "checkstyle:parameternumber"
    })
    @Operation(summary = "Get all of the CQM results for a specified certified product.",
            description = "Returns all of the CQM results in the CHPL related to the specified certified product.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
                    + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number.  A valid call to "
                    + "this service would look like /certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/cqm_results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CQMResultDetailResults.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its CQMs.",
                    content = @Content)
    })
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
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                versionCode, icsCode, addlSoftwareCode, certDateCode);
        CQMResultDetailResults results = new CQMResultDetailResults(cpdManager.getCertifiedProductCqms(chplProductNumber));

        return results;
    }

    @Operation(summary = "Get all of the CQM results for a specified certified product based on a legacy "
            + "CHPL Product Number.",
            description = "\"Returns all of the CQM results in the CHPL related to the specified certified product.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call "
                    + "to this service would look like /certified_products/CHP-999999/cqm_results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CQMResultDetailResults.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its CQMs.",
                    content = @Content)
    })
    @RequestMapping(value = "/{chplPrefix}-{identifier}/cqm_results", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CQMResultDetailResults getCqmsByCertifiedProductId(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        CQMResultDetailResults results = new CQMResultDetailResults(cpdManager.getCertifiedProductCqms(chplProductNumber));

        return results;
    }

    @DeprecatedApiResponseFields(friendlyUrl = "/certified_products/{certifiedProductId}/measures",
            responseClass = MeasureResults.class)
    @Operation(summary = "Get all of the Measures for a specified certified product.",
            description = "Returns all of the Measures in the CHPL related to the specified certified product.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MeasureResults.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its measures.",
                    content = @Content)
    })
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/measures", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody MeasureResults getMeasuresByCertifiedProductId(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {

        MeasureResults results = new MeasureResults(cpdManager.getCertifiedProductMeasures(certifiedProductId, true));

        return results;
    }

    @SuppressWarnings({
            "checkstyle:parameternumber"
    })
    @DeprecatedApiResponseFields(friendlyUrl = "/certified_products/{certifiedProductId}/measures",
        responseClass = MeasureResults.class)
    @Operation(summary = "Get all of the Measures for a specified certified product.",
            description = "Returns all of the Measures in the CHPL related to the specified certified product.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
                    + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number.  A valid call to "
                    + "this service would look like /certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/measures.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MeasureResults.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its measures.",
                    content = @Content)
    })
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}/measures", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody MeasureResults getMeasuresByCertifiedProductId(
            @PathVariable("year") String year,
            @PathVariable("testingLab") String testingLab,
            @PathVariable("certBody") String certBody,
            @PathVariable("vendorCode") String vendorCode,
            @PathVariable("productCode") String productCode,
            @PathVariable("versionCode") String versionCode,
            @PathVariable("icsCode") String icsCode,
            @PathVariable("addlSoftwareCode") String addlSoftwareCode,
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                versionCode, icsCode, addlSoftwareCode, certDateCode);
        MeasureResults results = new MeasureResults(cpdManager.getCertifiedProductMeasures(chplProductNumber));
        return results;
    }

    @DeprecatedApiResponseFields(friendlyUrl = "/certified_products/{certifiedProductId}/measures",
            responseClass = MeasureResults.class)
    @Operation(summary = "Get all of the Measures for a specified certified product based on a legacy "
            + "CHPL Product Number.",
            description = "Returns all of the Measures in the CHPL related to the specified certified product. "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call "
                    + "to this service would look like /certified_products/CHP-999999/measures.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MeasureResults.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its measures.",
                    content = @Content)
    })
    @RequestMapping(value = "/{chplPrefix}-{identifier}/measures", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody MeasureResults getMeasuresByCertifiedProductId(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        MeasureResults results = new MeasureResults(cpdManager.getCertifiedProductMeasures(chplProductNumber));
        return results;
    }

    @Operation(summary = "Get all of the certification results for a specified certified product.",
            description = "Returns all of the certification results in the CHPL related to the specified certified product."
                    + " This includes both attested and unattested criteria and any data associated with each.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertificationResults.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its certification results.",
                    content = @Content)
    })
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/certification_results", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertificationResults getCertificationResultsByCertifiedProductId(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {
        CertificationResults results = new CertificationResults(cpdManager.getCertifiedProductCertificationResults(certifiedProductId));
        return results;
    }

    @SuppressWarnings({
            "checkstyle:linelength", "checkstyle:parameternumber"
    })
    @Operation(summary = "Get all of the certification results for a specified certified "
            + "product based on a CHPL Product Number.",
            description = "Returns all of the certification results in the CHPL related to the specified certified product.  "
                    + "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
                    + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number. "
                    + "A valid call to this service would look like "
                    + "/certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/certification_results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertificationResults.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its certification results.",
                    content = @Content)
    })
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
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException {
        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                versionCode, icsCode, addlSoftwareCode, certDateCode);
        CertificationResults results = new CertificationResults(cpdManager.getCertifiedProductCertificationResults(chplProductNumber));
        return results;
    }

    @Operation(summary = "Get all of the certification results for a specified certified product based on a legacy "
            + "CHPL Product Number. This includes both attested and unattested criteria and any associated data for each.",
            description = "Returns all of the certification results in the CHPL related to the specified certified product.  "
                    + "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to this "
                    + "service would look like /certified_products/CHP-999999/certification_results.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The certified product ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertificationResults.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The certified product ID was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "The certified product ID is valid but there was an unexpected error retrieving its certification results.",
                    content = @Content)
    })
    @RequestMapping(value = "/{chplPrefix}-{identifier}/certification_results", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertificationResults getCertificationResultsByCertifiedProductId(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException {

        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        CertificationResults results = new CertificationResults(cpdManager.getCertifiedProductCertificationResults(chplProductNumber));
        return results;
    }

    @Operation(summary = "Get the ICS family for the specified certified product.",
            description = "Returns all members of the family connected to the specified certified product.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{certifiedProductId:^-?\\d+$}/ics-relationships", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingIcsNode> getIcsFamilyById(
            @PathVariable("certifiedProductId") Long certifiedProductId) throws EntityRetrievalException {
        return icsManager.getIcsFamilyTree(certifiedProductId);
    }

    @Operation(summary = "Get the ICS family for the specified certified product based on a CHPL Product Number.",
    description = "{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}."
            + "{addlSoftwareCode}.{certDateCode} represents a valid CHPL Product Number.  A valid call to this "
            + "service would look like /certified_products/YY.99.99.9999.XXXX.99.99.9.YYMMDD/ics-relationships.",
    security = {
            @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @RequestMapping(value = "/{year}.{testingLab}.{certBody}.{vendorCode}.{productCode}.{versionCode}.{icsCode}.{addlSoftwareCode}.{certDateCode}/ics-relationships",
        method = RequestMethod.GET,
        produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingIcsNode> getIcsFamilyByChplProductNumber(
            @PathVariable("year") String year,
            @PathVariable("testingLab") String testingLab,
            @PathVariable("certBody") String certBody,
            @PathVariable("vendorCode") String vendorCode,
            @PathVariable("productCode") String productCode,
            @PathVariable("versionCode") String versionCode,
            @PathVariable("icsCode") String icsCode,
            @PathVariable("addlSoftwareCode") String addlSoftwareCode,
            @PathVariable("certDateCode") String certDateCode) throws EntityRetrievalException {
        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(year, testingLab, certBody, vendorCode, productCode,
                versionCode, icsCode, addlSoftwareCode, certDateCode);
        return icsManager.getIcsFamilyTree(chplProductNumber);
    }

    @Operation(summary = "Get the ICS family for the specified certified product based on a legacy CHPL Product Number",
            description = "{chplPrefix}-{identifier} represents a valid legacy CHPL Product Number.  A valid call to this "
                    + "service would look like /certified_products/CHP-999999/ics-relationships.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{chplPrefix}-{identifier}/ics-relationships", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingIcsNode> getIcsFamilyByChplProductNumber(
            @PathVariable("chplPrefix") String chplPrefix,
            @PathVariable("identifier") String identifier) throws EntityRetrievalException {
        String chplProductNumber = chplProductNumberUtil.getChplProductNumber(chplPrefix, identifier);
        return icsManager.getIcsFamilyTree(chplProductNumber);
    }

    @Operation(summary = "Update an existing certified product.",
            description = "Updates the certified product after first validating the request. If a different "
                    + "ACB is passed in as part of the request, an ownership change will take place.  "
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and have administrative "
                    + "authority on the ACB that certified the product.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{certifiedProductId}", method = RequestMethod.PUT,
            produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = CertifiedProductSearchDetails.class, httpMethod = "PUT", friendlyUrl = "/certified_products/{certifiedProductId}")
    public ResponseEntity<CertifiedProductSearchDetails> updateCertifiedProduct(
            @RequestBody(required = true) ListingUpdateRequest updateRequest)
                    throws MissingReasonException, EntityRetrievalException, EntityCreationException,
                    CertifiedProductUpdateException, ActivityException,
                    ValidationException, InvalidArgumentsException, IOException {

        CertifiedProductSearchDetails updatedListing = updateRequest.getListing();

        CertifiedProductSearchDetails existingListing = cpdManager.getCertifiedProductDetails(updatedListing.getId());
        Long acbId = Long.parseLong(existingListing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString());

        // if the ACB owner is changed this is a separate action with different
        // security
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

    private CertifiedProductSearchDetails validateCertifiedProduct(
            CertifiedProductSearchDetails certifiedProduct) {
        Validator validator = validatorFactory.getValidator(certifiedProduct);
        if (validator != null) {
            validator.validate(certifiedProduct);
        }

        return certifiedProduct;
    }

    private static Function<CertifiedProductSearchDetails, CertifiedProductSearchBasicDetails> mapCertifiedProductDetailsToBasic = (CertifiedProductSearchDetails e) -> {
        return CertifiedProductSearchBasicDetails.builder()
                .acbCertificationId(e.getAcbCertificationId())
                .accessibilityCertified(e.getAccessibilityCertified())
                .accessibilityStandards(e.getAccessibilityStandards())
                .certificationDate(e.getCertificationDate())
                .edition(e.getEdition())
                .certificationEvents(e.getCertificationEvents())
                .certificationStatus(e.getCertificationStatus())
                .certifyingBody(e.getCertifyingBody())
                .chplProductNumber(e.getChplProductNumber())
                .classificationType(e.getClassificationType())
                .countCerts(e.getCountCerts())
                .countClosedNonconformities(e.getCountClosedNonconformities())
                .countClosedSurveillance(e.getCountClosedSurveillance())
                .countCqms(e.getCountCqms())
                .countOpenNonconformities(e.getCountOpenNonconformities())
                .countOpenSurveillance(e.getCountOpenSurveillance())
                .countSurveillance(e.getCountSurveillance())
                .decertificationDay(e.getDecertificationDay())
                .developer(e.getDeveloper())
                .directReviews(e.getDirectReviews())
                .directReviewsAvailable(e.isDirectReviewsAvailable())
                .ics(e.getIcs())
                .id(e.getId())
                .mandatoryDisclosures(e.getMandatoryDisclosures())
                .promotingInteroperabilityUserHistory(e.getPromotingInteroperabilityUserHistory())
                .otherAcb(e.getOtherAcb())
                .practiceType(e.getPracticeType())
                .product(e.getProduct())
                .productAdditionalSoftware(e.getProductAdditionalSoftware())
                .qmsStandards(e.getQmsStandards())
                .reportFileLocation(e.getReportFileLocation())
                .rwtPlansCheckDate(e.getRwtPlansCheckDate())
                .rwtPlansUrl(e.getRwtPlansUrl())
                .rwtResultsCheckDate(e.getRwtResultsCheckDate())
                .rwtResultsUrl(e.getRwtResultsUrl())
                .sed(e.getSed())
                .sedIntendedUserDescription(e.getSedIntendedUserDescription())
                .sedReportFileLocation(e.getSedReportFileLocation())
                .sedTestingEndDay(e.getSedTestingEndDay())
                .svapNoticeUrl(e.getSvapNoticeUrl())
                .surveillance(e.getSurveillance())
                .targetedUsers(e.getTargetedUsers())
                .testingLabs(e.getTestingLabs())
                .version(e.getVersion())
                .build();
    };
}
