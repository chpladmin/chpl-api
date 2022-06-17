package gov.healthit.chpl.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.attestation.domain.AttestationForm;
import gov.healthit.chpl.attestation.manager.AttestationManager;
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

    @Operation(summary = "Get the list of Attestation Conditions, Attestations, and Valid Responses",
            description = "Can be used to dynamically generate the Attestion form.",
            security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "/form",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @Deprecated
    public AttestationForm getAttestationForm() {
        return attestationManager.getAttestationForm();
    }

    @Operation(summary = "Get the list of Attestation Conditions, Attestations, and Valid Responses for an Attestation period",
            description = "Can be used to dynamically generate the Attestion form.",
            security = {@SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "/periods/{periodId}/form",
            method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public AttestationForm getAttestationFormByPeriod(@PathVariable("periodId") Long periodId) {
        return attestationManager.getAttestationForm();
    }
}
