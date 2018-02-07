package gov.healthit.chpl.app.questionableActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.Util;
import gov.healthit.chpl.app.App;
import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.dao.NotificationDAO;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;

public class QuestionableActivityReportApp extends App {
    private static final Logger LOGGER = LogManager.getLogger(QuestionableActivityReportApp.class);
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
    private Date startDate, endDate;
    
    protected QuestionableActivityDAO qaDao;
    protected NotificationDAO notificationDao;

    public QuestionableActivityReportApp() {
    }
    
    public QuestionableActivityReportApp(Date startDate, Date endDate) {
        this();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    protected void initiateSpringBeans(AbstractApplicationContext context) throws IOException {
        this.setQaDao((QuestionableActivityDAO) context.getBean("questionableActivityDao"));
        this.setNotificationDao((NotificationDAO) context.getBean("notificationDAO"));
    }
    
    protected void runJob() throws IOException, MessagingException {
        //generate header
        List<String> headerRows = createHeader();
        
        //generate all of the data rows
        List<List<String>> listingActivityRows = createListingActivityRows();
        List<List<String>> criteriaActivityRows = createCriteriaActivityRows();
        List<List<String>> developerActivityRows = createDeveloperActivityRows();
        List<List<String>> productActivityRows = createProductActivityRows();
        List<List<String>> versionActivityRows = createVersionActivityRows();
        
        List<File> filesToEmail = new ArrayList<File>();
        String emailBody = "";
        
        //write out listing, developer, product, and version activities
        if((listingActivityRows == null || listingActivityRows.size() == 0) &&
           (criteriaActivityRows == null || criteriaActivityRows.size() == 0) &&
           (developerActivityRows == null || developerActivityRows.size() == 0) &&
           (productActivityRows == null || productActivityRows.size() == 0) &&
           (versionActivityRows == null || versionActivityRows.size() == 0)) {
            //send no activity email
            emailBody = "<p>No questionable activity was found between " + 
                Util.getDateFormatter().format(startDate) + " and " + 
                Util.getDateFormatter().format(endDate) + ".</p>";
        } else {
            FileWriter writer = null;
            CSVPrinter csvPrinter = null;
            File reportFile = null;
            try {
                String reportFilename = this.getProperties().getProperty("questionableActivityReportFilename");
                reportFile = new File(
                        this.getDownloadFolder().getAbsolutePath() + File.separator + 
                        reportFilename);
                writer = new FileWriter(reportFile);
                csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
                csvPrinter.printRecord(headerRows);
                for (List<String> rowValue : listingActivityRows) {
                    csvPrinter.printRecord(rowValue);
                }
                for (List<String> rowValue : criteriaActivityRows) {
                    csvPrinter.printRecord(rowValue);
                }
                for (List<String> rowValue : developerActivityRows) {
                    csvPrinter.printRecord(rowValue);
                }
                for (List<String> rowValue : productActivityRows) {
                    csvPrinter.printRecord(rowValue);
                }
                for (List<String> rowValue : versionActivityRows) {
                    csvPrinter.printRecord(rowValue);
                }
            } catch (final IOException ex) {
                LOGGER.error("Could not write file " + reportFile.getName(), ex);
            } finally {
                try {
                    writer.flush();
                    writer.close();
                    csvPrinter.flush();
                    csvPrinter.close();
                } catch (Exception ignore) {
                }
            }
            filesToEmail.add(reportFile);
            emailBody = "<p>A summary of questionable activity found between " + 
                    Util.getDateFormatter().format(startDate) + " and " + 
                    Util.getDateFormatter().format(endDate) + " is attached.</p>";
        }
        
        //look up subscribers and email them
        Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
        permissions.add(new GrantedPermission("ROLE_ADMIN"));
        List<RecipientWithSubscriptionsDTO> recipients = notificationDao
                .getAllNotificationMappingsForType(permissions, NotificationTypeConcept.QUESTIONABLE_ACTIVITY, null);
        if (recipients != null && recipients.size() > 0) {
            String[] emailAddrs = new String[recipients.size()];
            for (int i = 0; i < recipients.size(); i++) {
                RecipientWithSubscriptionsDTO recip = recipients.get(i);
                emailAddrs[i] = recip.getEmail();
                LOGGER.info("Sending email to " + recip.getEmail());
            }
            SendMailUtil mailUtil = new SendMailUtil();
            mailUtil.sendEmail(null, emailAddrs, 
                    this.getProperties().getProperty("questionableActivityEmailSubject").toString(), 
                    emailBody, filesToEmail, this.getProperties());
        }
    }
    
    private List<List<String>> createCriteriaActivityRows() throws IOException {
        LOGGER.debug("Getting certification result activity between " + startDate + " and " + endDate);
        List<QuestionableActivityCertificationResultDTO> certResultActivities = 
                qaDao.findCertificationResultActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + certResultActivities.size() + " questionable certification result activities");
        
        //create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityCertificationResultDTO>> activityByGroup = 
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityCertificationResultDTO>>();
        for(QuestionableActivityCertificationResultDTO activity : certResultActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(
                    activity.getActivityDate(), activity.getTrigger());
            
            if(activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityCertificationResultDTO> activitiesForGroup = 
                        new ArrayList<QuestionableActivityCertificationResultDTO>();
                activitiesForGroup.add(activity);
                activityByGroup.put(groupKey, activitiesForGroup);
            } else {
                List<QuestionableActivityCertificationResultDTO> existingActivitiesForGroup = 
                        activityByGroup.get(groupKey);
                existingActivitiesForGroup.add(activity);
            }
        }
        
        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for(ActivityDateTriggerGroup activityGroup : activityGroups) {
           List<String> currRow = createEmptyRow();
           currRow.set(ACTIVITY_DATE_COL, Util.timestampFormatter.format(activityGroup.getActivityDate()));
           currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

           List<QuestionableActivityCertificationResultDTO> activitiesForGroup = 
                   activityByGroup.get(activityGroup);
           for(QuestionableActivityCertificationResultDTO activity : activitiesForGroup) {
               putCertResultActivityInRow(activity, currRow);
           }
           
           activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }
    
    private List<List<String>> createListingActivityRows() throws IOException {
        LOGGER.debug("Getting listing activity between " + startDate + " and " + endDate);
        List<QuestionableActivityListingDTO> listingActivities = 
                qaDao.findListingActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + listingActivities.size() + " questionable listing activities");

        //create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityListingDTO>> activityByGroup = 
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityListingDTO>>();
        for(QuestionableActivityListingDTO activity : listingActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(
                    activity.getActivityDate(), activity.getTrigger());
            
            if(activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityListingDTO> activitiesForDate = 
                        new ArrayList<QuestionableActivityListingDTO>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityListingDTO> existingActivitiesForDate = 
                        activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }
        
        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for(ActivityDateTriggerGroup activityGroup : activityGroups) {
           List<String> currRow = createEmptyRow();
           currRow.set(ACTIVITY_DATE_COL, Util.timestampFormatter.format(activityGroup.getActivityDate()));
           currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

           List<QuestionableActivityListingDTO> activitiesForGroup = 
                   activityByGroup.get(activityGroup);
           for(QuestionableActivityListingDTO activity : activitiesForGroup) {
               putListingActivityInRow(activity, currRow);
           }
           
           activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }
    
    private List<List<String>> createDeveloperActivityRows() {
        LOGGER.debug("Getting developer activity between " + startDate + " and " + endDate);
        List<QuestionableActivityDeveloperDTO> developerActivities =
                qaDao.findDeveloperActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + developerActivities.size() + " questionable developer activities");

        //create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityDeveloperDTO>> activityByGroup = 
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityDeveloperDTO>>();
        for(QuestionableActivityDeveloperDTO activity : developerActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(
                    activity.getActivityDate(), activity.getTrigger());
            
            if(activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityDeveloperDTO> activitiesForDate = 
                        new ArrayList<QuestionableActivityDeveloperDTO>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityDeveloperDTO> existingActivitiesForDate = 
                        activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }
        
        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for(ActivityDateTriggerGroup activityGroup : activityGroups) {
           List<String> currRow = createEmptyRow();
           currRow.set(ACTIVITY_DATE_COL, Util.timestampFormatter.format(activityGroup.getActivityDate()));
           currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

           List<QuestionableActivityDeveloperDTO> activitiesForGroup = 
                   activityByGroup.get(activityGroup);
           for(QuestionableActivityDeveloperDTO activity : activitiesForGroup) {
               putDeveloperActivityInRow(activity, currRow);
           }
           
           activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }
    
    private List<List<String>> createProductActivityRows() {
        LOGGER.debug("Getting product activity between " + startDate + " and " + endDate);
        List<QuestionableActivityProductDTO> productActivities = 
                qaDao.findProductActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + productActivities.size() + " questionable developer activities");

        //create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityProductDTO>> activityByGroup = 
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityProductDTO>>();
        for(QuestionableActivityProductDTO activity : productActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(
                    activity.getActivityDate(), activity.getTrigger());
            
            if(activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityProductDTO> activitiesForDate = 
                        new ArrayList<QuestionableActivityProductDTO>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityProductDTO> existingActivitiesForDate = 
                        activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }
        
        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for(ActivityDateTriggerGroup activityGroup : activityGroups) {
           List<String> currRow = createEmptyRow();
           currRow.set(ACTIVITY_DATE_COL, Util.timestampFormatter.format(activityGroup.getActivityDate()));
           currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

           List<QuestionableActivityProductDTO> activitiesForGroup = 
                   activityByGroup.get(activityGroup);
           for(QuestionableActivityProductDTO activity : activitiesForGroup) {
               putProductActivityInRow(activity, currRow);
           }
           
           activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }
    
    private List<List<String>> createVersionActivityRows() {
        LOGGER.debug("Getting version activity between " + startDate + " and " + endDate);
        List<QuestionableActivityVersionDTO> versionActivities = 
                qaDao.findVersionActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Found " + versionActivities.size() + " questionable developer activities");

        //create a bucket for each activity timestamp+trigger type
        Map<ActivityDateTriggerGroup, List<QuestionableActivityVersionDTO>> activityByGroup = 
                new HashMap<ActivityDateTriggerGroup, List<QuestionableActivityVersionDTO>>();
        for(QuestionableActivityVersionDTO activity : versionActivities) {
            ActivityDateTriggerGroup groupKey = new ActivityDateTriggerGroup(
                    activity.getActivityDate(), activity.getTrigger());
            
            if(activityByGroup.get(groupKey) == null) {
                List<QuestionableActivityVersionDTO> activitiesForDate = 
                        new ArrayList<QuestionableActivityVersionDTO>();
                activitiesForDate.add(activity);
                activityByGroup.put(groupKey, activitiesForDate);
            } else {
                List<QuestionableActivityVersionDTO> existingActivitiesForDate = 
                        activityByGroup.get(groupKey);
                existingActivitiesForDate.add(activity);
            }
        }
        
        List<List<String>> activityCsvRows = new ArrayList<List<String>>();
        Set<ActivityDateTriggerGroup> activityGroups = activityByGroup.keySet();
        for(ActivityDateTriggerGroup activityGroup : activityGroups) {
           List<String> currRow = createEmptyRow();
           currRow.set(ACTIVITY_DATE_COL, Util.timestampFormatter.format(activityGroup.getActivityDate()));
           currRow.set(ACTIVITY_TYPE_COL, activityGroup.getTrigger().getName());

           List<QuestionableActivityVersionDTO> activitiesForGroup = 
                   activityByGroup.get(activityGroup);
           for(QuestionableActivityVersionDTO activity : activitiesForGroup) {
               putVersionActivityInRow(activity, currRow);
           }
           
           activityCsvRows.add(currRow);
        }
        return activityCsvRows;
    }
    
    private void putListingActivityInRow(QuestionableActivityListingDTO activity,
        List<String> currRow) throws IOException {
        //fill in info about the listing that will be the same for every
        //activity found in this date bucket
        currRow.set(ACB_COL, activity.getListing().getCertificationBodyName());
        currRow.set(DEVELOPER_COL, activity.getListing().getDeveloper().getName());
        currRow.set(PRODUCT_COL, activity.getListing().getProduct().getName());
        currRow.set(VERSION_COL, activity.getListing().getVersion().getVersion());
        currRow.set(LISTING_COL, activity.getListing().getChplProductNumber());
        currRow.set(STATUS_COL, activity.getListing().getCertificationStatusName());
        currRow.set(LINK_COL, this.getProperties().getProperty("chplUrlBegin") + "/#/admin/reports/" + activity.getListing().getId());
        currRow.set(ACTIVITY_USER_COL, activity.getUser().getSubjectName());

        if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CRITERIA_ADDED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            } 
            currActivityRowValue += activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CRITERIA_REMOVED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            } 
            currActivityRowValue += activity.getBefore();   
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CQM_ADDED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            } 
            currActivityRowValue += activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CQM_REMOVED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            } 
            currActivityRowValue += activity.getBefore();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.SURVEILLANCE_REMOVED.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "TRUE");
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.EDITION_2011_EDITED.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL, "TRUE");
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED.getName())) {
            currRow.set(ACTIVITY_DESCRIPTION_COL,
                    "From " + activity.getBefore() + " to " + activity.getAfter());
            currRow.set(ACTIVITY_CERT_STATUS_CHANGE_REASON_COL, activity.getCertificationStatusChangeReason());
        }
        currRow.set(ACTIVITY_REASON_COL, activity.getReason());
    }
    
    private void putCertResultActivityInRow(QuestionableActivityCertificationResultDTO activity,
        List<String> currRow) throws IOException {
        //fill in info about the listing that will be the same for every
        //activity found in this date bucket
        currRow.set(ACB_COL, activity.getListing().getCertificationBodyName());
        currRow.set(DEVELOPER_COL, activity.getListing().getDeveloper().getName());
        currRow.set(PRODUCT_COL, activity.getListing().getProduct().getName());
        currRow.set(VERSION_COL, activity.getListing().getVersion().getVersion());
        currRow.set(LISTING_COL, activity.getListing().getChplProductNumber());
        currRow.set(STATUS_COL, activity.getListing().getCertificationStatusName());
        currRow.set(LINK_COL, this.getProperties().getProperty("chplUrlBegin") + "/#/admin/reports/" + activity.getListing().getId());
        currRow.set(ACTIVITY_USER_COL, activity.getUser().getSubjectName());
        
        if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": from " + 
                        activity.getBefore() + " to " + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.G1_MEASURE_ADDED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getBefore();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": from " + 
                        activity.getBefore() + " to " + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.G2_MEASURE_ADDED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getBefore();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.GAP_EDITED.getName())) {
            String currActivityRowValue = currRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += activity.getCertResult().getNumber() + " from " + 
                        activity.getBefore() + " to " + activity.getAfter();
            currRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        }
        currRow.set(ACTIVITY_REASON_COL, activity.getReason());
    }
    
    private void putDeveloperActivityInRow(QuestionableActivityDeveloperDTO developerActivity,
            List<String> activityRow) {
        activityRow.set(DEVELOPER_COL, developerActivity.getDeveloper().getName());
        activityRow.set(ACTIVITY_USER_COL, developerActivity.getUser().getSubjectName()); 
        
        if(developerActivity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED.getName())) {
            activityRow.set(ACTIVITY_DESCRIPTION_COL,
                    "From " + developerActivity.getBefore() + " to " + developerActivity.getAfter());
        } else if(developerActivity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += "From " + developerActivity.getBefore() + " to " + developerActivity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(developerActivity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += "Added status " + developerActivity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(developerActivity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += "Removed status " + developerActivity.getBefore();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(developerActivity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += "Changed status from " + developerActivity.getBefore() + 
                    " to " + developerActivity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        }
    }
    
    private void putProductActivityInRow(QuestionableActivityProductDTO activity,
            List<String> activityRow) {
        activityRow.set(DEVELOPER_COL, activity.getProduct().getDeveloperName());
        activityRow.set(PRODUCT_COL, activity.getProduct().getName());
        activityRow.set(ACTIVITY_USER_COL, activity.getUser().getSubjectName()); 
        
        if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED.getName())) {
            activityRow.set(ACTIVITY_DESCRIPTION_COL,
                    "From " + activity.getBefore() + " to " + activity.getAfter());
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += "From " + activity.getBefore() + " to " + activity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += "Added owner " + activity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += "Removed owner " + activity.getBefore();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
            String currActivityRowValue = activityRow.get(ACTIVITY_DESCRIPTION_COL);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            }
            currActivityRowValue += "Changed owner from " + activity.getBefore() + " to " + activity.getAfter();
            activityRow.set(ACTIVITY_DESCRIPTION_COL, currActivityRowValue);
        }
    }
    
    private void putVersionActivityInRow(QuestionableActivityVersionDTO activity,
            List<String> activityRow) {
        activityRow.set(DEVELOPER_COL, activity.getVersion().getDeveloperName());
        activityRow.set(PRODUCT_COL, activity.getVersion().getProductName());
        activityRow.set(VERSION_COL, activity.getVersion().getVersion());
        activityRow.set(ACTIVITY_USER_COL, activity.getUser().getSubjectName()); 
        
        if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.VERSION_NAME_EDITED.getName())) {
            activityRow.set(ACTIVITY_DESCRIPTION_COL,
                    "From " + activity.getBefore() + " to " + activity.getAfter());
        }
    }
    
    private List<String> createHeader() {
        List<String> row = new ArrayList<String>();
        row.add("ACB");
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
    
    private List<String> createEmptyRow() {
        List<String> row = new ArrayList<String>(NUM_REPORT_COLS);
        for(int i = 0; i < NUM_REPORT_COLS; i++) {
            row.add("");
        }
        return row;
    }
    
    public QuestionableActivityDAO getQaDao() {
        return qaDao;
    }

    public void setQaDao(QuestionableActivityDAO qaDao) {
        this.qaDao = qaDao;
    }
    
    public NotificationDAO getNotificationDao() {
        return notificationDao;
    }

    public void setNotificationDao(NotificationDAO notificationDao) {
        this.notificationDao = notificationDao;
    }

    private class ActivityDateTriggerGroup {
        private Date activityDate;
        private QuestionableActivityTriggerDTO trigger;
        
        public ActivityDateTriggerGroup(Date activityDate, QuestionableActivityTriggerDTO trigger) {
            this.activityDate = activityDate;
            this.trigger = trigger;
        }
        
        @Override
        public boolean equals(Object anotherObject) {
            if(anotherObject == null) {
                return false;
            }
            if(!(anotherObject instanceof ActivityDateTriggerGroup)) {
                return false;
            }
            ActivityDateTriggerGroup anotherGroup = (ActivityDateTriggerGroup) anotherObject;
            if(this.activityDate == null || anotherGroup.activityDate == null || 
                    this.trigger == null || anotherGroup.trigger == null) {
                return false;
            } 
            if(this.activityDate.getTime() == anotherGroup.activityDate.getTime() && 
                    (this.trigger.getId().longValue() == anotherGroup.trigger.getId().longValue() ||
                    this.trigger.getName().equals(anotherGroup.trigger.getName()))) {
                return true;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            if(this.activityDate == null || this.trigger == null) {
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
    
    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 2) {
            LOGGER.error("QuestionableActivityReportApp HELP: \n" 
                    + "QuestionableActivityReportApp 2017-10-01 2017-10-31\n"
                    + "QuestionableActivityReportApp expects two arguments "
                    + "that are the start and end dates for which to report questionable activity.");
            return;
        }

        String startDateStr = args[0].trim();
        String endDateStr = args[1].trim();
        SimpleDateFormat startEndDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        Date endDate = null;
        try {
           startDate = startEndDateFormat.parse(startDateStr);
           //defaults to 00:00:00 HMS
        } catch(ParseException ex) {
            LOGGER.error("Could not parse " + startDateStr + " as a date. Please make sure the " 
                    + " start date is in the format yyyy-MM-dd.");
            return;
        }
        
        try {
            endDate = startEndDateFormat.parse(endDateStr);
            //got date from args, set time to end of day
            Calendar endDateCal = new GregorianCalendar();
            endDateCal.setTime(endDate);
            endDateCal.set(Calendar.HOUR, 23);
            endDateCal.set(Calendar.MINUTE, 59);
            endDateCal.set(Calendar.SECOND, 59);
            endDateCal.set(Calendar.MILLISECOND, 999);
            endDate = endDateCal.getTime();
        } catch(ParseException ex) {
            LOGGER.error("Could not parse " + endDateStr + " as a date. Please make sure the " 
                    + " end date is in the format yyyy-MM-dd.");
            return;
        }
        
        LOGGER.info("Generating questionable activity report between " + startDate + " and " + endDate);
        
        QuestionableActivityReportApp app = new QuestionableActivityReportApp(startDate, endDate);
        app.setLocalContext();
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        app.initiateSpringBeans(context);
        app.runJob();
        context.close();
    }
}
