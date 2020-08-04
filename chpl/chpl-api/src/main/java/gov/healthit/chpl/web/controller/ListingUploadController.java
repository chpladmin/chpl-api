package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.domain.ListingUpload;
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

    private ListingUploadManager uploadManager;
    private ErrorMessageUtil msgUtil;
    private Environment env;

    @Autowired
    public ListingUploadController(ListingUploadManager uploadManager,
            ErrorMessageUtil msgUtil, Environment env) {
        this.uploadManager = uploadManager;
        this.msgUtil = msgUtil;
        this.env = env;
    }

    /**
     * Upload a file with certified products.
     * @param file the file
     * @return the list of pending listings
     * @throws ValidationException if validation fails
     * @throws MaxUploadSizeExceededException if the file is too large
     */
    @ApiOperation(value = "Upload a file with certified products",
            notes = "Accepts a CSV file with very specific fields to create pending certified products. "
                    + "Security Restrictions: ROLE_ADMIN or user uploading the file must have ROLE_ACB "
                    + "and administrative authority on the ACB(s) specified in the file.")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void upload(@RequestParam("file") MultipartFile file)
            throws ValidationException, MaxUploadSizeExceededException {
        List<ListingUpload> listingsToAdd = uploadManager.parseUploadFile(file);
        for (ListingUpload listingToAdd : listingsToAdd) {
            try {
                String fileContents = getFileAsString(file);
                uploadManager.createListingUpload(listingToAdd, fileContents);
            } catch (Exception ex) {
                String error = "Error uploading listing(s) from file " + file.getOriginalFilename()
                + ". Error was: " + ex.getMessage();
                LOGGER.error(error);
                //send an email that something weird happened
                sendUploadError(file, ex);
                throw new ValidationException(error);
            }
        }
    }


    private String getFileAsString(MultipartFile file) {
        String content = null;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.error("Could not read file as String.", ex);
        }
        return content;
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
