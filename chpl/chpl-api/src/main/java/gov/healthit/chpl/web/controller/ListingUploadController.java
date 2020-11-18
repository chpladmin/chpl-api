package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.upload.listing.ListingUploadManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.ErrorMessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Api(value = "listings")
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
            FF4j ff4j, ErrorMessageUtil msgUtil, Environment env) {
        this.listingUploadManager = listingUploadManager;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
        this.env = env;
    }

    @ApiOperation(value = "Get all uploaded listings to which the current user has access.",
            notes = "Security Restrictions: User will be presented the uploaded listings that "
                    + "they have access to according to ACB(s) and CHPL permissions.")
    @RequestMapping(value = "/pending", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ListingUpload> geAll() {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException();
        }
        return listingUploadManager.getAll();
    }

    @ApiOperation(value = "Get the details of an uploaded listing.",
            notes = "Security Restrictions: User must be authorized to view the uploaded listing "
                    + "according to ACB(s) and CHPL permissions.")
    @RequestMapping(value = "/pending/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public CertifiedProductSearchDetails geById(@PathVariable("id") Long id) throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException();
        }
        return listingUploadManager.getDetailsById(id);
    }

    @ApiOperation(value = "Upload a file with certified products",
            notes = "Accepts a CSV file with a valid set of fields to upload a listing. "
                    + "Security Restrictions: ROLE_ADMIN or user uploading the file must have ROLE_ACB "
                    + "and administrative authority on the ACB(s) specified in the file.")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public List<ListingUpload> upload(@RequestParam("file") MultipartFile file)
            throws ValidationException, MaxUploadSizeExceededException {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException();
        }

        List<ListingUpload> createdListingUploads = new ArrayList<ListingUpload>();
        List<ListingUpload> listingsToAdd = listingUploadManager.parseUploadFile(file);
        for (ListingUpload listingToAdd : listingsToAdd) {
            try {
                ListingUpload created = listingUploadManager.createOrReplaceListingUpload(listingToAdd);
                createdListingUploads.add(created);
            } catch (Exception ex) {
                String error = "Error uploading listing(s) from file " + file.getOriginalFilename()
                + ". Error was: " + ex.getMessage();
                LOGGER.error(error);
                //send an email that something weird happened
                sendUploadError(file, ex);
                throw new ValidationException(error);
            }
        }
        return createdListingUploads;
    }

    @ApiOperation(value = "Reject an uploaded listing.",
            notes = "Deletes an uploaded listing. Security Restrictions: ROLE_ADMIN or have ROLE_ACB "
                    + "and administrative authority on the ACB for each uploaded listing is required.")
    @RequestMapping(value = "/pending/{id}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public void rejectListingUpload(@PathVariable("id") Long id)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, ObjectMissingValidationException {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException();
        }

        //call the GET to return bad request if the id is not something that can be deleted
        listingUploadManager.getById(id);
        //perform delete
        listingUploadManager.delete(id);
    }

    @ApiOperation(value = "Reject several uploaded listings.",
            notes = "Marks a list of uploaded listings as deleted. ROLE_ADMIN or ROLE_ACB "
                    + " and administrative authority on the ACB for each uploaded listing is required.")
    @RequestMapping(value = "/pending", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public void rejectListingUploads(@RequestBody IdListContainer idList)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, InvalidArgumentsException, ObjectsMissingValidationException {
        if (!ff4j.check(FeatureList.ENHANCED_UPLOAD)) {
            throw new NotImplementedException();
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

    /**
     * Creates an email message to the configured recipients
     * with configured subject and uses the stack trace as the
     * email body. Creates a temporary file that is the uploaded
     * CSV and attaches it to the email.
     * @param file
     * @param ex
     */
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
