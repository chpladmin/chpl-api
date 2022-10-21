package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.upload.listing.ListingUploadDao;
import gov.healthit.chpl.upload.listing.ListingUploadManager;
import gov.healthit.chpl.upload.listing.ListingUploadStatus;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "listingUploadValidationJobLogger")
public class ListingUploadValidationJob implements Job {
    public static final String JOB_NAME = "listingUploadValidationJob";
    public static final String LISTING_UPLOAD_IDS = "listingUploadIds";
    public static final String USER_KEY = "user";

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private ListingUploadDao listingUploadDao;

    @Autowired
    private ListingUploadManager listingUploadManager;

    @Value("${uploadErrorEmailRecipients}")
    private String uploadErrorEmailRecipients;

    @Value("${uploadErrorEmailSubject}")
    private String uploadErrorEmailSubject;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Listing Upload Validation job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            try {
                setSecurityContext(user);

                @SuppressWarnings("unchecked")
                List<Long> listingUploadIds = (List<Long>) jobDataMap.get(LISTING_UPLOAD_IDS);
                if (listingUploadIds == null || listingUploadIds.size() == 0) {
                    LOGGER.error("Missing parameter for listing upload IDs.");
                } else {
                    for (Long listingUploadId : listingUploadIds) {
                        try {
                            calculateErrorAndWarningCounts(listingUploadId);
                        } catch (Exception ex) {
                            LOGGER.error("Unexpected exception calculating error/warning counts for listing upload with ID " + listingUploadId);
                            LOGGER.catching(ex);
                            saveValidationFailed(listingUploadId, ex);
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.fatal("Unexpected exception was caught. All listing uploads may not have been processed.", ex);
            }
        }
        LOGGER.info("********* Completed the Listing Upload Validation job. *********");
    }

    private void calculateErrorAndWarningCounts(Long listingUploadId) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                ListingUpload listingUpload = null;
                try {
                    listingUpload = listingUploadDao.getById(listingUploadId);
                } catch (EntityRetrievalException ex) {
                    LOGGER.error("Unable to get listing upload with id " + listingUploadId + ".", ex);
                }

                if (listingUpload != null) {
                    LOGGER.info("Calculating error and warning counts for listing upload ID " + listingUploadId
                            + "; CHPL Product Number " + listingUpload.getChplProductNumber());
                    CertifiedProductSearchDetails listingDetails = null;
                    try {
                        listingDetails = listingUploadManager.getDetailsById(listingUpload.getId());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }

                    if (listingDetails != null) {
                        listingUpload.setStatus(ListingUploadStatus.UPLOAD_SUCCESS);
                        listingUpload.setErrorCount(listingDetails.getErrorMessages() == null ? 0 : listingDetails.getErrorMessages().size());
                        listingUpload.setWarningCount(listingDetails.getWarningMessages() == null ? 0 : listingDetails.getWarningMessages().size());
                        LOGGER.info("Listing upload with ID " + listingUpload.getId() + " had "
                                + listingUpload.getErrorCount() + " errors and " + listingUpload.getWarningCount()
                                + " warnings.");
                        try {
                            listingUploadDao.updateErrorAndWarningCounts(listingUpload);
                        } catch (Exception ex) {
                            LOGGER.error("The pending listing " + listingUpload.getChplProductNumber() + " could not be updated. No updates were made to the error/warning counts.", ex);
                        }
                    }
                }
            }
        });
    }

    private void saveValidationFailed(Long listingUploadId, Exception ex) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    listingUploadDao.updateStatus(listingUploadId, ListingUploadStatus.UPLOAD_FAILURE);
                } catch (Exception ex) {
                    LOGGER.error("The pending listing with ID " + listingUploadId + " could not be updated. No updates were made to its status.");
                }

                try {
                    sendUploadError(createCsv(listingUploadId), ex);
                } catch (IOException ex) {
                    LOGGER.error("Error creating CSV file from upload failure.", ex);
                } catch (Exception ex) {
                    LOGGER.error("Unexpected problem sending upload error email.", ex);
                }
            }
        });
    }

    private void sendUploadError(File file, Exception ex) {
        if (StringUtils.isEmpty(uploadErrorEmailRecipients)) {
            return;
        }
        List<String> recipients = Arrays.asList(uploadErrorEmailRecipients.split(","));

        //attach the file the user tried to upload
        List<File> attachments = new ArrayList<File>();
        attachments.add(file);

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

    private File createCsv(Long listingUploadId) throws IOException {
        ListingUpload listingUpload = null;
        try {
            listingUpload = listingUploadDao.getById(listingUploadId);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Unable to get listing upload with id " + listingUploadId + ".", ex);
        }

        List<CSVRecord> csvRecords = listingUpload.getRecords();
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.builder()
                .setRecordSeparator(System.lineSeparator())
                .build();

        File csvFile = File.createTempFile("listing-upload-failure-" + listingUpload.getId() + "-", ".csv");
        try (FileWriter fileWriter = new FileWriter(csvFile);
                CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {

            fileWriter.write('\ufeff');
            csvRecords.stream()
                .forEach(csvRecord -> printRow(csvFilePrinter, csvRecord));
        }
        return csvFile;
    }

    private void printRow(CSVPrinter csvFilePrinter, CSVRecord csvRecord) {
        try {
            csvFilePrinter.printRecord(csvRecord);
        } catch (IOException e) {
            LOGGER.catching(e);
        }
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser splitUser = new JWTAuthenticatedUser();
        splitUser.setFullName(user.getFullName());
        splitUser.setId(user.getId());
        splitUser.setFriendlyName(user.getFriendlyName());
        splitUser.setSubjectName(user.getUsername());
        splitUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(splitUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
