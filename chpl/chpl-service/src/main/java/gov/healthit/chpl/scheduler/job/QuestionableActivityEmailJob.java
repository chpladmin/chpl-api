package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.Util;

public class QuestionableActivityEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("questionableActivityEmailJobLogger");
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private Properties props;

    @Autowired
    private QuestionableActivityDAO questionableActivityDao;

    @Autowired
    private Environment env;

    private static final int NUM_REPORT_COLS = 13;
    private static final int ACB_COL = 0;
    private static final int DEVELOPER_COL = 1;
    private static final int PRODUCT_COL = 2;
    private static final int VERSION_COL = 3;
    private static final int LISTING_COL = 4;
    private static final int STATUS_COL = 5;
    private static final int LINK_COL = 6;
    private static final int ACTIVITY_DATE_COL = 7;
    private static final int ACTIVITY_USER_COL = 8;
    private static final int ACTIVITY_TYPE_COL = 9;
    private static final int ACTIVITY_DESCRIPTION_COL = 10;
    private static final int ACTIVITY_CERT_STATUS_CHANGE_REASON_COL = 11;
    private static final int ACTIVITY_REASON_COL = 12;

    private Integer minRangeInDays = 1;
    private Integer maxRangeInDays = 365;
    private static final Integer DEFAULT_RANGE = 7;

    /**
     * Constructor that initializes the QuestionableActivityEmailJob object.
     * 
     * @throws Exception
     *             if thrown
     */
    public QuestionableActivityEmailJob() throws Exception {
        super();
        loadProperties();
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Questionable Activity Email job. *********");
        LOGGER.info("Creating questionable activity email for: " + jobContext.getMergedJobDataMap().getString("email"));

        populateRangeDefaultsFromJobData(jobContext);
        LOGGER.info("Valid range read from job context: " + minRangeInDays + " - " + maxRangeInDays);

        String errors = "";
        Integer range = getRangeInDays(jobContext);
        if (!isRangeValid(range)) {
            errors = String.format("Range is invalid.  It must be numeric and between %d and %d.  Using %d.", minRangeInDays,
                    maxRangeInDays, range);
            LOGGER.error(errors);
        }

        Calendar end = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, range * -1);
        List<List<String>> csvRows = getAppropriateActivities(jobContext, start.getTime(), end.getTime());
        String to = jobContext.getMergedJobDataMap().getString("email");
        String subject = props.getProperty("questionableActivityEmailSubject");
        String htmlMessage = null;
        List<File> files = null;
        if (csvRows != null && csvRows.size() > 0) {
            htmlMessage = String.format(props.getProperty("questionableActivityHasDataEmailBody"), Util
                    .getDateFormatter().format(start.getTime()), Util.getDateFormatter().format(end.getTime()));
            String filename = props.getProperty("questionableActivityReportFilename");
            File output = null;
            files = new ArrayList<File>();
            if (csvRows.size() > 0) {
                output = getOutputFile(csvRows, filename);
                files.add(output);
            }
        } else {
            htmlMessage = String.format(props.getProperty("questionableActivityNoDataEmailBody"), Util
                    .getDateFormatter().format(start.getTime()), Util.getDateFormatter().format(end.getTime()));
        }

        // Add line in email if we had to use to default range value.
        if (errors != "") {
            htmlMessage = errors + "<br />" + htmlMessage;
        }

        LOGGER.info("Sending email to {} with contents {} and a total of {} questionable activities", to, htmlMessage,
                csvRows.size());

        try {
            List<String> recipients = new ArrayList<String>();
            recipients.add(to);

            EmailBuilder emailBuilder = new EmailBuilder(env);
            emailBuilder.recipients(recipients).subject(subject).htmlMessage(htmlMessage).fileAttachments(files)
                    .sendEmail();

        } catch (MessagingException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Questionable Activity Email job. *********");
    }

    private Integer getRangeInDays(final JobExecutionContext jobContext) {
        Integer range = DEFAULT_RANGE;
        if (jobContext.getMergedJobDataMap().containsKey("range")) {
            try {
                range = jobContext.getMergedJobDataMap().getInt("range");
            } catch (ClassCastException e) {
                LOGGER.info("Range is not stored as an integer.  Using the default value of: " + range);
            }
        }
        return range;
    }

    private Boolean isRangeValid(Integer range) {
        return range >= minRangeInDays && range <= maxRangeInDays;
    }

    private List<List<String>> getAppropriateActivities(final JobExecutionContext jobContext, final Date start,
            final Date end) {
        List<List<String>> activities = new ArrayList<List<String>>();
        activities.addAll(createListingActivityRows(start, end));
        activities.addAll(createCriteriaActivityRows(start, end));
        activities.addAll(createDeveloperActivityRows(start, end));
        activities.addAll(createProductActivityRows(start, end));
        activities.addAll(createVersionActivityRows(start, end));
        return activities;
    }

    private File getOutputFile(final List<List<String>> rows, final String reportFilename) {
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp), Charset.forName("UTF-8")
                    .newEncoder()); CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                csvPrinter.printRecord(getHeaderRow());
                for (List<String> rowValue : rows) {
                    csvPrinter.printRecord(rowValue);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return temp;
    }

    private List<String> getHeaderRow() {
        List<String> row = new ArrayList<String>();
        row.add("ONC-ACB");
        row.add("Developer");
        row.add("Product");
        row.add("Version");
        row.add("CHPL Product Number");
        row.add("Current Certification Status");
        row.add("Link");
        row.add("Activity Timestamp");
        row.add("Responsible User");
        row.add("Activity Type");
        row.add("Activity");
        row.add("Reason for Status Change");
        row.add("Reason");
        return row;
    }

    private List<List<String>> createCriteriaActivityRows(final Date startDate, final Date endDate) {
        LOGGER.debug("Getting certification result activity between " + startDate + " and " + endDate);
        List<QuestionableActivityCertificationResultDTO> certResultActivities = questionableActivityDao
                .findCertificationResultActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + certResultActivities.size() + " questionable certification result activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityCertificationResultDTO>> activityByGroup = new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityCertificationResultDTO>>();
        for (QuestionableActivityCertificationResultDTO activity : certResultActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(), activity
                    .getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityCertificationResultDTO> activitiesForGroup = new ArrayList<QuestionableActivityCertificationResultDTO>();
                activitiesForGroup.add(activity);
                activityByGroup.put(groupKey, activitiesForGroup);
            } else {
                List<QuestionableActivityCertificationResultDTO> existingActivitiesForGroup = activityByGroup.get(
                        groupKey);
                existingActivitiesForGroup.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<String> currRow = createEmptyRow();
            currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
            currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

            List<QuestionableActivityCertificationResultDTO> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityCertificationResultDTO activity : activitiesForGroup) {
                putCertResultActivityInRow(activity, currRow);
            }

            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private List<List<String>> createListingActivityRows(final Date startDate, final Date endDate) {
        LOGGER.debug("Getting listing activity between " + startDate + " and " + endDate);
        List<QuestionableActivityListingDTO> listingActivities = questionableActivityDao
                .findListingActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + listingActivities.size() + " questionable listing activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityListingDTO>> activityByGroup = new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityListingDTO>>();
        for (QuestionableActivityListingDTO activity : listingActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(), activity
                    .getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityListingDTO> activitiesForDate = new ArrayList<QuestionableActivityListingDTO>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityListingDTO> existingActivitiesForDate = activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<String> currRow = createEmptyRow();
            currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
            currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

            List<QuestionableActivityListingDTO> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityListingDTO activity : activitiesForGroup) {
                putListingActivityInRow(activity, currRow);
            }

            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private List<List<String>> createDeveloperActivityRows(final Date startDate, final Date endDate) {
        LOGGER.debug("Getting developer activity between " + startDate + " and " + endDate);
        List<QuestionableActivityDeveloperDTO> developerActivities = questionableActivityDao
                .findDeveloperActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + developerActivities.size() + " questionable developer activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityDeveloperDTO>> activityByGroup = new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityDeveloperDTO>>();
        for (QuestionableActivityDeveloperDTO activity : developerActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(), activity
                    .getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityDeveloperDTO> activitiesForDate = new ArrayList<QuestionableActivityDeveloperDTO>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityDeveloperDTO> existingActivitiesForDate = activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<String> currRow = createEmptyRow();
            currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
            currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

            List<QuestionableActivityDeveloperDTO> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityDeveloperDTO activity : activitiesForGroup) {
                putDeveloperActivityInRow(activity, currRow);
            }

            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private List<List<String>> createProductActivityRows(final Date startDate, final Date endDate) {
        LOGGER.debug("Getting product activity between " + startDate + " and " + endDate);
        List<QuestionableActivityProductDTO> productActivities = questionableActivityDao
                .findProductActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + productActivities.size() + " questionable developer activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityProductDTO>> activityByGroup = new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityProductDTO>>();
        for (QuestionableActivityProductDTO activity : productActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(), activity
                    .getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityProductDTO> activitiesForDate = new ArrayList<QuestionableActivityProductDTO>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityProductDTO> existingActivitiesForDate = activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<String> currRow = createEmptyRow();
            currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
            currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

            List<QuestionableActivityProductDTO> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityProductDTO activity : activitiesForGroup) {
                putProductActivityInRow(activity, currRow);
            }

            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private List<List<String>> createVersionActivityRows(final Date startDate, final Date endDate) {
        LOGGER.debug("Getting version activity between " + startDate + " and " + endDate);
        List<QuestionableActivityVersionDTO> versionActivities = questionableActivityDao
                .findVersionActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + versionActivities.size() + " questionable developer activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityVersionDTO>> activityByGroup = new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityVersionDTO>>();
        for (QuestionableActivityVersionDTO activity : versionActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(), activity
                    .getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityVersionDTO> activitiesForDate = new ArrayList<QuestionableActivityVersionDTO>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityVersionDTO> existingActivitiesForDate = activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<String> currRow = createEmptyRow();
            currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
            currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

            List<QuestionableActivityVersionDTO> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityVersionDTO activity : activitiesForGroup) {
                putVersionActivityInRow(activity, currRow);
            }

            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private void putListingActivityInRow(final QuestionableActivityListingDTO activity, final List<String> currRow) {
        // fill in info about the listing that will be the same for every
        // activity found in this date bucket
        currRow.set(ACB_COL, activity.getListing().getCertificationBodyName());
        currRow.set(DEVELOPER_COL, activity.getListing().getDeveloper().getName());
        currRow.set(PRODUCT_COL, activity.getListing().getProduct().getName());
        currRow.set(VERSION_COL, activity.getListing().getVersion().getVersion());
        currRow.set(LISTING_COL, activity.getListing().getChplProductNumber());
        currRow.set(STATUS_COL, activity.getListing().getCertificationStatusName());
        currRow.set(LINK_COL, props.getProperty("chplUrlBegin") + "/#/admin/reports/" + activity.getListing().getId());
        currRow.set(ACTIVITY_USER_COL, activity.getUser().getSubjectName());

        if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.CRITERIA_ADDED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.CRITERIA_REMOVED
                .getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getBefore();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.CQM_ADDED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.CQM_REMOVED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getBefore();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.SURVEILLANCE_REMOVED
                .getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "TRUE");
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.EDITION_2011_EDITED
                .getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "TRUE");
        } else if (activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        } else if (activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_DATE_EDITED_CURRENT.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        } else if (activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.TESTING_LAB_CHANGED
                .getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        }
        currRow.set(ACTIVITY_REASON_COL, activity.getReason());
    }

    private void putCertResultActivityInRow(final QuestionableActivityCertificationResultDTO activity,
            final List<String> currRow) {
        // fill in info about the listing that will be the same for every
        // activity found in this date bucket
        currRow.set(ACB_COL, activity.getListing().getCertificationBodyName());
        currRow.set(DEVELOPER_COL, activity.getListing().getDeveloper().getName());
        currRow.set(PRODUCT_COL, activity.getListing().getProduct().getName());
        currRow.set(VERSION_COL, activity.getListing().getVersion().getVersion());
        currRow.set(LISTING_COL, activity.getListing().getChplProductNumber());
        currRow.set(STATUS_COL, activity.getListing().getCertificationStatusName());
        currRow.set(LINK_COL, props.getProperty("chplUrlBegin") + "/#/admin/reports/" + activity.getListing().getId());
        currRow.set(ACTIVITY_USER_COL, activity.getUser().getSubjectName());

        if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": from " + activity.getBefore() + " to "
                    + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.G1_MEASURE_ADDED
                .getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED
                .getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getBefore();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED
                .getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": from " + activity.getBefore() + " to "
                    + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.G2_MEASURE_ADDED
                .getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED
                .getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getBefore();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.GAP_EDITED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += activity.getCertResult().getNumber() + " from " + activity.getBefore() + " to "
                    + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        }
        currRow.set(ACTIVITY_REASON_COL, activity.getReason());
    }

    private void putDeveloperActivityInRow(final QuestionableActivityDeveloperDTO developerActivity,
            final List<String> activityRow) {
        activityRow.set(DEVELOPER_COL, developerActivity.getDeveloper().getName());
        activityRow.set(ACTIVITY_USER_COL, developerActivity.getUser().getSubjectName());
        if (developerActivity.getReason() != null) {
            activityRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, developerActivity.getReason());
        }

        if (developerActivity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED
                .getName())) {
            activityRow.set(ACTIVITY_DESCRIPTION_COL, "From " + developerActivity.getBefore() + " to "
                    + developerActivity.getAfter());
        } else if (developerActivity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "From " + developerActivity.getBefore() + " to " + developerActivity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (developerActivity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Added status " + developerActivity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (developerActivity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Removed status " + developerActivity.getBefore();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (developerActivity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Changed status from " + developerActivity.getBefore() + " to " + developerActivity
                    .getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        }
    }

    private void putProductActivityInRow(final QuestionableActivityProductDTO activity,
            final List<String> activityRow) {
        activityRow.set(DEVELOPER_COL, activity.getProduct().getDeveloperName());
        activityRow.set(PRODUCT_COL, activity.getProduct().getName());
        activityRow.set(ACTIVITY_USER_COL, activity.getUser().getSubjectName());

        if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED.getName())) {
            activityRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED
                .getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "From " + activity.getBefore() + " to " + activity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED
                .getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Added owner " + activity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Removed owner " + activity.getBefore();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Changed owner from " + activity.getBefore() + " to " + activity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        }
    }

    private void putVersionActivityInRow(final QuestionableActivityVersionDTO activity,
            final List<String> activityRow) {
        activityRow.set(DEVELOPER_COL, activity.getVersion().getDeveloperName());
        activityRow.set(PRODUCT_COL, activity.getVersion().getProductName());
        activityRow.set(VERSION_COL, activity.getVersion().getVersion());
        activityRow.set(ACTIVITY_USER_COL, activity.getUser().getSubjectName());

        if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.VERSION_NAME_EDITED.getName())) {
            activityRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
        }
    }

    private List<String> createEmptyRow() {
        List<String> row = new ArrayList<String>(NUM_REPORT_COLS);
        for (int i = 0; i < NUM_REPORT_COLS; i++) {
            row.add("");
        }
        return row;
    }

    private Properties loadProperties() throws IOException {
        InputStream in = QuestionableActivityEmailJob.class.getClassLoader().getResourceAsStream(
                DEFAULT_PROPERTIES_FILE);
        if (in == null) {
            props = null;
            throw new FileNotFoundException("Environment Properties File not found in class path.");
        } else {
            props = new Properties();
            props.load(in);
            in.close();
        }
        return props;
    }

    private void populateRangeDefaultsFromJobData(final JobExecutionContext context) {
        ObjectMapper mapper = new ObjectMapper();
        String parametersJson = context.getMergedJobDataMap().getString("parameters");
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(parametersJson);
        } catch (Exception e) {
            LOGGER.error(
                    "Could not determine min and max range values.  "
                            + "Using default: " + minRangeInDays + " - " + maxRangeInDays);
            return;
        }
        if (rootNode != null) {
            JsonNode minNode = rootNode.findValue("min");
            if (minNode != null) {
                minRangeInDays = minNode.asInt();
            }
            JsonNode maxNode = rootNode.findValue("max");
            if (maxNode != null) {
                maxRangeInDays = maxNode.asInt();
            }
        }
    }

    private static class ActivityDateTriggerGroup {
        private Date activityDate;
        private QuestionableActivityTriggerDTO trigger;

        ActivityDateTriggerGroup(final Date activityDate, final QuestionableActivityTriggerDTO trigger) {
            this.activityDate = activityDate;
            this.trigger = trigger;
        }

        @Override
        public boolean equals(final Object anotherObject) {
            if (anotherObject == null) {
                return false;
            }
            if (!(anotherObject instanceof ActivityDateTriggerGroup)) {
                return false;
            }
            ActivityDateTriggerGroup anotherGroup = (ActivityDateTriggerGroup) anotherObject;
            if (this.activityDate == null || anotherGroup.activityDate == null || this.trigger == null
                    || anotherGroup.trigger == null) {
                return false;
            }
            if (this.activityDate.getTime() == anotherGroup.activityDate.getTime() && (this.trigger.getId()
                    .longValue() == anotherGroup.trigger.getId().longValue() || this.trigger.getName().equals(
                            anotherGroup.trigger.getName()))) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (this.activityDate == null || this.trigger == null) {
                return -1;
            }
            return this.activityDate.hashCode() + this.trigger.getName().hashCode();
        }

        public Date getActivityDate() {
            return activityDate;
        }

        public QuestionableActivityTriggerDTO getTrigger() {
            return trigger;
        }
    }
}
