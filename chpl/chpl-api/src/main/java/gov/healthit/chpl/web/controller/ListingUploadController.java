package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.IdListContainer;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.upload.listing.ListingUploadManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.ListingUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "listing-upload", description = "Allows upload of listings.")
@RestController
@RequestMapping("/listings")
@Log4j2
public class ListingUploadController {

    @Value("${uploadErrorEmailRecipients}")
    private String uploadErrorEmailRecipients;

    @Value("${uploadErrorEmailSubject}")
    private String uploadErrorEmailSubject;

    private ListingUploadManager listingUploadManager;
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;
    private Environment env;

    @Autowired
    public ListingUploadController(ListingUploadManager listingUploadManager,
            ErrorMessageUtil msgUtil, FF4j ff4j, Environment env) {
        this.listingUploadManager = listingUploadManager;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
        this.env = env;
    }

    @Operation(summary = "Get all uploaded listings to which the current user has access.",
            description = "Security Restrictions: User will be presented the uploaded listings that "
                    + "they have access to according to ONC-ACB(s) and CHPL permissions.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ListingUpload> getAll() {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }
        return listingUploadManager.getAll();
    }

    @Operation(summary = "Get the details of an uploaded listing.",
            description = "Security Restrictions: User must be authorized to view the uploaded listing "
                    + "according to ONC-ACB(s) and CHPL permissions.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public CertifiedProductSearchDetails geById(@PathVariable("id") Long id)
            throws ValidationException, EntityRetrievalException {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }
        return listingUploadManager.getDetailsById(id);
    }

    @Operation(summary = "Upload a file with certified products",
            description = "Accepts a CSV file with a valid set of fields to upload a listing. "
                    + "Security Restrictions: ROLE_ADMIN or user uploading the file must have ROLE_ACB "
                    + "and administrative authority on the ONC-ACB(s) specified in the file.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity<ListingUploadResponse> upload(@RequestParam("file") MultipartFile file) throws ValidationException, MaxUploadSizeExceededException {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }

        List<ListingUpload> successfulListingUploads = new ArrayList<ListingUpload>();
        List<ListingUpload> listingsToAdd = new ArrayList<ListingUpload>();
        try {
           listingsToAdd = listingUploadManager.parseUploadFile(file);
        } catch (AccessDeniedException | ValidationException | NullPointerException | IndexOutOfBoundsException ex) {
            LOGGER.error("Error uploading listing(s) from file " + file.getOriginalFilename() + ". " + ex.getMessage());
            //send an email that something weird happened
            sendUploadError(file, ex);
            throw new ValidationException(ex.getMessage());
        }

        Map<String, String> processedListingErrorMap = new LinkedHashMap<String, String>();
        if (listingsToAdd != null && listingsToAdd.size() > 0) {
            for (ListingUpload listingToAdd : listingsToAdd) {
                processedListingErrorMap.put(listingToAdd.getChplProductNumber(), null);
                try {
                    ListingUpload created = listingUploadManager.createOrReplaceListingUpload(listingToAdd);
                    successfulListingUploads.add(created);
                } catch (ValidationException ex) {
                    LOGGER.error("Error uploading listing(s) from file " + file.getOriginalFilename() + ". " + ex.getMessage());
                    //don't send an email for this exception because it's one that we create and
                    //we expect it to provide a decent error message for the user
                    processedListingErrorMap.put(listingToAdd.getChplProductNumber(), ex.getMessage());
                } catch (AccessDeniedException | NullPointerException | IndexOutOfBoundsException
                        | JsonProcessingException | EntityRetrievalException | EntityCreationException ex) {
                    LOGGER.error("Error uploading listing(s) from file " + file.getOriginalFilename() + ". " + ex.getMessage());
                    //send an email that something weird happened since the error message coming back
                    //from the caught exception might not be that helpful to the user
                    sendUploadError(file, ex);
                    processedListingErrorMap.put(listingToAdd.getChplProductNumber(), ex.getMessage());
                }
            }
        }

        try {
            if (successfulListingUploads != null && successfulListingUploads.size() > 0) {
                listingUploadManager.calculateErrorAndWarningCounts(successfulListingUploads.stream()
                    .map(lu -> lu.getId())
                    .collect(Collectors.toList()));
            }
        } catch (SchedulerException | ValidationException ex) {
            LOGGER.error("Unable to start job to calculate error and warning counts for uploaded listings.", ex);
        }

        ListingUploadResponse response = createResponse(successfulListingUploads, processedListingErrorMap);
        return new ResponseEntity<ListingUploadResponse>(response,
                response.getErrorMessages().size() == 0 ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT);
    }

