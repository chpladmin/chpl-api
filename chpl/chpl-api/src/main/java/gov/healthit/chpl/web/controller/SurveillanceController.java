package gov.healthit.chpl.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;

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
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.IdListContainer;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityDocument;
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
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.PendingSurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.validation.surveillance.reviewer.AuthorityReviewer;
import gov.healthit.chpl.web.controller.results.SurveillanceResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "surveillance", description = "Allows management of listing surveillance.")
@RestController
@RequestMapping("/surveillance")
@Loggable
@Log4j2
public class SurveillanceController  {
    private SurveillanceManager survManager;
    private ActivityManager activityManager;
    private CertifiedProductDetailsManager cpdetailsManager;
    private AuthorityReviewer survAuthorityReviewer;
    private PendingSurveillanceManager pendingSurveillanceManager;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SurveillanceController(
            SurveillanceManager survManager,
            ActivityManager activityManager,
            CertifiedProductDetailsManager cpdetailsManager,
            AuthorityReviewer survAuthorityReviewer,
            PendingSurveillanceManager pendingSurveillanceManager,
            ResourcePermissions resourcePermissions,
            ErrorMessageUtil errorMessageUtil) {
        this.survManager = survManager;
        this.activityManager = activityManager;
        this.cpdetailsManager = cpdetailsManager;
        this.survAuthorityReviewer = survAuthorityReviewer;
        this.pendingSurveillanceManager = pendingSurveillanceManager;
        this.resourcePermissions = resourcePermissions;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Operation(summary = "Get the listing of all pending surveillance items that this user has access to.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority on the ACB associated "
                    + "with the certified product is required.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody SurveillanceResults getAllPendingSurveillance() throws AccessDeniedException {

        List<Surveillance> pendingSurvs = pendingSurveillanceManager.getAllPendingSurveillances();
        SurveillanceResults results = new SurveillanceResults();
        results.setPendingSurveillance(pendingSurvs);
        return results;
    }

    @Operation(summary = "Download nonconformity supporting documentation.",
            description = "Download a specific file that was previously uploaded to a surveillance nonconformity.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "/document/{documentId}", method = RequestMethod.GET)
    public void streamDocumentContents(@PathVariable("documentId") final Long documentId,
            final HttpServletResponse response) throws EntityRetrievalException, IOException {
        SurveillanceNonconformityDocument doc = survManager.getDocumentById(documentId, true);

        if (doc != null && doc.getFileContents() != null && doc.getFileContents().length > 0) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(doc.getFileContents());
                    OutputStream outStream = response.getOutputStream()) {

                // get MIME type of the file
                String mimeType = doc.getFileType();
                if (mimeType == null) {
                    // set to binary type if MIME mapping not found
                    mimeType = "application/octet-stream";
                }
                // set content attributes for the response
                response.setContentType(mimeType);
                response.setContentLength(doc.getFileContents().length);

                // set headers for the response
                String headerKey = "Content-Disposition";
                String headerValue = String.format("attachment; filename=\"%s\"", doc.getFileName());
                response.setHeader(headerKey, headerValue);

                byte[] buffer = new byte[FileUtils.BUFFER_SIZE];
                int bytesRead = -1;

                // write bytes read from the input stream into the output stream
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    @Operation(summary = "Create a new surveillance activity for a certified product.",
            description = "Creates a new surveillance activity, surveilled requirements, and any applicable non-conformities "
                    + "in the system and associates them with the certified product indicated in the "
                    + "request body. The surveillance passed into this request will first be validated "
                    + " to check for errors. "
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority on the ACB associated with "
                    + "the certified product is required.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity<Surveillance> createSurveillance(
            @RequestBody(required = true) final Surveillance survToInsert) throws ValidationException,
    EntityRetrievalException, CertificationBodyAccessException, UserPermissionRetrievalException,
    EntityCreationException, JsonProcessingException, SurveillanceAuthorityAccessDeniedException {
        HttpHeaders responseHeaders = new HttpHeaders();
        Long insertedSurv = null;
        try {
            insertedSurv = survManager.createSurveillance(survToInsert);
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        } catch (final SurveillanceAuthorityAccessDeniedException ex) {
            LOGGER.error("User lacks authority to create surveillance");
            throw new SurveillanceAuthorityAccessDeniedException("User lacks authority to create surveillance");
        } catch (ValidationException ex) {
            throw ex;
        }

        // query the inserted surveillance
        Surveillance result = survManager.getById(insertedSurv);
        return new ResponseEntity<Surveillance>(result, responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "Add documentation to an existing nonconformity.",
            description = "Upload a file of any kind (current size limit 5MB) as supporting "
                    + " documentation to an existing nonconformity. Security Restrictions: ROLE_ADMIN, ROLE_ONC, or "
                    + "ROLE_ACB and administrative authority on the associated ONC-ACB.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/{surveillanceId}/nonconformity/{nonconformityId}/document",
    method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody String uploadNonconformityDocument(
            @PathVariable("surveillanceId") final Long surveillanceId,
            @PathVariable("nonconformityId") final Long nonconformityId,
            @RequestParam("file") final MultipartFile file)
                    throws InvalidArgumentsException, MaxUploadSizeExceededException, EntityRetrievalException,
                    EntityCreationException, IOException {

        return createNonconformityDocumentForSurveillance(surveillanceId, nonconformityId, file);
    }

    private String createNonconformityDocumentForSurveillance(
            final Long surveillanceId,
            final Long nonconformityId,
            final MultipartFile file)
                    throws InvalidArgumentsException, MaxUploadSizeExceededException, EntityRetrievalException,
                    EntityCreationException, IOException {

        if (file.isEmpty()) {
            throw new InvalidArgumentsException("You cannot upload an empty file!");
        }

        Surveillance surv = survManager.getById(surveillanceId);
        CertifiedProductSearchDetails beforeCp = cpdetailsManager
                .getCertifiedProductDetails(surv.getCertifiedProduct().getId());

        SurveillanceNonconformityDocument toInsert = new SurveillanceNonconformityDocument();
        toInsert.setFileContents(file.getBytes());
        toInsert.setFileName(file.getOriginalFilename());
        toInsert.setFileType(file.getContentType());

        Long insertedDocId = survManager.addDocumentToNonconformity(nonconformityId, toInsert);
        if (insertedDocId == null) {
            throw new EntityCreationException("Error adding a document to nonconformity with id " + nonconformityId);
        }

        CertifiedProductSearchDetails afterCp = cpdetailsManager
                .getCertifiedProductDetails(surv.getCertifiedProduct().getId());
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT,
                beforeCp.getId(), "Documentation " + toInsert.getFileName()
                + " was added to a nonconformity for certified product " + afterCp.getChplProductNumber(),
                beforeCp, afterCp);
        return "{\"success\": \"true\"}";
    }

    @Operation(summary = "Update a surveillance activity for a certified product.",
            description = "Updates an existing surveillance activity, surveilled requirements, and any applicable "
                    + "non-conformities in the system. The surveillance passed into this request will first be "
                    + "validated to check for errors. Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB "
                    + "and associated with the certified product is required.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/{surveillanceId}", method = RequestMethod.PUT,
    produces = "application/json; charset=utf-8")
    public ResponseEntity<Surveillance> updateSurveillance(
            @RequestBody(required = true) final Surveillance survToUpdate) throws
    InvalidArgumentsException, ValidationException, EntityCreationException, EntityRetrievalException,
    JsonProcessingException, SurveillanceAuthorityAccessDeniedException {
        // update the surveillance
        HttpHeaders responseHeaders = new HttpHeaders();
        try {
            survManager.updateSurveillance(survToUpdate);
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        } catch (SurveillanceAuthorityAccessDeniedException ex) {
            LOGGER.error("User lacks authority to update surveillance");
            throw new SurveillanceAuthorityAccessDeniedException("User lacks authority to update surveillance");
        } catch (UserPermissionRetrievalException ex) {
            String error = "Cannot find user permission for the surveillance.";
            throw new EntityRetrievalException(error);
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
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/{surveillanceId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody ResponseEntity<String> deleteSurveillance(
            @PathVariable(value = "surveillanceId") final Long surveillanceId,
            @RequestBody(required = false) final SimpleExplainableAction requestBody) throws
    InvalidArgumentsException, ValidationException, EntityCreationException, EntityRetrievalException,
    JsonProcessingException, AccessDeniedException, SurveillanceAuthorityAccessDeniedException,
    MissingReasonException {
        Surveillance survToDelete = survManager.getById(surveillanceId);

        if (survToDelete == null) {
            throw new InvalidArgumentsException("Cannot find surveillance with id " + surveillanceId + " to delete.");
        }

        survAuthorityReviewer.review(survToDelete);
        if (survToDelete.getErrorMessages() != null && survToDelete.getErrorMessages().size() > 0) {
            throw new ValidationException(survToDelete.getErrorMessages(), null);
        }

        CertifiedProductSearchDetails beforeCp = cpdetailsManager
                .getCertifiedProductDetails(survToDelete.getCertifiedProduct().getId());

        HttpHeaders responseHeaders = new HttpHeaders();
        // delete it
        try {
            survManager.deleteSurveillance(survToDelete);
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        } catch (SurveillanceAuthorityAccessDeniedException ex) {
            LOGGER.error("User lacks authority to delete surveillance");
            throw new SurveillanceAuthorityAccessDeniedException("User lacks authority to delete surveillance");
        }

        CertifiedProductSearchDetails afterCp = cpdetailsManager
                .getCertifiedProductDetails(survToDelete.getCertifiedProduct().getId());
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, afterCp.getId(),
                "Surveillance was delete from certified product " + afterCp.getChplProductNumber(),
                beforeCp, afterCp, requestBody.getReason());

        return new ResponseEntity<String>("{\"success\" : true}", responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "Remove documentation from a nonconformity.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative authority "
                    + "on the associated ACB.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/{surveillanceId}/document/{docId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public String deleteNonconformityDocumentFromSurveillance(
            @PathVariable("surveillanceId") final Long surveillanceId,
            @PathVariable("docId") final Long docId)
                    throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
                    InvalidArgumentsException {

        return deleteNonconformityDocument(surveillanceId, docId);
    }

    private String deleteNonconformityDocument(final Long surveillanceId, final Long docId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException,
            InvalidArgumentsException {

        Surveillance surv = survManager.getById(surveillanceId);
        if (surv == null) {
            throw new InvalidArgumentsException("Cannot find surveillance with id " + surveillanceId + " to delete.");
        }

        CertifiedProductSearchDetails beforeCp = cpdetailsManager
                .getCertifiedProductDetails(surv.getCertifiedProduct().getId());
        try {
            survManager.deleteNonconformityDocument(docId);
        } catch (Exception ex) {
            throw ex;
        }

        CertifiedProductSearchDetails afterCp = cpdetailsManager
                .getCertifiedProductDetails(surv.getCertifiedProduct().getId());
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, beforeCp.getId(),
                "A document was removed from a nonconformity for certified product " + afterCp.getChplProductNumber(),
                beforeCp, afterCp);
        return "{\"success\": \"true\"}";
    }

    @Operation(summary = "Reject (effectively delete) a pending surveillance item.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending/{pendingSurvId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody String rejectPendingSurveillance(@PathVariable("pendingSurvId") final Long id)
            throws EntityNotFoundException, AccessDeniedException, ObjectMissingValidationException,
            JsonProcessingException, EntityRetrievalException, EntityCreationException {

        pendingSurveillanceManager.rejectPendingSurveillance(id);
        return "{\"success\" : true}";
    }

    @Operation(summary = "Reject several pending surveillance.",
            description = "Marks a list of pending surveillance as deleted. "
                    + "If ROLE_ACB, administrative authority on the ACB for each pending surveillance is required. "
                    + "If ROLE_ADMIN or ROLE_ONC, authority for each pending surveillance is required.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody String rejectPendingSurveillance(@RequestBody final IdListContainer idList)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, InvalidArgumentsException, ObjectsMissingValidationException {
        return deletePendingSurveillance(idList);
    }

    private @ResponseBody String deletePendingSurveillance(final IdListContainer idList)
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
            } catch (final ObjectMissingValidationException ex) {
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
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending/confirm", method = RequestMethod.POST,
    produces = "application/json; charset=utf-8")
    public ResponseEntity<Surveillance> confirmPendingSurveillance(
            @RequestBody(required = true) final Surveillance survToInsert)
                    throws ValidationException, EntityRetrievalException, EntityCreationException,
                    JsonProcessingException, UserPermissionRetrievalException,
                    SurveillanceAuthorityAccessDeniedException {

        Surveillance newSurveillance = pendingSurveillanceManager.confirmPendingSurveillance(survToInsert);
        if (newSurveillance != null) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
            return new ResponseEntity<Surveillance>(newSurveillance, responseHeaders, HttpStatus.OK);
        } else {
            return null;
        }
    }

    @Operation(summary = "Upload a file with surveillance and nonconformities for certified products.",
            description = "Accepts a CSV file with very specific fields to create pending surveillance items. "
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative authority "
                    + "on the ACB(s) responsible for the product(s) in the file.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ResponseEntity<?> upload(@RequestParam("file") final MultipartFile file)
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
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
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
