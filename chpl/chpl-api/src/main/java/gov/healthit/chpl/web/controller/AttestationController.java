package gov.healthit.chpl.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.attestation.domain.AttestationPeriodForm;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.AttestationPeriodResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "attestation", description = "Provides data related to attestations")
@RestController
@RequestMapping("/attestations")
public class AttestationController {
    private AttestationManager attestationManager;

    @Autowired
    public AttestationController(AttestationManager attestationManager) {
        this.attestationManager = attestationManager;
    }

    @Operation(summary = "Get all of the Attestation Periods",
            description = "Attestation Periods define the time period which an Attestion applies.",
            security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "/periods",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public AttestationPeriodResults getAllPeriods() {
        return new AttestationPeriodResults(attestationManager.getAllPeriods());
    }

    @Operation(summary = "Get the list of Attestation Conditions, Attestations, and Valid Responses for an Attestation period",
            description = "Can be used to dynamically generate the Attestion form. The optional 'developerId' parameter "
                    + "may be provided to pre-fill the form data for each allowed response with a message specific to "
                    + "that developers listings during the attestation period. If 'developerId' is not provided, all allowed "
                    + "response messages will be blank.",
            security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "/periods/{periodId}/form",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public AttestationPeriodForm getAttestationFormByPeriod(@PathVariable("periodId") Long periodId,
            @RequestParam(name = "developerId", required = false) Long developerId) throws EntityRetrievalException {
        return attestationManager.getAttestationForm(periodId, developerId);
    }
}