    private ListingUploadResponse createResponse(List<ListingUpload> successfulListingUploads, Map<String, String> processedListingErrorMap) throws ValidationException {
        ListingUploadResponse response = new ListingUploadResponse();
        response.setSuccessfulListingUploads(successfulListingUploads);
        long listingUploadsAttempted = processedListingErrorMap.keySet().size();
        long listingUploadsFailed = processedListingErrorMap.values().stream().filter(value -> !StringUtils.isEmpty(value)).count();

        if (listingUploadsAttempted > 0 && listingUploadsFailed > 0
                && listingUploadsAttempted == listingUploadsFailed) {
            //all uploads failed - exception that returns error status
            throw new ValidationException(createErrorMessages(processedListingErrorMap));
        } else if (listingUploadsFailed > 0 && listingUploadsAttempted > 0
                && listingUploadsAttempted > listingUploadsFailed) {
            //some uploads were attempted and some failed but not all - exception that returns "partial success" status
            response.setErrorMessages(createErrorMessages(processedListingErrorMap));
        }
        return response;
    }

    private Set<String> createErrorMessages(Map<String, String> processedListingErrorMap) {
        return processedListingErrorMap.keySet().stream()
            .filter(key -> !StringUtils.isEmpty(processedListingErrorMap.get(key)))
            .map(key -> key + ": " + processedListingErrorMap.get(key))
            .collect(Collectors.toSet());
    }

    @Operation(summary = "Reject an uploaded listing.",
            description = "Deletes an uploaded listing. Security Restrictions: ROLE_ADMIN or have ROLE_ACB "
                    + "and administrative authority on the ONC-ACB for each uploaded listing is required.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending/{id}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public void rejectListingUpload(@PathVariable("id") Long id)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, ObjectMissingValidationException {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }

        //call the GET to return bad request if the id is not something that can be deleted
        listingUploadManager.getById(id);
        //perform delete
        listingUploadManager.delete(id);
    }

    @Operation(summary = "Reject several uploaded listings.",
            description = "Marks a list of uploaded listings as deleted. ROLE_ADMIN or ROLE_ACB "
                    + " and administrative authority on the ONC-ACB for each uploaded listing is required.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public void rejectListingUploads(@RequestBody IdListContainer idList)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, InvalidArgumentsException, ObjectsMissingValidationException {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }

        if (idList == null || idList.getIds() == null || idList.getIds().size() == 0) {
            throw new InvalidArgumentsException("At least one id must be provided for rejection.");
        }

        ObjectsMissingValidationException possibleExceptions = new ObjectsMissingValidationException();
        for (Long id : idList.getIds()) {
            try {
                //call the GET to return bad request if the id is not something that can be deleted
                listingUploadManager.getById(id);
                //perform delete
                listingUploadManager.delete(id);
            } catch (ObjectMissingValidationException ex) {
                possibleExceptions.getExceptions().add(ex);
            }
        }

        if (possibleExceptions.getExceptions() != null && possibleExceptions.getExceptions().size() > 0) {
            throw possibleExceptions;
        }
    }

    private void sendUploadError(MultipartFile file, Exception ex) {
        if (StringUtils.isEmpty(uploadErrorEmailRecipients)) {
            return;
        }
        List<String> recipients = Arrays.asList(uploadErrorEmailRecipients.split(","));

        //figure out the filename for the attachment
        String originalFilename = file.getOriginalFilename();
        int indexOfExtension = originalFilename.indexOf(".");
        String filenameWithoutExtension = file.getOriginalFilename();
        if (indexOfExtension >= 0) {
            filenameWithoutExtension
            = originalFilename.substring(0, indexOfExtension);
        }
        String extension = ".csv";
        if (indexOfExtension >= 0) {
            extension = originalFilename.substring(indexOfExtension);
        }

        //attach the file the user tried to upload
        File temp = null;
        List<File> attachments = null;
        try {
            temp = File.createTempFile(filenameWithoutExtension, extension);
            file.transferTo(temp);
            attachments = new ArrayList<File>();
            attachments.add(temp);
        } catch (IOException io) {
            LOGGER.error("Could not create temporary file for attachment: " + io.getMessage(), io);
        }

        //create the email body
        String htmlBody = "<p>Upload attempted at " + new Date()
                + "<br/>Uploaded by " + AuthUtil.getUsername() + "</p>";
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        htmlBody += "<pre>" + writer.toString() + "</pre>";

        //build and send the email
        try {
            EmailBuilder emailBuilder = new EmailBuilder(env);
            emailBuilder.recipients(recipients)
            .subject(uploadErrorEmailSubject)
            .fileAttachments(attachments)
            .htmlMessage(htmlBody)
            .sendEmail();
        } catch (MessagingException msgEx) {
            LOGGER.error("Could not send email about failed listing upload.", msgEx);
        }
    }
}
