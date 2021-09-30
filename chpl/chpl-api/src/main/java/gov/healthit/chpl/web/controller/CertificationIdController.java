package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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

import gov.healthit.chpl.certificationId.Validator;
import gov.healthit.chpl.certificationId.ValidatorFactory;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.CertificationIdException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.web.controller.results.CertificationIdLookupResults;
import gov.healthit.chpl.web.controller.results.CertificationIdResults;
import gov.healthit.chpl.web.controller.results.CertificationIdVerifyResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Api(value = "certification-ids")
@RestController
@RequestMapping("/certification_ids")
@Loggable
@Log4j2
public class CertificationIdController {

    private CertifiedProductManager certifiedProductManager;
    private CertificationIdManager certificationIdManager;
    private ResourcePermissions resourcePermissions;
    private ValidatorFactory validatorFactory;

    @Autowired
    public CertificationIdController(CertifiedProductManager certifiedProductManager,
            CertificationIdManager certificationIdManager, ValidatorFactory validatorFactory,
            ResourcePermissions resourcePermissions) {
        this.certifiedProductManager = certifiedProductManager;
        this.certificationIdManager = certificationIdManager;
        this.resourcePermissions = resourcePermissions;
        this.validatorFactory = validatorFactory;
    }

    // **********************************************************************************************************
    // getAll
    //
    // Mapping: / (Root)
    //
    // Retrieves all CMS Certification IDs and their date of creation.
    // **********************************************************************************************************
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ONC_STAFF', 'ROLE_CMS_STAFF')")
    @ApiOperation(value = "Retrieves a list of all CMS EHR Certification IDs along with the date they were created.",
    notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_CMS_STAFF")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public List<SimpleCertificationId> getAll() throws IOException {
        List<SimpleCertificationId> results = null;
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()
                || resourcePermissions.isUserRoleOncStaff()) {
            results = certificationIdManager.getAllWithProductsCached();
        } else {
            results = certificationIdManager.getAllCached();
        }

        return results;
    }

    // **********************************************************************************************************
    // searchCertificationId
    //
    // Mapping: /search
    // Params: List ids
    //
    // Retrieves a CMS EHR Certification ID for a collection of products.
    // **********************************************************************************************************
    @ApiOperation(value = "Retrieves a CMS EHR Certification ID for a collection of products.",
            notes = "Retrieves a CMS EHR Certification ID for a collection of products. Returns a list of "
                    + "basic product information, Criteria and CQM calculations, and the associated CMS EHR "
                    + "Certification ID if one exists.")
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdResults searchCertificationId(
            @RequestParam(required = false) final List<Long> ids) throws InvalidArgumentsException,
    CertificationIdException {
        return this.findCertificationByProductIds(ids, false);
    }

    @ApiOperation(
            value = "Creates a new CMS EHR Certification ID for a collection of products if one does not already "
                    + "exist.",
                    notes = "Retrieves a CMS EHR Certification ID for a collection of products or creates a new one "
                            + "if one does not already exist. Returns a list of basic product information, Criteria "
                            + "and CQM calculations, and the associated CMS EHR Certification ID if one exists.")
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

    /**
     * Retrieves detailed information about a specific CMS EHR Certification ID
     * including the list of products
     * that make it up. It optionally retrieves the Certification Criteria and
     * CQMs of the products
     * associated with the CMS EHR Certification ID.
     * @param certificationId ID to look up
     * @param includeCriteria indicates if should return criteria
     * @param includeCqms indicates if should return CQMs
     * @return information about CMS ID
     * @throws InvalidArgumentsException if arguments are invalid
     * @throws EntityRetrievalException if couldn't retrieve entity
     * @throws CertificationIdException if cert id fails
     */
    @ApiOperation(value = "Get information about a specific EHR Certification ID.",
            notes = "Retrieves detailed information about a specific EHR Certification ID including the list of "
                    + "products that make it up.  This method can be used when verfying a small number of"
                    + "Certification Ids, where the length of the URL, plus the list of IDs, is less than the"
                    + "maximum length URL that your client can handle.")
    @RequestMapping(value = "/{certificationId:^[A-Z0-9]+$}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdLookupResults getCertificationId(
            @PathVariable("certificationId") final String certificationId, @RequestParam(required = false,
            defaultValue = "false") final Boolean includeCriteria, @RequestParam(required = false,
            defaultValue = "false") final Boolean includeCqms) throws InvalidArgumentsException,
    EntityRetrievalException, CertificationIdException {
        return this.findCertificationIdByCertificationId(certificationId, includeCriteria, includeCqms);
    }

    /**
     * Verify whether one or more specific EHR Certification ID is valid or not.
     * @param body post body
     * @return response indicating if IDs are valid
     * @throws InvalidArgumentsException if arguments are invalid
     * @throws CertificationIdException if cert id fails
     */
    @ApiOperation(value = "Verify whether one or more specific EHR Certification IDs are valid or not.",
            notes = "Returns a boolean value for each EHR Certification ID specified.")
    @RequestMapping(value = "/verify", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdVerifyResults verifyCertificationId(
            @RequestBody final CertificationIdVerificationBody body) throws InvalidArgumentsException,
    CertificationIdException {
        return this.verifyCertificationIds(body.getIds());
    }

    /**
     * Verify whether one or more specific EHR Certification ID is valid or not.
     * @param certificationIds incoming IDs
     * @return response indicating if IDs are valid
     * @throws InvalidArgumentsException if arguments are invalid
     * @throws CertificationIdException if cert id fails
     */
    @ApiOperation(value = "Verify whether one or more specific EHR Certification IDs are valid or not.",
            notes = "Returns true or false for each EHR Certification ID specified.")
    @RequestMapping(value = "/verify", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdVerifyResults verifyCertificationId(
            @RequestParam("ids") final List<String> certificationIds) throws InvalidArgumentsException,
    CertificationIdException {
        return this.verifyCertificationIds(certificationIds);
    }

    // **********************************************************************************************************
    // findCertificationIdByCertificationId
    //
    // **********************************************************************************************************
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

    // **********************************************************************************************************
    // verifyCertificationIds
    //
    // **********************************************************************************************************
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

    // **********************************************************************************************************
    // findCertificationByProductIds
    //
    // **********************************************************************************************************
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
            if (create && !dto.getYear().equalsIgnoreCase("2015")) {
                throw new CertificationIdException("New Certification IDs can only be created using 2015 Edition Listings");
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
                idDto = certificationIdManager.getByProductIds(productIdList, year);
                if (null != idDto) {
                    results.setEhrCertificationId(idDto.getCertificationId());
                } else {
                    if ((create) && (results.isValid())) {
                        // Generate a new ID
                        idDto = certificationIdManager.create(productIdList, year);
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
