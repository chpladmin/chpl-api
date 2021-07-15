package gov.healthit.chpl.scheduler.job.promotingInteroperability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.PromotingInteroperabilityUserDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.EmailBuilder;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "promotingInteroperabilityUploadJobLogger")
public class PromotingInteroperabilityUploadJob implements Job {
    public static final String JOB_NAME = "promotingInteroperabilityUploadJob";
    public static final String FILE_CONTENTS_KEY = "fileContents";
    public static final String ACCURATE_AS_OF_DATE_KEY = "accurateAsOfDate";
    public static final String USER_KEY = "user";

    @Autowired
    private Environment env;
    @Autowired
    private CertifiedProductDetailsManager cpdManager;
    @Autowired
    private PromotingInteroperabilityUserDAO piuDao;
    @Autowired
    private ActivityManager activityManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Promoting Interoperability Upload job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            setSecurityContext(user);
            String piuData = jobDataMap.getString(FILE_CONTENTS_KEY);
            Long accurateAsOfDateMillis = jobDataMap.getLong(ACCURATE_AS_OF_DATE_KEY);
            if (StringUtils.isEmpty(piuData) || accurateAsOfDateMillis == null) {
                LOGGER.fatal("File contents or date is empty.");
            } else {
                Set<PromotingInteroperabilityUserRecord> piuRecords = parsePromotingInteroperabilityUserRecords(piuData);
                processPromotingInteroperabilityUpdates(piuRecords, DateUtil.toLocalDate(accurateAsOfDateMillis));
                emailResultsToUser(piuRecords, user);
            }
        }
        LOGGER.info("********* Completed the Promoting Interoperability Upload job. *********");
    }

    private Set<PromotingInteroperabilityUserRecord> parsePromotingInteroperabilityUserRecords(String fileContents) {
        Set<PromotingInteroperabilityUserRecord> piusToUpdate = new LinkedHashSet<PromotingInteroperabilityUserRecord>();
        Set<String> uniquePiusFromFile = new LinkedHashSet<String>();
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContents));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {

            List<CSVRecord> records = parser.getRecords();
            CSVRecord heading = null;
            for (int i = 1; i <= records.size(); i++) {
                CSVRecord currRecord = records.get(i - 1);
                PromotingInteroperabilityUserRecord piu = new PromotingInteroperabilityUserRecord();

                // get header if something similar to "chpl_product_number"
                // and "user_count" exists
                if (heading == null && i == 1 && !StringUtils.isEmpty(currRecord.get(0).trim())
                        && currRecord.get(0).trim().contains("product")
                        && !StringUtils.isEmpty(currRecord.get(1).trim())
                        && currRecord.get(1).trim().contains("count")) {
                    heading = currRecord;
                } else if (heading != null) {
                    String chplProductNumber = currRecord.get(0).trim();
                    Long userCount = null;
                    try {
                        userCount = Long.parseLong(currRecord.get(1).trim());
                        piu.setChplProductNumber(chplProductNumber);
                        piu.setUserCount(userCount);
                        piu.setCsvLineNumber(i);
                        // check if product number has already been updated
                        if (uniquePiusFromFile.contains(piu.getChplProductNumber())) {
                            throw new IOException();
                        }
                        piusToUpdate.add(piu);
                        uniquePiusFromFile.add(piu.getChplProductNumber());
                    } catch (NumberFormatException e) {
                        piu.setChplProductNumber(chplProductNumber);
                        piu.setCsvLineNumber(i);
                        String error = "Line " + piu.getCsvLineNumber()
                                + ": Field \"user_count\" with value \"" + currRecord.get(1).trim()
                                + "\" is invalid. " + "Value in field \"user_count\" must be an integer.";
                        piu.setError(error);
                        piusToUpdate.add(piu);
                        uniquePiusFromFile.add(piu.getChplProductNumber());
                    } catch (IOException e) {
                        piu.setChplProductNumber(chplProductNumber);
                        piu.setCsvLineNumber(i);
                        Integer dupLineNumber = null;
                        // get line number with duplicate chpl_product_number
                        for (PromotingInteroperabilityUserRecord entry : piusToUpdate) {
                            if (entry.getChplProductNumber().equals(piu.getChplProductNumber())) {
                                dupLineNumber = entry.getCsvLineNumber();
                            }
                        }

                        String error = "Line " + piu.getCsvLineNumber()
                                + ": Field \"chpl_product_number\" with value \"" + piu.getChplProductNumber() + "\" is invalid. "
                                + "Duplicate \"chpl_product_number\" at line " + dupLineNumber;
                        piu.setError(error);
                        piusToUpdate.add(piu);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Could not read file as CSV: " + ex.getMessage());
        }
        return piusToUpdate;
    }

    private void processPromotingInteroperabilityUpdates(Set<PromotingInteroperabilityUserRecord> piuRecords, LocalDate accurateAsOfDate) {
        for (PromotingInteroperabilityUserRecord piu : piuRecords) {
            if (StringUtils.isEmpty(piu.getError())) {
                try {
                    // If bad input, add error for this entry and continue
                    if (StringUtils.isEmpty(piu.getChplProductNumber())) {
                        piu.setError("Line " + piu.getCsvLineNumber()
                                + ": Field \"chpl_product_number\" is missing.");
                    } else if (piu.getUserCount() == null) {
                        piu.setError("Line " + piu.getCsvLineNumber()
                            + ": Field \"user_count\" is missing.");
                    } else {
                        //make sure the listing is valid and get the details
                        //object so that it can be updated
                        CertifiedProductSearchDetails existingListing = null;
                        try {
                            existingListing =
                                    cpdManager.getCertifiedProductDetailsByChplProductNumber(piu.getChplProductNumber());
                        } catch (EntityRetrievalException ex) {
                            LOGGER.warn("Searching for CHPL ID " + piu.getChplProductNumber()
                                + " encountered exception: " + ex.getMessage());
                            piu.setError("Line " + piu.getCsvLineNumber()
                                + ": Field \"chpl_product_number\" with value \"" + piu.getChplProductNumber()
                                + "\" is invalid. " + "The provided \"chpl_product_number\" does not exist.");
                        }

                        if (existingListing != null) {
                            LOGGER.info("Updating " + existingListing.getChplProductNumber() + " with PIU count of " + piu.getUserCount());
                            PromotingInteroperabilityUser toCreate = PromotingInteroperabilityUser.builder()
                                    .userCount(piu.getUserCount())
                                    .userCountDate(accurateAsOfDate)
                                    .build();
                            piuDao.create(existingListing.getId(), toCreate);

                            //write activity for the listing update
                            CertifiedProductSearchDetails updatedListing =
                                    cpdManager.getCertifiedProductDetails(existingListing.getId());
                            activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, existingListing.getId(),
                                    "Updated certified product " + updatedListing.getChplProductNumber() + ".", existingListing,
                                    updatedListing,
                                    "User " + AuthUtil.getUsername() + " updated Promoting Interoperability user count via upload file.");
                        }
                    }
                } catch (Exception ex) {
                    String msg = "Line " + piu.getCsvLineNumber() + ": An unexpected error occurred. "
                            + ex.getMessage();
                    LOGGER.error(msg, ex);
                    piu.setError(msg);
                }
            }
        }
    }

    private void emailResultsToUser(Set<PromotingInteroperabilityUserRecord> piuRecords, UserDTO user) {
        if (piuRecords == null || piuRecords.size() == 0
                || countPiuRecordsWithoutError(piuRecords) == 0) {
            String errorSubject = env.getProperty("piu.email.subject.failure");
            String errorHtmlBody = createHtmlEmailBodyFailure(piuRecords);
            try {
                sendEmail(user.getEmail(), errorSubject, errorHtmlBody);
            } catch (MessagingException ex) {
                LOGGER.error("Unable to send email to " + user.getEmail());
                LOGGER.catching(ex);
            }
        } else {
            String errorSubject = env.getProperty("piu.email.subject.success");
            String errorHtmlBody = createHtmlEmailBodySuccess(piuRecords);
            try {
                sendEmail(user.getEmail(), errorSubject, errorHtmlBody);
            } catch (MessagingException ex) {
                LOGGER.error("Unable to send email to " + user.getEmail());
                LOGGER.catching(ex);
            }
        }
    }

    private long countPiuRecordsWithoutError(Set<PromotingInteroperabilityUserRecord> piuRecords) {
        return piuRecords.stream()
                .filter(piuRecord -> StringUtils.isEmpty(piuRecord.getError()))
                .count();
    }

    private void sendEmail(String recipientEmail, String subject, String htmlMessage)
            throws MessagingException {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlMessage);

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private String createHtmlEmailBodySuccess(Set<PromotingInteroperabilityUserRecord> piuRecords) {
        long piuRecordsSuccessful = countPiuRecordsWithoutError(piuRecords);
        String htmlMessage = String.format(env.getProperty("piu.email.body.success"),
                piuRecordsSuccessful);
        if (piuRecordsSuccessful != piuRecords.size()) {
            htmlMessage += "<p>The following records from the uploaded file resulted in errors and may not have been saved in the CHPL:"
                    + "<ul>";
            List<PromotingInteroperabilityUserRecord> piuRecordsWithErrors = piuRecords.stream()
                .filter(piuRecord -> !StringUtils.isEmpty(piuRecord.getError()))
                .collect(Collectors.toList());
            for (PromotingInteroperabilityUserRecord piuRecordWithError : piuRecordsWithErrors) {
                htmlMessage += getErrorMessageForHtmlList(piuRecordWithError);
            }
            htmlMessage += "</ul></p>";
        }
        return htmlMessage;
    }

    private String createHtmlEmailBodyFailure(Set<PromotingInteroperabilityUserRecord> piuRecords) {
        String errorMessage = "Unknown error.";
        if (piuRecords == null || piuRecords.size() == 0) {
            errorMessage = "There were no promoting interoperability records found in the file.";
        } else if (countPiuRecordsWithoutError(piuRecords) == 0) {
            errorMessage = "All records in the upload file failed.<ul>";
            for (PromotingInteroperabilityUserRecord piuRecord : piuRecords) {
                errorMessage += getErrorMessageForHtmlList(piuRecord);
            }
            errorMessage += "</ul>";
        }
        String htmlMessage = String.format(env.getProperty("piu.email.body.failure"), errorMessage);
        return htmlMessage;
    }

    private String getErrorMessageForHtmlList(PromotingInteroperabilityUserRecord piuRecord) {
        return "<li>" + piuRecord.getError() + "</li>";
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser jobUser = new JWTAuthenticatedUser();
        jobUser.setFullName(user.getFullName());
        jobUser.setId(user.getId());
        jobUser.setFriendlyName(user.getFriendlyName());
        jobUser.setSubjectName(user.getUsername());
        jobUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(jobUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
