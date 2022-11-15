package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.quartz.SchedulerException;
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
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.IdListContainer;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceUploadResult;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.CertificationBodyAccessException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.PendingSurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
import gov.healthit.chpl.web.controller.results.SurveillanceResults;
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
    private PendingSurveillanceManager pendingSurveillanceManager;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SurveillanceController(
            SurveillanceManager survManager,
            ActivityManager activityManager,
            CertifiedProductDetailsManager cpdetailsManager,
            PendingSurveillanceManager pendingSurveillanceManager,
            ResourcePermissions resourcePermissions,
            ErrorMessageUtil errorMessageUtil) {
        this.survManager = survManager;
        this.activityManager = activityManager;
        this.cpdetailsManager = cpdetailsManager;
        this.pendingSurveillanceManager = pendingSurveillanceManager;
        this.resourcePermissions = resourcePermissions;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Operation(summary = "Get the listing of all pending surveillance items that this user has access to.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority on the ACB associated "
                    + "with the certified product is required.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @Deprecated
    @DeprecatedApiResponseFields(responseClass = SurveillanceResults.class, friendlyUrl = "/surveillance/pending")
    @DeprecatedApi(friendlyUrl = "/surveillance/pending",
        removalDate = "2022-11-01",
        message = "This endpoint is deprecated and will be removed in a future release. There is no replacement for this functionality.")
    @RequestMapping(value = "/pending", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody SurveillanceResults getAllPendingSurveillance() throws AccessDeniedException {

        List<Surveillance> pendingSurvs = pendingSurveillanceManager.getAllPendingSurveillances();
        SurveillanceResults results = new SurveillanceResults();
        results.setPendingSurveillance(pendingSurvs);
        return results;
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

    @Operation(summary = "Reject (effectively delete) a pending surveillance item.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @Deprecated
    @DeprecatedApi(friendlyUrl = "/surveillance/pending/{pendingSurvId}",
        removalDate = "2022-11-01",
        message = "This endpoint is deprecated and will be removed in a future release. There is no replacement for this functionality.")
    @RequestMapping(value = "/pending/{pendingSurvId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String rejectPendingSurveillance(@PathVariable("pendingSurvId") Long id)
            throws EntityNotFoundException, AccessDeniedException, ObjectMissingValidationException,
            JsonProcessingException, EntityRetrievalException, EntityCreationException {

        pendingSurveillanceManager.rejectPendingSurveillance(id);
        return "{\"success\" : true}";
    }

    @Operation(summary = "Reject several pending surveillance.",
            description = "Marks a list of pending surveillance as deleted. "
                    + "If ROLE_ACB, administrative authority on the ACB for each pending surveillance is required. "
                    + "If ROLE_ADMIN or ROLE_ONC, authority for each pending surveillance is required.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @Deprecated
    @DeprecatedApi(friendlyUrl = "/surveillance/pending",
        removalDate = "2022-11-01",
        message = "This endpoint is deprecated and will be removed in a future release. There is no replacement for this functionality.")
    @RequestMapping(value = "/pending", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String rejectPendingSurveillance(@RequestBody IdListContainer idList)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, InvalidArgumentsException, ObjectsMissingValidationException {
        return deletePendingSurveillance(idList);
    }

    private @ResponseBody String deletePendingSurveillance(IdListContainer idList)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, InvalidArgumentsException, ObjectsMissingValidationException {
        if (idList == null || idList.getIds() == null || idList.getIds().size() == 0) {
            throw new InvalidArgumentsException("At least one id must be provided for rejection.");
        }

        ObjectsMissingValidationException possibleExceptions = new ObjectsMissingValidationException();
        List<CertificationBodyDTO> acbs = resourcePermissions.getAllAcbsForCurrentUser();
        for (Long id : idList.getIds()) {
            try {
                pendingSurveillanceManager.rejectPendingSurveillance(id);
            } catch (ObjectMissingValidationException ex) {
                possibleExceptions.getExceptions().add(ex);
            }
        }

        if (possibleExceptions.getExceptions() != null && possibleExceptions.getExceptions().size() > 0) {
            throw possibleExceptions;
        }
        return "{\"success\" : true}";
    }

    @Operation(summary = "Confirm a pending surveillance activity.",
            description = "Creates a new surveillance activity, surveilled requirements, and any applicable non-conformities "
                    + "in the system and associates them with the certified product indicated in the "
                    + "request body. If the surveillance is an update of an existing surveillance activity "
                    + "as indicated by the 'surveillanceIdToReplace' field, that existing surveillance "
                    + "activity will be marked as deleted and the surveillance in this request body will "
                    + "be inserted. The surveillance passed into this request will first be validated "
                    + " to check for errors and the related pending surveillance will be removed. "
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority on the ACB associated "
                    + "with the certified product is required.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @Deprecated
    @DeprecatedApiResponseFields(responseClass = Surveillance.class, httpMethod = "POST", friendlyUrl = "/surveillance/pending/confirm")
    @DeprecatedApi(friendlyUrl = "/surveillance/pending/confirm",
        removalDate = "2022-11-01",
        message = "This endpoint is deprecated and will be removed in a future release. There is no replacement for this functionality.")
    @RequestMapping(value = "/pending/confirm", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public ResponseEntity<Surveillance> confirmPendingSurveillance(@RequestBody(required = true) Surveillance survToInsert)
            throws ValidationException, EntityRetrievalException, EntityCreationException, JsonProcessingException, UserPermissionRetrievalException {

        Surveillance newSurveillance = pendingSurveillanceManager.confirmPendingSurveillance(survToInsert);
        if (newSurveillance != null) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
            return new ResponseEntity<Surveillance>(newSurveillance, responseHeaders, HttpStatus.OK);
        } else {
            return null;
        }
    }

    @Operation(summary = "Upload a file with surveillance and non-conformities for certified products.",
            description = "Accepts a CSV file with very specific fields to create pending surveillance items. "
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative authority "
                    + "on the ACB(s) responsible for the product(s) in the file.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @Deprecated
    @DeprecatedApiResponseFields(responseClass = SurveillanceResults.class, httpMethod = "POST", friendlyUrl = "/surveillance/upload")
    @DeprecatedApi(friendlyUrl = "/surveillance/upload",
        removalDate = "2022-11-01",
        message = "This endpoint is deprecated and will be removed in a future release. There is no replacement for this functionality.")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ResponseEntity<?> upload(@RequestParam("file") MultipartFile file)
            throws ValidationException, MaxUploadSizeExceededException, EntityRetrievalException,
            EntityCreationException, IOException, SchedulerException {
        SurveillanceUploadResult uploadResult = pendingSurveillanceManager.uploadPendingSurveillance(file);

        if (uploadResult.getTrigger() != null) {
            return new ResponseEntity<ChplOneTimeTrigger>(uploadResult.getTrigger(), HttpStatus.OK);
        } else {
            SurveillanceResults results = new SurveillanceResults();
            results.getPendingSurveillance().addAll(uploadResult.getSurveillances());
            return new ResponseEntity<SurveillanceResults>(results, HttpStatus.OK);
        }
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
