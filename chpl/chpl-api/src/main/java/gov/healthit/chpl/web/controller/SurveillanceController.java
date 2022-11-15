package gov.healthit.chpl.web.controller;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.exception.CertificationBodyAccessException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
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
    private ActivityManager activityManager;
    private CertifiedProductDetailsManager cpdetailsManager;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SurveillanceController(
            SurveillanceManager survManager,
            ActivityManager activityManager,
            CertifiedProductDetailsManager cpdetailsManager,
            ResourcePermissions resourcePermissions,
            ErrorMessageUtil errorMessageUtil) {
        this.survManager = survManager;
        this.activityManager = activityManager;
        this.cpdetailsManager = cpdetailsManager;
        this.resourcePermissions = resourcePermissions;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Operation(summary = "Create a new surveillance activity for a certified product.",
            description = "Creates a new surveillance activity, surveilled requirements, and any applicable non-conformities "
                    + "in the system and associates them with the certified product indicated in the "
                    + "request body. The surveillance passed into this request will first be validated "
                    + " to check for errors. "
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority on the ACB associated with "
                    + "the certified product is required.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedApiResponseFields(responseClass = Surveillance.class, httpMethod = "POST", friendlyUrl = "/surveillance")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity<Surveillance> createSurveillance(
            @RequestBody(required = true) Surveillance survToInsert) throws ValidationException,
            EntityRetrievalException, CertificationBodyAccessException, UserPermissionRetrievalException,
            EntityCreationException, JsonProcessingException {
        HttpHeaders responseHeaders = new HttpHeaders();
        Long insertedSurv = null;
        try {
            insertedSurv = survManager.createSurveillance(survToInsert);
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        } catch (ValidationException ex) {
            throw ex;
        }

        // query the inserted surveillance
        Surveillance result = survManager.getById(insertedSurv);
        return new ResponseEntity<Surveillance>(result, responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "Update a surveillance activity for a certified product.",
            description = "Updates an existing surveillance activity, surveilled requirements, and any applicable "
                    + "non-conformities in the system. The surveillance passed into this request will first be "
                    + "validated to check for errors. Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB "
                    + "and associated with the certified product is required.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedApiResponseFields(responseClass = Surveillance.class, httpMethod = "PUT", friendlyUrl = "/surveillance/{surveillanceId}")
    @RequestMapping(value = "/{surveillanceId}", method = RequestMethod.PUT,
            produces = "application/json; charset=utf-8")
    public ResponseEntity<Surveillance> updateSurveillance(
            @RequestBody(required = true) Surveillance survToUpdate) throws InvalidArgumentsException, ValidationException, EntityCreationException, EntityRetrievalException,
            JsonProcessingException {
        // update the surveillance
        HttpHeaders responseHeaders = new HttpHeaders();
        try {
            survManager.updateSurveillance(survToUpdate);
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        } catch (ValidationException ex) {
            throw ex;
        }

        // query the inserted surveillance
        Surveillance result = survManager.getById(survToUpdate.getId());
        return new ResponseEntity<Surveillance>(result, responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "Delete a surveillance activity for a certified product.",
            description = "Deletes an existing surveillance activity, surveilled requirements, and any applicable "
                    + "non-conformities in the system. Security Restrictions: ROLE_ADMIN or ROLE_ACB and have "
                    + "administrative authority on the specified ACB for each pending surveillance is required.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{surveillanceId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ResponseEntity<String> deleteSurveillance(
            @PathVariable(value = "surveillanceId") Long surveillanceId,
            @RequestBody(required = false) SimpleExplainableAction requestBody) throws InvalidArgumentsException, ValidationException, EntityCreationException, EntityRetrievalException,
            JsonProcessingException, AccessDeniedException, MissingReasonException {
        Surveillance survToDelete = null;
        try {
            survToDelete = survManager.getById(surveillanceId);
        } catch (EntityRetrievalException ex) {
            throw new InvalidArgumentsException("No surveillance with ID " + surveillanceId + " was found.");
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        // delete it
        survManager.deleteSurveillance(survToDelete, requestBody.getReason());
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        return new ResponseEntity<String>("{\"success\" : true}", responseHeaders, HttpStatus.OK);
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
