package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.questionableactivity.QuestionableActivityDAO;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityDeveloper;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityProduct;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityVersion;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.Util;

public class QuestionableActivityEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("questionableActivityEmailJobLogger");

    @Autowired
    private QuestionableActivityDAO questionableActivityDao;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Value("${questionableActivityEmailSubject}")
    private String emailSubject;

    @Value("${questionableActivityEmailBodyTitle}")
    private String emailTitle;

    @Value("${questionableActivityHasDataEmailBody}")
    private String emailBodyWithData;

    @Value("${questionableActivityNoDataEmailBody}")
    private String emailBodyEmptyData;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    private static final int NUM_REPORT_COLS = 14;
    private static final int ACB_COL = 0;
    private static final int DEVELOPER_COL = 1;
    private static final int PRODUCT_COL = 2;
    private static final int VERSION_COL = 3;
    private static final int LISTING_COL = 4;
    private static final int STATUS_COL = 5;
    private static final int LINK_COL = 6;
    private static final int ACTIVITY_DATE_COL = 7;
    private static final int ACTIVITY_USER_COL = 8;
    private static final int ACTIVITY_LEVEL_COL = 9;
    private static final int ACTIVITY_TYPE_COL = 10;
    private static final int ACTIVITY_DESCRIPTION_COL = 11;
    private static final int ACTIVITY_CERT_STATUS_CHANGE_REASON_COL = 12;
    private static final int ACTIVITY_REASON_COL = 13;

    private static final Integer MIN_RANGE_IN_DAYS = 1;
    private static final Integer MAX_RANGE_IN_DAYS = 365;
    private Range<Integer> rangeInDays = Range.between(MIN_RANGE_IN_DAYS, MAX_RANGE_IN_DAYS);
    private static final Integer DEFAULT_RANGE = 7;

    /**
     * Constructor that initializes the QuestionableActivityEmailJob object.
     *
     * @throws Exception if thrown
     */
    public QuestionableActivityEmailJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Questionable Activity Email job. *********");
        LOGGER.info("Creating questionable activity email for: " + jobContext.getMergedJobDataMap().getString("email"));

        populateRangeDefaultsFromJobData(jobContext);
        LOGGER.info(
                "Valid range read from job context: " + rangeInDays.getMinimum() + " - " + rangeInDays.getMaximum());

        String errors = "";
        Integer range = getRangeInDays(jobContext);
        if (!rangeInDays.contains(range)) {
            errors = String.format("Range is invalid.  It must be numeric and between %d and %d.  Using %d.",
                    rangeInDays.getMinimum(), rangeInDays.getMaximum(), range);
            LOGGER.error(errors);
        }

        Calendar end = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, range * -1);
        List<List<String>> csvRows = getAppropriateActivities(jobContext, start.getTime(), end.getTime());
        String to = jobContext.getMergedJobDataMap().getString("email");
        String htmlMessage = buildHtmlMessage(csvRows, start, end);
        List<File> attachments = null;
        if (csvRows != null && csvRows.size() > 0) {
            String filename = env.getProperty("questionableActivityReportFilename");
            File output = null;
            attachments = new ArrayList<File>();
            if (csvRows.size() > 0) {
                output = getOutputFile(csvRows, filename);
                attachments.add(output);
            }
        }

        LOGGER.info("Sending email to {} with contents {} and a total of {} questionable activities", to, htmlMessage,
                csvRows.size());

        try {
            List<String> recipients = new ArrayList<String>();
            recipients.add(to);

            chplEmailFactory.emailBuilder()
                .recipients(recipients)
                .subject(emailSubject)
                .htmlMessage(htmlMessage)
                .fileAttachments(attachments)
                .sendEmail();
        } catch (EmailNotSentException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Questionable Activity Email job. *********");
    }

    private String buildHtmlMessage(List<List<String>> csvRows, Calendar start, Calendar end) {
        if (csvRows != null && csvRows.size() > 0) {
            String formattedEmailBody = String.format(emailBodyWithData, Util.getDateFormatter().format(start.getTime()), Util.getDateFormatter().format(end.getTime()));
            return chplHtmlEmailBuilder.initialize()
                    .heading(emailTitle)
                    .paragraph(null, formattedEmailBody)
                    .footer(AdminFooter.class)
                    .build();
        } else {
            String formattedEmailBody = String.format(emailBodyEmptyData, Util.getDateFormatter().format(start.getTime()), Util.getDateFormatter().format(end.getTime()));
            return chplHtmlEmailBuilder.initialize()
                    .heading(emailTitle)
                    .paragraph(null, formattedEmailBody)
                    .footer(AdminFooter.class)
                    .build();
        }
    }

    private Integer getRangeInDays(JobExecutionContext jobContext) {
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

    private List<List<String>> getAppropriateActivities(JobExecutionContext jobContext, Date start, Date end) {
        List<List<String>> activities = new ArrayList<List<String>>();
        activities.addAll(createListingActivityRows(start, end));
        activities.addAll(createCriteriaActivityRows(start, end));
        activities.addAll(createDeveloperActivityRows(start, end));
        activities.addAll(createProductActivityRows(start, end));
        activities.addAll(createVersionActivityRows(start, end));
        return activities;
    }

    private File getOutputFile(List<List<String>> rows, String reportFilename) {
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp),
                    Charset.forName("UTF-8").newEncoder());
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                writer.write('\ufeff');
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
        row.add("Activity Level");
        row.add("Activity Type");
        row.add("Activity");
        row.add("Reason for Status Change");
        row.add("Reason");
        return row;
    }

    private List<List<String>> createCriteriaActivityRows(Date startDate, Date endDate) {
        LOGGER.debug("Getting certification result activity between " + startDate + " and " + endDate);
        List<QuestionableActivityCertificationResult> certResultActivities = questionableActivityDao
                .findCertificationResultActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + certResultActivities.size() + " questionable certification result activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityCertificationResult>> activityByGroup =
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityCertificationResult>>();

        for (QuestionableActivityCertificationResult activity : certResultActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(),
                    activity.getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityCertificationResult> activitiesForGroup =
                        new ArrayList<QuestionableActivityCertificationResult>();
                activitiesForGroup.add(activity);
                activityByGroup.put(groupKey, activitiesForGroup);
            } else {
                List<QuestionableActivityCertificationResult> existingActivitiesForGroup = activityByGroup
                        .get(groupKey);
                existingActivitiesForGroup.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<String> currRow = createEmptyRow();
            currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
            currRow.set(ACTIVITY_LEVEL_COL, activityGroup.getTrigger().getLevel());
            currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

            List<QuestionableActivityCertificationResult> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityCertificationResult activity : activitiesForGroup) {
                putCertResultActivityInRow(activity, currRow);
            }

            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private List<List<String>> createListingActivityRows(Date startDate, Date endDate) {
        LOGGER.debug("Getting listing activity between " + startDate + " and " + endDate);
        List<QuestionableActivityListing> listingActivities = questionableActivityDao
                .findListingActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + listingActivities.size() + " questionable listing activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityListing>> activityByGroup =
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityListing>>();

        for (QuestionableActivityListing activity : listingActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(),
                    activity.getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityListing> activitiesForDate = new ArrayList<QuestionableActivityListing>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityListing> existingActivitiesForDate = activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<QuestionableActivityListing> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityListing activity : activitiesForGroup) {
                List<String> currRow = createEmptyRow();
                currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
                currRow.set(ACTIVITY_LEVEL_COL, activityGroup.getTrigger().getLevel());
                currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());
                putListingActivityInRow(activity, currRow);
                activityCsvRows.add(currRow);
            }
        }
        return activityCsvRows;
    }

    private List<List<String>> createDeveloperActivityRows(Date startDate, Date endDate) {
        LOGGER.debug("Getting developer activity between " + startDate + " and " + endDate);
        List<QuestionableActivityDeveloper> developerActivities = questionableActivityDao
                .findDeveloperActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + developerActivities.size() + " questionable developer activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityDeveloper>> activityByGroup =
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityDeveloper>>();

        for (QuestionableActivityDeveloper activity : developerActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(),
                    activity.getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityDeveloper> activitiesForDate = new ArrayList<QuestionableActivityDeveloper>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityDeveloper> existingActivitiesForDate = activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<String> currRow = createEmptyRow();
            currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
            currRow.set(ACTIVITY_LEVEL_COL, activityGroup.getTrigger().getLevel());
            currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

            List<QuestionableActivityDeveloper> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityDeveloper activity : activitiesForGroup) {
                putDeveloperActivityInRow(activity, currRow);
            }

            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private List<List<String>> createProductActivityRows(Date startDate, Date endDate) {
        LOGGER.debug("Getting product activity between " + startDate + " and " + endDate);
        List<QuestionableActivityProduct> productActivities = questionableActivityDao
                .findProductActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + productActivities.size() + " questionable developer activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityProduct>> activityByGroup =
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityProduct>>();

        for (QuestionableActivityProduct activity : productActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(),
                    activity.getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityProduct> activitiesForDate = new ArrayList<QuestionableActivityProduct>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityProduct> existingActivitiesForDate = activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<String> currRow = createEmptyRow();
            currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
            currRow.set(ACTIVITY_LEVEL_COL, activityGroup.getTrigger().getLevel());
            currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

            List<QuestionableActivityProduct> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityProduct activity : activitiesForGroup) {
                putProductActivityInRow(activity, currRow);
            }

            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private List<List<String>> createVersionActivityRows(Date startDate, Date endDate) {
        LOGGER.debug("Getting version activity between " + startDate + " and " + endDate);
        List<QuestionableActivityVersion> versionActivities = questionableActivityDao
                .findVersionActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + versionActivities.size() + " questionable developer activities");

        // create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityVersion>> activityByGroup =
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityVersion>>();

        for (QuestionableActivityVersion activity : versionActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(activity.getActivityDate(),
                    activity.getTrigger());

            if (activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityVersion> activitiesForDate = new ArrayList<QuestionableActivityVersion>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityVersion> existingActivitiesForDate = activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }

        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for (ActivityDateTriggerGroup activityGroup : activityGroups) {
            List<String> currRow = createEmptyRow();
            currRow.set(ACTIVITY_DATE_COL, Util.getTimestampFormatter().format(activityGroup.getActivityDate()));
            currRow.set(ACTIVITY_LEVEL_COL, activityGroup.getTrigger().getLevel());
            currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

            List<QuestionableActivityVersion> activitiesForGroup = activityByGroup.get(activityGroup);
            for (QuestionableActivityVersion activity : activitiesForGroup) {
                putVersionActivityInRow(activity, currRow);
            }

            activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }

    private void putListingActivityInRow(QuestionableActivityListing activity, List<String> currRow) {
        // fill in info about the listing that will be the same for every
        // activity found in this date bucket
        currRow.set(ACB_COL, activity.getListing().getCertificationBodyName());
        currRow.set(DEVELOPER_COL, activity.getListing().getDeveloper().getName());
        currRow.set(PRODUCT_COL, activity.getListing().getProduct().getName());
        currRow.set(VERSION_COL, activity.getListing().getVersion().getVersion());
        currRow.set(LISTING_COL, activity.getListing().getChplProductNumber());
        currRow.set(STATUS_COL, activity.getListing().getCertificationStatusName());
        currRow.set(LINK_COL, env.getProperty("chplUrlBegin") + env.getProperty("listingReportsUrlPart") + "/"
                + activity.getListing().getId());
        currRow.set(ACTIVITY_USER_COL, activity.getUser().getUsername());

        if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.CRITERIA_REMOVED.getName())) {
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
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.SURVEILLANCE_REMOVED.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "TRUE");
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.EDITION_2011_EDITED.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "TRUE");
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_DATE_EDITED_CURRENT.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        } else if (activity.getTrigger().getName()
                    .equals(QuestionableActivityTriggerConcept.CERTIFICATION_DATE_EDITED.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_HISTORY.getName())) {
            if (activity.getBefore() == null) {
                currRow.set(ACTIVITY_DESCRIPTION_COL, "Added " + activity.getAfter());
            } else if (activity.getAfter() == null) {
                currRow.set(ACTIVITY_DESCRIPTION_COL, "Removed " + activity.getBefore());
            } else if (activity.getBefore() != null && activity.getAfter() != null) {
                currRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
            }
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.FUTURE_CERTIFICATION_STATUS.getName())) {
            if (activity.getBefore() == null) {
                currRow.set(ACTIVITY_DESCRIPTION_COL, "Added " + activity.getAfter());
            }
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.TESTING_LAB_CHANGED.getName())) {
            if (!StringUtils.isEmpty(activity.getBefore()) && !StringUtils.isEmpty(activity.getAfter())) {
                currRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
            } else if (!StringUtils.isEmpty(activity.getBefore())) {
                currRow.set(ACTIVITY_DESCRIPTION_COL, activity.getBefore());
            } else if (!StringUtils.isEmpty(activity.getAfter())) {
                currRow.set(ACTIVITY_DESCRIPTION_COL, activity.getAfter());
            } else {
                currRow.set(ACTIVITY_DESCRIPTION_COL, "Unknown change");
            }
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.MEASURE_REMOVED
                .getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "Removed " + activity.getBefore());
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.REAL_WORLD_TESTING_ADDED.getName())) {
          currRow.set(ACTIVITY_DESCRIPTION_COL, activity.getAfter());
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.REAL_WORLD_TESTING_REMOVED.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, activity.getBefore());
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.PROMOTING_INTEROPERABILITY_UPDATED_BY_ACB.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, activity.getAfter());
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.RWT_PLANS_UPDATED_OUTSIDE_NORMAL_PERIOD.getName())
                || activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.RWT_RESULTS_UPDATED_OUTSIDE_NORMAL_PERIOD.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, activity.getBefore() + " updated to " + activity.getAfter());
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.CURES_UPDATE_REMOVED.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, activity.getBefore() + " updated to " + activity.getAfter());
        }
        currRow.set(ACTIVITY_REASON_COL, activity.getReason());
    }

    private void putCertResultActivityInRow(QuestionableActivityCertificationResult activity, List<String> currRow) {
        // fill in info about the listing that will be the same for every
        // activity found in this date bucket
        currRow.set(ACB_COL, activity.getListing().getCertificationBodyName());
        currRow.set(DEVELOPER_COL, activity.getListing().getDeveloper().getName());
        currRow.set(PRODUCT_COL, activity.getListing().getProduct().getName());
        currRow.set(VERSION_COL, activity.getListing().getVersion().getVersion());
        currRow.set(LISTING_COL, activity.getListing().getChplProductNumber());
        currRow.set(STATUS_COL, activity.getListing().getCertificationStatusName());
        currRow.set(LINK_COL, env.getProperty("chplUrlBegin") + env.getProperty("listingReportsUrlPart") + "/" + activity.getListing().getId());
        currRow.set(ACTIVITY_USER_COL, activity.getUser().getUsername());

        String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
        if (!StringUtils.isEmpty(currActivityRowValue)) {
            currActivityRowValue += "; ";
        }

        if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED.getName())
                || activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED.getName())
                || activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.GAP_EDITED.getName())) {
            currActivityRowValue += formatCriteriaNumber(activity.getCertResult()) + ": from " + activity.getBefore() + " to " + activity.getAfter();
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.REPLACED_SVAP_ADDED.getName())) {
           currActivityRowValue += formatCriteriaNumber(activity.getCertResult()) + ": " + activity.getAfter();
        } else if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.REMOVED_CODE_SET.getName())) {
            currActivityRowValue += formatCriteriaNumber(activity.getCertResult()) + ": Removed " + activity.getBefore();
        }

        currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        currRow.set(ACTIVITY_REASON_COL, activity.getReason());
    }

    private void putDeveloperActivityInRow(QuestionableActivityDeveloper developerActivity, List<String> activityRow) {
        activityRow.set(DEVELOPER_COL, developerActivity.getDeveloper().getName());
        activityRow.set(ACTIVITY_USER_COL, developerActivity.getUser().getUsername());
        if (developerActivity.getReason() != null) {
            activityRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, developerActivity.getReason());
        }

        if (developerActivity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED.getName())) {
            activityRow.set(ACTIVITY_DESCRIPTION_COL,
                    "From " + developerActivity.getBefore() + " to " + developerActivity.getAfter());
        } else if (developerActivity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "From " + developerActivity.getBefore() + " to " + developerActivity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (developerActivity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Added status " + developerActivity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (developerActivity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Removed status " + developerActivity.getBefore();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (developerActivity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Changed status from " + developerActivity.getBefore() + " to "
                    + developerActivity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        }
    }

    private void putProductActivityInRow(QuestionableActivityProduct activity, List<String> activityRow) {
        activityRow.set(DEVELOPER_COL, activity.getProduct().getOwner().getName());
        activityRow.set(PRODUCT_COL, activity.getProduct().getName());
        activityRow.set(ACTIVITY_USER_COL, activity.getUser().getUsername());

        if (activity.getTrigger().getName().equals(QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED.getName())) {
            activityRow.set(ACTIVITY_DESCRIPTION_COL, "From " + activity.getBefore() + " to " + activity.getAfter());
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "From " + activity.getBefore() + " to " + activity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Added owner " + activity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Removed owner " + activity.getBefore();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if (activity.getTrigger().getName()
                .equals(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if (!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; ";
            }
            currActivityRowValue += "Changed owner from " + activity.getBefore() + " to " + activity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        }
    }

    private void putVersionActivityInRow(QuestionableActivityVersion activity, List<String> activityRow) {
        activityRow.set(DEVELOPER_COL, activity.getVersion().getDeveloperName());
        activityRow.set(PRODUCT_COL, activity.getVersion().getProductName());
        activityRow.set(VERSION_COL, activity.getVersion().getVersion());
        activityRow.set(ACTIVITY_USER_COL, activity.getUser().getUsername());

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

    private void populateRangeDefaultsFromJobData(JobExecutionContext context) {
        ObjectMapper mapper = new ObjectMapper();
        String parametersJson = context.getMergedJobDataMap().getString("parameters");
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(parametersJson);
        } catch (Exception e) {
            LOGGER.error(String.format("Could not determine min and max range values.  Using default: %d - %d.",
                    rangeInDays.getMinimum(), rangeInDays.getMaximum()));
            return;
        }
        if (rootNode != null) {
            Integer min = rangeInDays.getMinimum();
            Integer max = rangeInDays.getMaximum();
            JsonNode minNode = rootNode.findValue("min");
            if (minNode != null) {
                min = minNode.asInt();
            }
            JsonNode maxNode = rootNode.findValue("max");
            if (maxNode != null) {
                max = maxNode.asInt();
            }
            rangeInDays = Range.between(min, max);
        }
    }

    private String formatCriteriaNumber(CertificationResultDetailsDTO certResult) {
        CertificationCriterion criterion = new CertificationCriterion();
        criterion.setNumber(certResult.getNumber());
        criterion.setTitle(certResult.getTitle());
        criterion.setId(certResult.getCertificationCriterionId());
        return CertificationCriterionService.formatCriteriaNumber(criterion);
    }

    private static class ActivityDateTriggerGroup {
        private Date activityDate;
        private QuestionableActivityTrigger trigger;

        ActivityDateTriggerGroup(Date activityDate, QuestionableActivityTrigger trigger) {
            this.activityDate = activityDate;
            this.trigger = trigger;
        }

        @Override
        public boolean equals(Object anotherObject) {
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
            if (this.activityDate.getTime() == anotherGroup.activityDate.getTime()
                    && (this.trigger.getId().longValue() == anotherGroup.trigger.getId().longValue()
                    || this.trigger.getName().equals(anotherGroup.trigger.getName()))) {
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

        public QuestionableActivityTrigger getTrigger() {
            return trigger;
        }
    }
}
