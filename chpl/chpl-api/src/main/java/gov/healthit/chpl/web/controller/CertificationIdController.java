package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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

import gov.healthit.chpl.certificationId.CertificationIdCreateBody;
import gov.healthit.chpl.certificationId.CertificationIdLookupResults;
import gov.healthit.chpl.certificationId.CertificationIdManager;
import gov.healthit.chpl.certificationId.CertificationIdResults;
import gov.healthit.chpl.certificationId.CertificationIdSearchService;
import gov.healthit.chpl.certificationId.CertificationIdVerificationBody;
import gov.healthit.chpl.certificationId.CertificationIdVerificationBodyDeprecated;
import gov.healthit.chpl.certificationId.CertificationIdVerifyResults;
import gov.healthit.chpl.certificationId.CertificationIdYearCalculator;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.CertificationIdException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "certification-ids", description = "All certification ID operations.")
@RestController
public class CertificationIdController {

    private CertificationIdSearchService certIdSearchService;
    private CertificationIdManager certificationIdManager;
    private CertificationIdYearCalculator certIdYearCalculator;

    @Autowired
    public CertificationIdController(CertificationIdSearchService certIdSearchService,
            CertificationIdManager certificationIdManager,
            CertificationIdYearCalculator certIdYearCalculator) {
        this.certIdSearchService = certIdSearchService;
        this.certificationIdManager = certificationIdManager;
        this.certIdYearCalculator = certIdYearCalculator;
    }

