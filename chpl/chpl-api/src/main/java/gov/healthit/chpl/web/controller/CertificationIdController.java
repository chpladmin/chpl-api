package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.BooleanUtils;
import org.ff4j.FF4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.certificationId.Validator;
import gov.healthit.chpl.certificationId.ValidatorFactory;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.CertificationIdException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import gov.healthit.chpl.web.controller.results.CertificationIdLookupResults;
import gov.healthit.chpl.web.controller.results.CertificationIdResults;
import gov.healthit.chpl.web.controller.results.CertificationIdVerifyResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "certification-ids", description = "All certification ID operations.")
@RestController
@RequestMapping("/certification_ids")
@Log4j2
public class CertificationIdController {

    private CertifiedProductManager certifiedProductManager;
    private CertificationIdManager certificationIdManager;
    private ResourcePermissions resourcePermissions;
    private ValidatorFactory validatorFactory;
    private FF4j ff4j;

    @Autowired
    public CertificationIdController(CertifiedProductManager certifiedProductManager,
            CertificationIdManager certificationIdManager, ValidatorFactory validatorFactory,
            ResourcePermissions resourcePermissions, FF4j ff4j) {
        this.certifiedProductManager = certifiedProductManager;
        this.certificationIdManager = certificationIdManager;
        this.resourcePermissions = resourcePermissions;
        this.validatorFactory = validatorFactory;
        this.ff4j = ff4j;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/certification_ids",
        removalDate = "2023-03-15",
        message = "This endpoint is deprecated and will be removed in a future release. Please use /certification_ids/report-request to receive all certification IDs in an emailed report.")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ONC_STAFF', 'ROLE_CMS_STAFF')")
    @Operation(summary = "Retrieves a list of all CMS EHR Certification IDs along with the date they were created.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_CMS_STAFF",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public List<SimpleCertificationId> getAll() throws IOException {
        List<SimpleCertificationId> results = null;
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()
                || resourcePermissions.isUserRoleOncStaff()) {
            results = certificationIdManager.getAllWithProducts();
        } else {
            results = certificationIdManager.getAll();
        }

        return results;
    }

    @Operation(summary = "Generate the CMS EHR Certification ID Report and email the results to the logged-in user.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_CMS_STAFF",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/report-request", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ChplOneTimeTrigger triggerCmsIdReport() throws SchedulerException, ValidationException {
        ChplOneTimeTrigger jobTrigger = certificationIdManager.triggerCmsIdReport();
        return jobTrigger;
    }

    @Operation(summary = "Retrieves a CMS EHR Certification ID for a collection of products.",
            description = "Retrieves a CMS EHR Certification ID for a collection of products. Returns a list of "
                    + "basic product information, Criteria and CQM calculations, and the associated CMS EHR "
                    + "Certification ID if one exists.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdResults searchCertificationId(
            @RequestParam(required = false) final List<Long> ids) throws InvalidArgumentsException,
            CertificationIdException {
        return this.findCertificationByProductIds(ids, false);
    }

    @Operation(summary = "Creates a new CMS EHR Certification ID for a collection of products if one does not already "
            + "exist.",
            description = "Retrieves a CMS EHR Certification ID for a collection of products or creates a new one "
                    + "if one does not already exist. Returns a list of basic product information, Criteria "
                    + "and CQM calculations, and the associated CMS EHR Certification ID if one exists.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdResults createCertificationId(
            @RequestParam(required = true) final List<Long> ids) throws InvalidArgumentsException,
            CertificationIdException {
        return create(ids);
    }

    private CertificationIdResults create(final List<Long> ids) throws InvalidArgumentsException,
            CertificationIdException {
        return this.findCertificationByProductIds(ids, true);
    }

