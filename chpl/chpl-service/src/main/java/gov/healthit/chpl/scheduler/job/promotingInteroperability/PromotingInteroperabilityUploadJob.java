package gov.healthit.chpl.scheduler.job.promotingInteroperability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.util.DateUtil;
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
    private CertifiedProductManager cpManager;

    @Autowired
    private FF4j ff4j;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

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
                        && headingContainsUserCount(currRecord.get(1).trim())) {
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
                        piu.setError(getInvalidUserCountError(piu, currRecord.get(1).trim()));
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

    private boolean headingContainsUserCount(String columnHeading) {
        return columnHeading.contains("count");
    }

    private String getInvalidUserCountError(PromotingInteroperabilityUserRecord piu, String userCountValue) {
        return "Line " + piu.getCsvLineNumber()
            + ": Field \"user_count\" with value \"" + userCountValue
                + "\" is invalid. " + "Value in field \"user_count\" must be an integer.";
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
                        piu.setError(getMissingUserCountError(piu));
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
                            existingListing.getPromotingInteroperabilityUserHistory().add(toCreate);
                            ListingUpdateRequest updateRequest = ListingUpdateRequest.builder()
                                    .acknowledgeWarnings(true)
                                    .listing(existingListing)
                                    .reason("Updating Promoting Interoperability User count.")
                                    .build();
                            cpManager.update(updateRequest);
                        }
                    }
                } catch (ValidationException ex) {
                    String msg = "Line " + piu.getCsvLineNumber() + ": " + piu.getChplProductNumber() + " has " + ex.getErrorMessages().size() + " errors and cannot be validated or updated.";
                    LOGGER.error(msg, ex);
                    piu.setError(msg);
                } catch (Exception ex) {
                    String msg = "Line " + piu.getCsvLineNumber() + ": An unexpected error occurred. "
                            + ex.getMessage();
                    LOGGER.error(msg, ex);
                    piu.setError(msg);
                }
            }
        }
    }

    private String getMissingUserCountError(PromotingInteroperabilityUserRecord piu) {
        return "Line " + piu.getCsvLineNumber() + ": Field \"user_count\" is missing.";
    }

    private void emailResultsToUser(Set<PromotingInteroperabilityUserRecord> piuRecords, UserDTO user) {
        if (piuRecords == null || piuRecords.size() == 0
                || countPiuRecordsWithoutError(piuRecords) == 0) {
            String errorSubject = env.getProperty("piu.email.subject.failure");
            String errorHtmlBody = createHtmlEmailBodyFailure(piuRecords);
            try {
                sendEmail(user.getEmail(), errorSubject, errorHtmlBody);
            } catch (EmailNotSentException ex) {
                LOGGER.error("Unable to send email to " + user.getEmail());
                LOGGER.catching(ex);
            }
        } else {
            String errorSubject = env.getProperty("piu.email.subject.success");
            String errorHtmlBody = createHtmlEmailBodySuccess(piuRecords);
            try {
                sendEmail(user.getEmail(), errorSubject, errorHtmlBody);
            } catch (EmailNotSentException ex) {
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
            throws EmailNotSentException {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlMessage);

        chplEmailFactory.emailBuilder()
                .recipient(recipientEmail)
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
