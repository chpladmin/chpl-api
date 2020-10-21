package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.util.EmailBuilder;

/**
 * The PendingChangeRequestEmailJob implements a Quartz job and is available to ROLE_ADMIN and ROLE_ONC. When invoked it
 * emails configured individuals with the Change Requests that are in a pending state.
 */
public class PendingChangeRequestEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("pendingChangeRequestEmailJobLogger");

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private ChangeRequestDAO changeRequestDAO;

    @Autowired
    private Environment env;

    private static final int DEVELOPER_NAME = 0;
    private static final int DEVELOPER_CODE = 1;
    private static final int DEVELOPER_CONTACT_NAME = 2;
    private static final int DEVELOPER_CONTACT_EMAIL = 3;
    private static final int DEVELOPER_CONTACT_PHONE_NUMBER = 4;
    private static final int CR_TYPE = 5;
    private static final int CR_STATUS = 6;
    private static final int CR_DATE = 7;
    private static final int CHANGE_REQUEST_DAYS_OPEN = 8;
    private static final int CR_LATEST_DATE = 9;
    private static final int CHANGE_REQUEST_LATEST_DAYS_OPEN = 10;
    private static final int CR_LATEST_COMMENT = 11;
    private static final int ONC_ACB_START = 12;
    private static final int NUM_REPORT_COLS = 12;

    private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;

    public PendingChangeRequestEmailJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Pending Change Request job. *********");
        LOGGER.info("Creating pending change request email for: " + jobContext.getMergedJobDataMap().getString("email"));

        try {
            List<CertificationBodyDTO> acbs = getAppropriateAcbs(jobContext);
            Date currentDate = new Date();
            List<List<String>> csvRows = getAppropriateActivities(acbs, currentDate);
            sendEmail(jobContext, csvRows, acbs);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("********* Completed the Pending Change Request Email job. *********");
    }

    private List<CertificationBodyDTO> getAppropriateAcbs(JobExecutionContext jobContext) {
        List<CertificationBodyDTO> acbs = certificationBodyDAO.findAllActive();
        if (jobContext.getMergedJobDataMap().getBooleanValue("acbSpecific")) {
            List<Long> acbsFromJob = getAcbsFromJobContext(jobContext);
            acbs = acbs.stream()
                    .filter(acb -> acbsFromJob.contains(acb.getId()))
                    .collect(Collectors.toList());
        }
        return acbs;
    }

    private List<Long> getAcbsFromJobContext(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acbIdAsString -> Long.parseLong(acbIdAsString))
                .collect(Collectors.toList());
    }

    private List<List<String>> getAppropriateActivities(final List<CertificationBodyDTO> activeAcbs,
            final Date currentDate) throws EntityRetrievalException {
        List<List<String>> activities = new ArrayList<List<String>>();
        activities.addAll(createChangeRequestRows(activeAcbs, currentDate));
        return activities;
    }

    private File getOutputFile(final List<List<String>> rows, final String reportFilename,
            final List<CertificationBodyDTO> activeAcbs) {
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp),
                    Charset.forName("UTF-8").newEncoder());
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                writer.write('\ufeff');
                csvPrinter.printRecord(getHeaderRow(activeAcbs));
                for (List<String> rowValue : rows) {
                    csvPrinter.printRecord(rowValue);
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return temp;
    }

    private List<String> getHeaderRow(final List<CertificationBodyDTO> activeAcbs) {
        List<String> row = createEmptyRow(activeAcbs);
        row.set(DEVELOPER_NAME, "Developer Name");
        row.set(DEVELOPER_CODE, "Developer Code");
        row.set(DEVELOPER_CONTACT_NAME, "Developer Contact Name");
        row.set(DEVELOPER_CONTACT_EMAIL, "Developer Contact Email");
        row.set(DEVELOPER_CONTACT_PHONE_NUMBER, "Developer Contact Phone Number");
        row.set(CR_TYPE, "Change Request Type");
        row.set(CR_STATUS, "Change Request Status");
        row.set(CR_DATE, "Change Request Open Date");
        row.set(CHANGE_REQUEST_DAYS_OPEN, "Change Request Days Open");
        row.set(CR_LATEST_DATE, "Change Request Latest Change Date");
        row.set(CHANGE_REQUEST_LATEST_DAYS_OPEN, "Change Request Days In Current State");
        row.set(CR_LATEST_COMMENT, "Change Request Latest Comment");
        for (int i = 0; i < activeAcbs.size(); i++) {
            row.set(ONC_ACB_START + i, activeAcbs.get(i).getName());
        }
        return row;
    }

    private List<List<String>> createChangeRequestRows(final List<CertificationBodyDTO> activeAcbs,
            final Date currentDate)
                    throws EntityRetrievalException {
        LOGGER.debug("Getting pending change requests");
        List<ChangeRequest> requests = getChangeRequestsFilteredByACBs(activeAcbs);
        LOGGER.debug("Found " + requests.size() + "pending change requests");

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        for (ChangeRequest changeRequest : requests) {
            List<String> currRow = createEmptyRow(activeAcbs);
            putChangeRequestActivityInRow(changeRequest, currRow, activeAcbs, currentDate);
            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private List<ChangeRequest> getChangeRequestsFilteredByACBs(List<CertificationBodyDTO> acbs) throws EntityRetrievalException {
        List<Long> activeAcbIds = acbs.stream()
                .map(CertificationBodyDTO::getId)
                .collect(Collectors.toList());

        Predicate<ChangeRequest> doesCrHaveAppropriateAcb = cr -> cr.getCertificationBodies().stream()
                .anyMatch(acb -> activeAcbIds.contains(acb.getId()));

        return changeRequestDAO.getAllPending().stream()
                .filter(doesCrHaveAppropriateAcb)
                .sorted((cr1, cr2) -> cr1.getSubmittedDate().compareTo(cr2.getSubmittedDate()))
                .collect(Collectors.<ChangeRequest>toList());
    }

    private void putChangeRequestActivityInRow(final ChangeRequest activity,
            final List<String> currRow, final List<CertificationBodyDTO> activeAcbs, final Date currentDate) {
        // Straightforward data
        currRow.set(DEVELOPER_NAME, activity.getDeveloper().getName());
        currRow.set(DEVELOPER_CODE, activity.getDeveloper().getDeveloperCode());
        currRow.set(DEVELOPER_CONTACT_NAME, activity.getDeveloper().getContact().getFullName());
        currRow.set(DEVELOPER_CONTACT_EMAIL, activity.getDeveloper().getContact().getEmail());
        currRow.set(DEVELOPER_CONTACT_PHONE_NUMBER, activity.getDeveloper().getContact().getPhoneNumber());
        currRow.set(CR_TYPE, activity.getChangeRequestType().getName());
        currRow.set(CR_STATUS, activity.getCurrentStatus().getChangeRequestStatusType().getName());
        currRow.set(CR_DATE, getTimestampFormatter().format(activity.getSubmittedDate()));
        currRow.set(CR_LATEST_DATE, getTimestampFormatter().format(activity.getCurrentStatus().getStatusChangeDate()));
        currRow.set(CR_LATEST_COMMENT, activity.getCurrentStatus().getComment());

        // Calculated time open & time in current status
        Date changeRequestDate = activity.getSubmittedDate();
        long daysOpen = ((currentDate.getTime() - changeRequestDate.getTime()) / MILLIS_PER_DAY);
        currRow.set(CHANGE_REQUEST_DAYS_OPEN, Double.toString(daysOpen));
        Date changeRequestLatestDate = activity.getCurrentStatus().getStatusChangeDate();
        long daysLatestOpen = ((currentDate.getTime() - changeRequestLatestDate.getTime()) / MILLIS_PER_DAY);
        currRow.set(CHANGE_REQUEST_LATEST_DAYS_OPEN, Double.toString(daysLatestOpen));

        // Is the CR relevant for each ONC-ACB?
        for (int i = 0; i < activeAcbs.size(); i++) {
            boolean isRelevant = false;
            for (CertificationBody acb : activity.getCertificationBodies()) {
                if (activeAcbs.get(i).getId().equals(acb.getId())) {
                    isRelevant = true;
                }
            }
            currRow.set(ONC_ACB_START + i, isRelevant ? "Applicable" : "Not Applicable");
        }
    }

    private List<String> createEmptyRow(final List<CertificationBodyDTO> activeAcbs) {
        List<String> row = new ArrayList<String>(NUM_REPORT_COLS);
        for (int i = 0; i < NUM_REPORT_COLS + activeAcbs.size(); i++) {
            row.add("");
        }
        return row;
    }

    private DateFormat getTimestampFormatter() {
        return DateFormat.getDateTimeInstance(
                DateFormat.LONG,
                DateFormat.LONG,
                Locale.US);
    }

    private void sendEmail(JobExecutionContext jobContext, List<List<String>> csvRows, List<CertificationBodyDTO> acbs)
            throws MessagingException {
        LOGGER.info("Sending email to {} with contents {} and a total of {} pending change requests",
                getEmailRecipients(jobContext).get(0), getHtmlMessage(csvRows.size(), getAcbNamesAsCommaSeparatedList(jobContext)));

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(getEmailRecipients(jobContext))
        .subject(getSubject(jobContext))
        .htmlMessage(getHtmlMessage(csvRows.size(), getAcbNamesAsCommaSeparatedList(jobContext)))
        .fileAttachments(getAttachments(csvRows, acbs))
        .sendEmail();
    }

    private String getSubject(JobExecutionContext jobContext) {
        return env.getProperty("pendingChangeRequestEmailSubject");
    }

    private List<File> getAttachments(List<List<String>> csvRows, List<CertificationBodyDTO> acbs) {
        List<File> attachments = new ArrayList<File>();
        File csvFile = getCsvFile(csvRows, acbs);
        if (csvFile != null) {
            attachments.add(csvFile);
        }
        return attachments;
    }

    private File getCsvFile(List<List<String>> csvRows, List<CertificationBodyDTO> acbs) {
        File csvFile = null;
        if (csvRows.size() > 0) {
            String filename = env.getProperty("pendingChangeRequestReportFilename");
            if (csvRows.size() > 0) {
                csvFile = getOutputFile(csvRows, filename, acbs);
            }
        }
        return csvFile;
    }

    private String getHtmlMessage(Integer rowCount, String acbList) {
        if (rowCount > 0) {
            return String.format(env.getProperty("pendingChangeRequestHasDataEmailBody"), acbList, rowCount);
        } else {
            return String.format(env.getProperty("pendingChangeRequestNoDataEmailBody"), acbList);
        }
    }

    private List<String> getEmailRecipients(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("email"));
    }
    
    private String getAcbNamesAsCommaSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString("acb"))) {
            return Arrays.asList(
                    jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                    .map(acbId -> {
                        try {
                            return certificationBodyDAO.getById(Long.parseLong(acbId)).getName();
                        } catch (NumberFormatException | EntityRetrievalException e) {
                            LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
                            return "";
                        }
                    })
                    .collect(Collectors.joining(", "));
        } else {
            return "";
        }
    }
}