    @Operation(summary = "Get information about a specific EHR Certification ID.",
            description = "Retrieves detailed information about a specific EHR Certification ID including the list of "
                    + "products that make it up.  This method can be used when verfying a small number of"
                    + "Certification Ids, where the length of the URL, plus the list of IDs, is less than the"
                    + "maximum length URL that your client can handle.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{certificationId:^[A-Z0-9]+$}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdLookupResults getCertificationId(
            @PathVariable("certificationId") final String certificationId, @RequestParam(required = false,
                    defaultValue = "false") final Boolean includeCriteria,
            @RequestParam(required = false,
                    defaultValue = "false") final Boolean includeCqms)
            throws InvalidArgumentsException,
            EntityRetrievalException, CertificationIdException {
        return this.findCertificationIdByCertificationId(certificationId, includeCriteria, includeCqms);
    }

    @Operation(summary = "Verify whether one or more specific EHR Certification IDs are valid or not.",
            description = "Returns a boolean value for each EHR Certification ID specified.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/verify", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            })
    public @ResponseBody CertificationIdVerifyResults verifyCertificationId(
            @RequestBody final CertificationIdVerificationBody body) throws InvalidArgumentsException,
            CertificationIdException {
        return this.verifyCertificationIds(body.getIds());
    }

    @Operation(summary = "Verify whether one or more specific EHR Certification IDs are valid or not.",
            description = "Returns true or false for each EHR Certification ID specified.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/verify", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdVerifyResults verifyCertificationId(
            @RequestParam("ids") final List<String> certificationIds) throws InvalidArgumentsException,
            CertificationIdException {
        return this.verifyCertificationIds(certificationIds);
    }

    private CertificationIdLookupResults findCertificationIdByCertificationId(final String certificationId,
            final Boolean includeCriteria, final Boolean includeCqms) throws InvalidArgumentsException,
            EntityRetrievalException, CertificationIdException {
        CertificationIdLookupResults results = new CertificationIdLookupResults();
        try {
            // Lookup the Cert ID
            CertificationIdDTO certDto = certificationIdManager.getByCertificationId(certificationId);
            if (null != certDto) {
                results.setEhrCertificationId(certDto.getCertificationId());
                results.setYear(certDto.getYear());

                // Find the products associated with the Cert ID
                List<Long> productIds = certificationIdManager.getProductIdsById(certDto.getId());
                List<CertifiedProductDetailsDTO> productDtos = certifiedProductManager.getDetailsByIds(productIds);

                SortedSet<Integer> yearSet = new TreeSet<Integer>();
                List<Long> certProductIds = new ArrayList<Long>();

                // Add product data to results
                List<CertificationIdLookupResults.Product> productList = results.getProducts();
                for (CertifiedProductDetailsDTO dto : productDtos) {
                    productList.add(new CertificationIdLookupResults.Product(dto));
                    yearSet.add(Integer.valueOf(dto.getYear()));
                    certProductIds.add(dto.getId());
                }

                // Add criteria and cqms met to results
                if (includeCriteria || includeCqms) {
                    Validator validator = this.validatorFactory.getValidator(certDto.getYear());

                    // Lookup Criteria for Validating
                    List<CertificationCriterionDTO> criteriaDtos = certificationIdManager
                            .getCriteriaMetByCertifiedProductIds(certProductIds);

                    // Lookup CQMs for Validating
                    List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(certProductIds);

                    boolean isValid = validator.validate(criteriaDtos, cqmDtos, new ArrayList<Integer>(yearSet));
                    if (isValid) {
                        if (includeCriteria) {
                            results.setCriteria(validator.getCriteriaMet().keySet());
                        }
                        if (includeCqms) {
                            results.setCqms(validator.getCqmsMet().keySet());
                        }
                    }
                }

            }

        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException("Unable to lookup Certification ID " + certificationId + ".");
        }

        return results;
    }

    private CertificationIdVerifyResults verifyCertificationIds(final List<String> certificationIds)
            throws InvalidArgumentsException, CertificationIdException {

        CertificationIdVerifyResults results = new CertificationIdVerifyResults();
        if (null != certificationIds) {

            try {
                Map<String, Boolean> lookupResults = certificationIdManager.verifyByCertificationId(certificationIds);

                // Put the IDs in the order that they were passed in
                for (String id : certificationIds) {
                    results.getResults().add(new CertificationIdVerifyResults.VerifyResult(id, lookupResults.get(id)));
                }

            } catch (final EntityRetrievalException e) {
                throw new CertificationIdException(
                        "Unable to verify EHR Certification IDs. Notify system administrator.");
            }

        } else {
            throw new InvalidArgumentsException("No EHR Certification IDs specified.");
        }

        return results;
    }

    private CertificationIdResults findCertificationByProductIds(List<Long> productIdListIncoming, Boolean create)
            throws InvalidArgumentsException, CertificationIdException {
        List<Long> productIdList;
        if (null == productIdListIncoming) {
            productIdList = new ArrayList<Long>();
        } else {
            productIdList = productIdListIncoming;
        }

        List<CertifiedProductDetailsDTO> productDtos = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            productDtos = certifiedProductManager.getDetailsByIds(productIdList);
        } catch (EntityRetrievalException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        // Add products to results
        CertificationIdResults results = new CertificationIdResults();
        SortedSet<Integer> yearSet = new TreeSet<Integer>();
        List<CertificationIdResults.Product> resultProducts = new ArrayList<CertificationIdResults.Product>();
        for (CertifiedProductDetailsDTO dto : productDtos) {
            if (create) {
                if (!ff4j.check(FeatureList.CANNOT_GENERATE_15E) && !dto.getYear().equalsIgnoreCase("2015")) {
                    throw new InvalidArgumentsException("New Certification IDs can only be created using 2015 Edition Listings");
                } else if (ff4j.check(FeatureList.CANNOT_GENERATE_15E) && BooleanUtils.isNotTrue(dto.getCuresUpdate())) {
                    throw new InvalidArgumentsException("New Certification IDs can only be created using 2015 Cures Update Listings");
                }
            }
            CertificationIdResults.Product p = new CertificationIdResults.Product(dto);
            resultProducts.add(p);
            yearSet.add(Integer.valueOf(dto.getYear()));
        }
        results.setProducts(resultProducts);
        String year = Validator.calculateAttestationYear(yearSet);
        results.setYear(year);

        // Validate the collection
        Validator validator = this.validatorFactory.getValidator(year);

        // Lookup Criteria for Validating
        List<CertificationCriterionDTO> criteriaDtos = certificationIdManager.getCriteriaMetByCertifiedProductIds(productIdList);

        // Lookup CQMs for Validating
        List<CQMMetDTO> cqmDtos = certificationIdManager.getCqmsMetByCertifiedProductIds(productIdList);

        boolean isValid = validator.validate(criteriaDtos, cqmDtos, new ArrayList<Integer>(yearSet));
        results.setValid(isValid);
        results.setMetPercentages(validator.getPercents());
        results.setMetCounts(validator.getCounts());
        results.setMissingCombo(validator.getMissingCombo());
        results.setMissingOr(validator.getMissingOr());
        results.setMissingAnd(validator.getMissingAnd());
        results.setMissingXOr(validator.getMissingXOr());

        // Lookup CERT ID
        if (validator.isValid()) {
            CertificationIdDTO idDto = null;
            try {
                idDto = certificationIdManager.getByListings(productDtos, year);
                if (null != idDto) {
                    results.setEhrCertificationId(idDto.getCertificationId());
                } else {
                    if ((create) && (results.isValid())) {
                        // Generate a new ID
                        idDto = certificationIdManager.create(productDtos, year);
                        results.setEhrCertificationId(idDto.getCertificationId());
                    }
                }
            } catch (final EntityRetrievalException ex) {
                throw new CertificationIdException("Unable to retrieve a Certification ID.");
            } catch (final EntityCreationException ex) {
                throw new CertificationIdException("Unable to create a new Certification ID.");
            } catch (final JsonProcessingException ex) {
                throw new CertificationIdException("Unable to create a new Certification ID.");
            }
        }
        return results;
    }
}
