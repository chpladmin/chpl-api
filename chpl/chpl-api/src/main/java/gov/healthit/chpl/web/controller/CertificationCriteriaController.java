package gov.healthit.chpl.web.controller;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.certificationCriteria.CertificationCriteriaManager;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionWithAttributes;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "certification-criteria", description = "Allows management of Certification Criteria.")
@RestController
@RequestMapping("/certification-criteria")
public class CertificationCriteriaController {

    private CertificationCriteriaManager certificationCriteriaManager;

    @Autowired
    public CertificationCriteriaController(CertificationCriteriaManager certificationCriteriaManager) {
        this.certificationCriteriaManager = certificationCriteriaManager;
    }

    @DeprecatedApiResponseFields(friendlyUrl = "/certification-criteria", responseClass = CertificationCriterionWithAttributes.class)
    @Operation(summary = "Retrieve all current Certification Criteria.",
            description = "Returns all of the Certification Criteria that are currently in the CHPL. "
                    + "If any of the optional parameters are provided then only criteria that match the parameters are returned.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterionWithAttributes> getAll(
            @RequestParam(name = "certificationEdition", defaultValue = "", required = false)
            String certificationEdition,
            @RequestParam(name = "activeStartDay", defaultValue = "", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activeStartDay,
            @RequestParam(name = "activeEndDay", defaultValue = "", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activeEndDay) {
        List<CertificationCriterionWithAttributes> criteria = certificationCriteriaManager.getAllWithAttributes();
        if (StringUtils.isEmpty(certificationEdition) && activeStartDay == null && activeEndDay == null) {
            criteria = certificationCriteriaManager.getAllWithAttributes();
        } else {
            criteria = certificationCriteriaManager.getActiveWithAttributes(certificationEdition, activeStartDay, activeEndDay);
        }
        return criteria;
    }

}
