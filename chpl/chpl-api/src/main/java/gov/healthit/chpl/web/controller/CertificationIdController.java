package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.Map;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.certificationId.CertificationIdLookupResults;
import gov.healthit.chpl.certificationId.CertificationIdManager;
import gov.healthit.chpl.certificationId.CertificationIdResults;
import gov.healthit.chpl.certificationId.CertificationIdSearchService;
import gov.healthit.chpl.certificationId.CertificationIdVerifyResults;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.CertificationIdException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "certification-ids", description = "All certification ID operations.")
@RestController
@RequestMapping("/certification_ids")
public class CertificationIdController {

    private CertificationIdSearchService certIdSearchService;
    private CertificationIdManager certificationIdManager;

    @Autowired
    public CertificationIdController(CertificationIdSearchService certIdSearchService,
            CertificationIdManager certificationIdManager) {
        this.certIdSearchService = certIdSearchService;
        this.certificationIdManager = certificationIdManager;
    }

    @Operation(summary = "Generate the CMS EHR Certification ID Report and email the results to the logged-in user.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-cms-staff",
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
            @RequestParam(required = false) List<Long> ids) throws InvalidArgumentsException,
            CertificationIdException {
        return certIdSearchService.findCertificationByProductIds(ids, false);
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
            @RequestParam(required = true) List<Long> ids) throws InvalidArgumentsException,
            CertificationIdException {
        return certIdSearchService.findCertificationByProductIds(ids, true);
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
            @PathVariable("certificationId") String certificationId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeCriteria,
            @RequestParam(required = false, defaultValue = "false") Boolean includeCqms)
            throws InvalidArgumentsException,
            EntityRetrievalException, CertificationIdException {
        return certIdSearchService.findCertificationIdByCertificationId(certificationId, includeCriteria, includeCqms);
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
}
