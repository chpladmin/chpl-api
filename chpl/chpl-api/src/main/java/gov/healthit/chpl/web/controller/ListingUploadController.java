package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ConfirmListingRequest;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.upload.listing.ListingUploadManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
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

    @Value("${internalErrorEmailRecipients}")
    private String internalErrorEmailRecipients;

    @Value("${uploadErrorEmailSubject}")
    private String uploadErrorEmailSubject;

    private ListingUploadManager listingUploadManager;
    private ChplEmailFactory chplEmailFactory;
    private FileUtils fileUtils;

    @Autowired
    public ListingUploadController(ListingUploadManager listingUploadManager,
            ChplEmailFactory chplEmailFactory, FileUtils fileUtils) {
        this.listingUploadManager = listingUploadManager;
        this.chplEmailFactory = chplEmailFactory;
        this.fileUtils = fileUtils;
    }

    @Operation(summary = "Get all uploaded listings to which the current user has access.",
            description = "Security Restrictions: User will be presented the uploaded listings that "
                    + "they have access to according to ONC-ACB(s) and CHPL permissions.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ListingUpload> getAll() {
        return listingUploadManager.getAllProcessingAndAvailable();
    }

    @Operation(summary = "Get the details of an uploaded listing.",
            description = "Security Restrictions: User must be authorized to view the uploaded listing "
                    + "according to ONC-ACB(s) and CHPL permissions.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @DeprecatedApiResponseFields(responseClass = CertifiedProductSearchDetails.class, friendlyUrl = "/listings/pending/{id}")
    @RequestMapping(value = "/pending/{id:^-?\\d+$}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public CertifiedProductSearchDetails geById(@PathVariable("id") Long id)
            throws ValidationException, EntityRetrievalException {
        return listingUploadManager.getDetailsById(id);
    }

    @Operation(summary = "Get the listing with only the data the user put in the upload file.",
            description = "Security Restrictions: User must be authorized to view the uploaded listing "
                    + "according to ONC-ACB(s) and CHPL permissions.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @DeprecatedApiResponseFields(responseClass = CertifiedProductSearchDetails.class, friendlyUrl = "/listings/pending/{id}/submitted")
    @RequestMapping(value = "/pending/{id:^-?\\d+$}/submitted", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public CertifiedProductSearchDetails geUserEnteredDeveloper(@PathVariable("id") Long id)
            throws ValidationException, EntityRetrievalException {
        return listingUploadManager.getSubmittedListing(id);
    }

    @Operation(summary = "Get the upload file originally used to confirm this listing.",
            description = "Security Restrictions: ROLE_ADMIN.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/{id:^-?\\d+$}/uploaded-file", method = RequestMethod.GET, produces = "text/csv")
    public void streamUploadedFile(@PathVariable("id") Long confirmedListingId,
            HttpServletResponse response) throws EntityRetrievalException, IOException {
        List<List<String>> rows = listingUploadManager.getUploadedCsvRecords(confirmedListingId);

        File file = new File("listing-" + confirmedListingId + "-upload.csv");
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            writer.write('\ufeff');
            for (List<String> row : rows) {
                csvPrinter.printRecord(row);
            }
        } catch (final IOException ex) {
            LOGGER.error("Could not write file " + file.getName(), ex);
        }
        fileUtils.streamFileAsResponse(file, "text/csv", response);
    }

    @Operation(summary = "Upload a file with certified products",
            description = "Accepts a CSV file with a valid set of fields to upload a listing. "
                    + "Security Restrictions: ROLE_ADMIN or user uploading the file must have ROLE_ACB "
                    + "and administrative authority on the ONC-ACB(s) specified in the file.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity<ListingUploadResponse> upload(@RequestParam("file") MultipartFile file) throws ValidationException, MaxUploadSizeExceededException {
        List<ListingUpload> successfulListingUploads = new ArrayList<ListingUpload>();
        List<ListingUpload> listingsToAdd = new ArrayList<ListingUpload>();
        try {
           listingsToAdd = listingUploadManager.parseUploadFile(file);
        } catch (ValidationException ex) {
            LOGGER.error("Error uploading listing(s) from file " + file.getOriginalFilename() + ". " + ex.getMessage());
            throw ex;
        } catch (AccessDeniedException | NullPointerException | IndexOutOfBoundsException ex) {
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

    @Operation(summary = "Confirm a previously uploaded listing.",
            description = "Creates a new live listing on the CHPL based on the listing information passed in. "
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ACB "
                    + "and administrative authority on the ONC-ACB for the potentially confirmed listing is required.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending/{id:^-?\\d+$}", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ResponseEntity<CertifiedProductSearchDetails> confirmLisitngUpload(@PathVariable("id") Long id,
            @RequestBody(required = true) ConfirmListingRequest confirmListingRequest)
                    throws ValidationException, EntityCreationException, EntityRetrievalException,
                    JsonProcessingException, InvalidArgumentsException {
        CertifiedProductSearchDetails createdListing = listingUploadManager.confirm(id, confirmListingRequest);

        //note - once all collections pages are converted to use the search/beta endpoint instead of the
        //collections endpoint i don't think we need the Cache-cleared header added and we can just
        //return the listing (won't show the toaster in the UI either)
        ResponseEntity<CertifiedProductSearchDetails> response = getConfirmResponse(createdListing);
        return response;
    }

    private ResponseEntity<CertifiedProductSearchDetails> getConfirmResponse(CertifiedProductSearchDetails createdListing) {
        if (createdListing != null) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
            return new ResponseEntity<CertifiedProductSearchDetails>(createdListing, responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<CertifiedProductSearchDetails>(null, null, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Reject an uploaded listing.",
            description = "Deletes an uploaded listing. Security Restrictions: ROLE_ADMIN or have ROLE_ACB "
                    + "and administrative authority on the ONC-ACB for each uploaded listing is required.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/pending/{id:^-?\\d+$}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public void rejectListingUpload(@PathVariable("id") Long id)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, EntityNotFoundException,
            AccessDeniedException, ObjectMissingValidationException {
        //call the GET to return bad request if the id is not something that can be deleted
        listingUploadManager.getById(id);
        //perform delete
        listingUploadManager.reject(id);
    }

    private void sendUploadError(MultipartFile file, Exception ex) {
        if (StringUtils.isEmpty(internalErrorEmailRecipients)) {
            return;
        }
        List<String> recipients = Arrays.asList(internalErrorEmailRecipients.split(","));

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
            chplEmailFactory.emailBuilder()
                    .recipients(recipients)
                    .subject(uploadErrorEmailSubject)
                    .fileAttachments(attachments)
                    .htmlMessage(htmlBody)
                    .sendEmail();
        } catch (EmailNotSentException msgEx) {
            LOGGER.error("Could not send email about failed listing upload: " + msgEx.getMessage(), msgEx);
        }
    }
}
