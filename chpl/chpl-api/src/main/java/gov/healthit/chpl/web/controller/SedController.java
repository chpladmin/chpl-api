package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.sed.AgeRange;
import gov.healthit.chpl.sed.EducationType;
import gov.healthit.chpl.sed.SedManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "sed", description = "Endpoints related to SED data.")
@RestController
@RequestMapping("/sed")
public class SedController {
    private SedManager sedManager;

    @Autowired
    public SedController(SedManager sedManager) {
        this.sedManager = sedManager;
    }

    @Operation(summary = "Retrieve all current age ranges available for SED test task participants. ",
            description = "Retrieve all current age ranges available for SED test task participants.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/age-ranges", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<AgeRange> getAllAgeRanges() {
        return sedManager.getAllAgeRanges();
    }

    @Operation(summary = "Retrieve all current education types available for SED test task participants. ",
            description = "Retrieve all current education types available for SED test task participants.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/education-types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<EducationType> getAllEducationTypes() {
        return sedManager.getAllEducationTypes();
    }

    @Operation(summary = "Get all criteria that SED can be associated with.",
            description = "Get all criteria that SED can be associated with.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForSed() {
        return sedManager.getCertificationCriteriaForSed();
    }
}