    @Operation(summary = "Generate the CMS EHR Certification ID Report and email the results to the logged-in user.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-cms-staff",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/certification_ids/report-request",
        method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @Deprecated
    @DeprecatedApi(friendlyUrl = "/certification_ids/report-request", httpMethod = "POST",
    message = "This endpoint is deprecated and will be removed. Please use certification-ids/report-request",
    removalDate = "2026-10-01")
    public @ResponseBody ChplOneTimeTrigger triggerCmsIdReportDeprecated() throws SchedulerException, ValidationException {
        ChplOneTimeTrigger jobTrigger = certificationIdManager.triggerCmsIdReport();
        return jobTrigger;
    }

    @Operation(summary = "Generate the CMS EHR Certification ID Report and email the results to the logged-in user.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-cms-staff",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/certification-ids/report-request",
        method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ChplOneTimeTrigger triggerCmsIdReport() throws SchedulerException, ValidationException {
        ChplOneTimeTrigger jobTrigger = certificationIdManager.triggerCmsIdReport();
        return jobTrigger;
    }

    @Operation(summary = "Retrieves a CMS EHR Certification ID for the current certification year and a collection of products.",
            description = "Retrieves a CMS EHR Certification ID for the current certification year and a collection of products. "
                    + "The response includes basic product information, Criteria and CQM requirement calculations, and the associated CMS EHR "
                    + "Certification ID if one exists.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification_ids/search", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    @Deprecated
    @DeprecatedApi(friendlyUrl = "/certification_ids/search",
        message = "This endpoint is deprecated and will be removed. Please use certification-ids/search",
        removalDate = "2026-10-01")
    public @ResponseBody CertificationIdResults searchCertificationIdDeprecated(
            @RequestParam(required = false) List<Long> ids) throws InvalidArgumentsException,
            CertificationIdException {
        return certIdSearchService.findCertificationByListingIds(ids, null, false);
    }

    @Operation(summary = "Retrieves CMS EHR Certification IDs for all available certification years and the collection of products.",
            description = "Retrieves CMS EHR Certification IDs for all available certification years and the collection of products. "
                    + "For each available certification year, the response includes basic product information, Criteria and CQM requirement calculations, and the associated CMS EHR "
                    + "Certification ID if one exists.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification-ids/search", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody List<CertificationIdResults> searchCertificationId(
            @RequestParam(required = true) List<Long> listingIds)  throws InvalidArgumentsException,
            CertificationIdException {
        List<String> certificationYears = certIdYearCalculator.getValidCertIdYearsToday();
        return certificationYears.stream()
            .map(certYear -> {
                try {
                    return certIdSearchService.findCertificationByListingIds(listingIds, certYear, false);
                } catch (InvalidArgumentsException | CertificationIdException ex) {
                    throw new RuntimeException(ex);
                }
            })
            .collect(Collectors.toList());
    }

    @Operation(summary = "Creates a new CMS EHR Certification ID for the current certification year and collection "
            + "of products if one does not already exist.",
            description = "Retrieves a CMS EHR Certification ID for the current certification year and collection of products "
                    + "or creates a new one if one does not already exist. Returns a list of basic product information, Criteria "
                    + "and CQM calculations, and the associated CMS EHR Certification ID if one exists.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification_ids", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    @Deprecated
    @DeprecatedApi(friendlyUrl = "/certification_ids", httpMethod = "POST",
        message = "This endpoint is deprecated and will be removed. Please POST to /certification-ids",
        removalDate = "2026-10-01")
    public @ResponseBody CertificationIdResults createCertificationIdDeprecated(
            @RequestParam(required = true) List<Long> ids) throws InvalidArgumentsException,
            CertificationIdException {
        return certIdSearchService.findCertificationByListingIds(ids, null, true);
    }

    @Operation(summary = "Creates a new CMS EHR Certification ID for the specified certification year and collection "
            + "of products if one does not already exist.",
            description = "Retrieves a CMS EHR Certification ID for the specified certification year and collection of products "
                    + "or creates a new one if one does not already exist. Returns a list of basic product information, Criteria "
                    + "and CQM calculations, and the associated CMS EHR Certification ID if one exists.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification-ids", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdResults createCertificationId(
            @RequestBody CertificationIdCreateBody createBody) throws InvalidArgumentsException,
            CertificationIdException {
        return certIdSearchService.findCertificationByListingIds(createBody.getListingIds(), createBody.getCertificationYear(), true);
    }

    @Operation(summary = "Get information about a specific EHR Certification ID.",
            description = "Retrieves detailed information about a specific EHR Certification ID including the list of "
                    + "products that make it up.  This method can be used when verfying a small number of"
                    + "Certification Ids, where the length of the URL, plus the list of IDs, is less than the"
                    + "maximum length URL that your client can handle.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification_ids/{certificationId:^[A-Z0-9]+$}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    @Deprecated
    @DeprecatedApi(friendlyUrl = "/certification_ids/{certificationId}",
        message = "This endpoint is deprecated and will be removed. Please GET from /certification-ids/{certificationId}",
        removalDate = "2026-10-01")
    public @ResponseBody CertificationIdLookupResults getCertificationIdDeprecated(
            @PathVariable("certificationId") String certificationId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeCriteria,
            @RequestParam(required = false, defaultValue = "false") Boolean includeCqms)
            throws InvalidArgumentsException,
            EntityRetrievalException, CertificationIdException {
        return certIdSearchService.findCertificationIdByCertificationId(certificationId, includeCriteria, includeCqms);
    }

    @Operation(summary = "Get information about a specific EHR Certification ID.",
            description = "Retrieves detailed information about a specific EHR Certification ID including the list of "
                    + "products that make it up.  This method can be used when verfying a small number of"
                    + "Certification Ids, where the length of the URL, plus the list of IDs, is less than the"
                    + "maximum length URL that your client can handle.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification-ids/{certificationId:^[A-Z0-9]+$}", method = RequestMethod.GET, produces = {
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
    @RequestMapping(value = "/certification_ids/verify", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            })
    @Deprecated
    @DeprecatedApi(friendlyUrl = "/certification_ids/verify", httpMethod = "POST",
        message = "This endpoint is deprecated and will be removed. Please POST to /certification-ids/verify",
        removalDate = "2026-10-01")
    public @ResponseBody CertificationIdVerifyResults verifyCertificationIdsDeprecated(
            @RequestBody final CertificationIdVerificationBodyDeprecated body) throws InvalidArgumentsException,
            CertificationIdException {
        return this.verify(body.getIds());
    }

    @Operation(summary = "Verify whether one or more specific EHR Certification IDs are valid or not.",
            description = "Returns a boolean value for each EHR Certification ID specified.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification-ids/verify", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            })
    public @ResponseBody CertificationIdVerifyResults verifyCertificationIds(
            @RequestBody CertificationIdVerificationBody body) throws InvalidArgumentsException,
            CertificationIdException {
        return this.verify(body.getCertificationIds());
    }

    @Operation(summary = "Verify whether one or more specific EHR Certification IDs are valid or not.",
            description = "Returns true or false for each EHR Certification ID specified.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification_ids/verify", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    @Deprecated
    @DeprecatedApi(friendlyUrl = "/certification_ids/verify",
        message = "This endpoint is deprecated and will be removed. Please GET from /certification-ids/verify",
        removalDate = "2026-10-01")
    public @ResponseBody CertificationIdVerifyResults verifyCertificationIdsDeprecated(
            @RequestParam("ids") final List<String> certificationIds) throws InvalidArgumentsException,
            CertificationIdException {
        return this.verify(certificationIds);
    }

    @Operation(summary = "Verify whether one or more specific EHR Certification IDs are valid or not.",
            description = "Returns true or false for each EHR Certification ID specified.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/certification-ids/verify", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE
    })
    public @ResponseBody CertificationIdVerifyResults verifyCertificationIds(
            @RequestParam("certificationIds") List<String> certificationIds) throws InvalidArgumentsException,
            CertificationIdException {
        return this.verify(certificationIds);
    }

    private CertificationIdVerifyResults verify(List<String> certificationIds)
            throws InvalidArgumentsException, CertificationIdException {

        CertificationIdVerifyResults results = new CertificationIdVerifyResults();
        if (!CollectionUtils.isEmpty(certificationIds)) {
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
