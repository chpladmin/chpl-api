package gov.healthit.chpl.web.controller;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.compliance.surveillance.SurveillanceManager;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "surveillance", description = "Allows management of listing surveillance.")
@RestController
@RequestMapping("/surveillance")
@Log4j2
public class SurveillanceController {
    private SurveillanceManager survManager;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SurveillanceController(
            SurveillanceManager survManager,
            ErrorMessageUtil errorMessageUtil) {
        this.survManager = survManager;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Operation(summary = "List all surveillance types in the system.",
            description = "List all surveillance types in the system.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<SurveillanceType> getSurveillanceTypes() {
        return survManager.getAllSurveillanceTypes();
    }

    @Operation(summary = "List all surveillance result types in the system.",
            description = "List all surveillance result types in the system.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/result-types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<SurveillanceResultType> getSurveillanceResultTypes() {
        return survManager.getAllSurveillanceResultTypes();
    }

    @Operation(summary = "List all surveillance requirement group types in the system.",
            description = "List all surveillance requirement group types in the system.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/requirement-group-types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RequirementGroupType> getRequirementGroupTypes() {
        return survManager.getAllRequirementGroupTypes();
    }

    @Operation(summary = "List all surveillance requirement types in the system.",
            description = "List all surveillance requirement types in the system.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/requirement-types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RequirementType> getRequirementTypes() {
        return survManager.getAllRequirementTypes();
    }

    @Operation(summary = "List all surveillance non-conformity types in the system.",
            description = "List all surveillance non-conformity types in the system.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/non-conformity-types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<NonconformityType> getNonconformityTypes() {
        return survManager.getAllNonconformityTypes();
    }

    @Operation(summary = "Triggers a Scheduled Job to create a surveillance activity report and email it to the current user.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/reports/activity", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChplOneTimeTrigger getActivityReport(@RequestParam("start") String start, @RequestParam("end") String end) throws ValidationException, UserRetrievalException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(start, formatter);
            endDate = LocalDate.parse(end, formatter);
            return survManager.submitActivityReportRequest(startDate, endDate);
        } catch (DateTimeException e) {
            throw new ValidationException(errorMessageUtil.getMessage("surveillance.activity.report.invalidDate"));
        }
    }
}
