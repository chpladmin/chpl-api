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
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;

public class QuestionableActivityReportApp extends App {
    private static final Logger LOGGER = LogManager.getLogger(QuestionableActivityReportApp.class);
    private static final int NUM_REPORT_COLS = 28;
    private Map<QuestionableActivityTriggerConcept, Integer> triggerColumnIndexMap;
    private Date startDate, endDate;
    
    protected QuestionableActivityDAO qaDao;
    protected NotificationDAO notificationDao;

    public QuestionableActivityReportApp() {
        triggerColumnIndexMap = new HashMap<QuestionableActivityTriggerConcept, Integer>();
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.CRITERIA_ADDED, 9);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.CRITERIA_REMOVED, 10);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.CQM_ADDED, 11);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.CQM_REMOVED, 12);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED, 13);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.G1_MEASURE_ADDED, 14);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED, 15);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED, 16);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.G2_MEASURE_ADDED, 17);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED, 18);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.GAP_EDITED, 19);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.SURVEILLANCE_REMOVED, 20);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.EDITION_2011_EDITED, 21);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED, 22);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED, 23);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED, 24);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED, 25);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED, 26);
        triggerColumnIndexMap.put(QuestionableActivityTriggerConcept.VERSION_NAME_EDITED, 27);
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
        List<List<String>> developerActivityRows = createDeveloperActivityRows();
        List<List<String>> productActivityRows = createProductActivityRows();
        List<List<String>> versionActivityRows = createVersionActivityRows();
        
        List<File> filesToEmail = new ArrayList<File>();
        String emailBody = "";
        
        //write out listing, developer, product, and version activities
        if((listingActivityRows == null || listingActivityRows.size() == 0) && 
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
            int numQuestionableActivities = listingActivityRows.size() + developerActivityRows.size() + 
                    productActivityRows.size() + versionActivityRows.size();
            emailBody = "<p>" + numQuestionableActivities + " questionable activities were " + 
                    "found between " + 
                    Util.getDateFormatter().format(startDate) + " and " + 
                    Util.getDateFormatter().format(endDate) + ".</p>";
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
    
    private List<List<String>> createListingActivityRows() throws IOException {
        LOGGER.debug("Getting listing activity between " + startDate + " and " + endDate);
        List<QuestionableActivityListingDTO> listingActivities = 
                qaDao.findListingActivityBetweenDates(startDate, endDate);
        LOGGER.debug("Getting certification result activity between " + startDate + " and " + endDate);
        List<QuestionableActivityCertificationResultDTO> certResultActivities = 
                qaDao.findCertificationResultActivityBetweenDates(startDate, endDate);
        
        //create csv rows for listing and criteria activities (they will go on the same row)
        //create a bucket for each activity timestamp
        Map<Date, List<QuestionableActivityDTO>> listingActivityByDate = 
                new HashMap<Date, List<QuestionableActivityDTO>>();
        for(QuestionableActivityDTO listingActivity : listingActivities) {
            if(listingActivityByDate.get(listingActivity.getActivityDate()) == null) {
                List<QuestionableActivityDTO> activitiesForDate = 
                        new ArrayList<QuestionableActivityDTO>();
                activitiesForDate.add(listingActivity);
                listingActivityByDate.put(listingActivity.getActivityDate(), activitiesForDate);
            } else {
                List<QuestionableActivityDTO> existingActivitiesForDate = 
                        listingActivityByDate.get(listingActivity.getActivityDate());
                existingActivitiesForDate.add(listingActivity);
            }
        }
        //add cert result activities into the activity date buckets
        for(QuestionableActivityDTO certResultActivity : certResultActivities) {
            if(listingActivityByDate.get(certResultActivity.getActivityDate()) == null) {
                List<QuestionableActivityDTO> activitiesForDate = 
                        new ArrayList<QuestionableActivityDTO>();
                activitiesForDate.add(certResultActivity);
                listingActivityByDate.put(certResultActivity.getActivityDate(), activitiesForDate);
            } else {
                List<QuestionableActivityDTO> existingActivitiesForDate = 
                        listingActivityByDate.get(certResultActivity.getActivityDate());
                existingActivitiesForDate.add(certResultActivity);
            }
        }
        LOGGER.debug("Found " + listingActivityByDate.keySet().size() + " dates with questionable listing/cert result activity");
        
        //For each activity date, multiple activities could have occurred.
        //For example, a user could change multiple GAP criteria fields, add a criteria, 
        //and change the certification status of a listing all in one action but that
        //should appear as one line in our questionable activity file.
        //Create a list of strings that will eventually represent a row in the 
        //questionable activity file for this particular activity
        List<List<String>> listingActivityRows = new ArrayList<List<String>>();
        Set<Date> activityDates = listingActivityByDate.keySet();
        for(Date activityDate : activityDates) {
           List<String> currRow = createEmptyRow();
           currRow.set(7, Util.timestampFormatter.format(activityDate));

           List<QuestionableActivityDTO> listingActivityForDate = 
                   listingActivityByDate.get(activityDate);
           for(QuestionableActivityDTO activity : listingActivityForDate) {
               if(activity instanceof QuestionableActivityListingDTO) {
                   putListingActivityInRow((QuestionableActivityListingDTO)activity, currRow);
               } else if(activity instanceof QuestionableActivityCertificationResultDTO) {
                   putCertResultActivityInRow((QuestionableActivityCertificationResultDTO)activity, currRow);
               }
           }
           
           listingActivityRows.add(currRow);
        }
        return listingActivityRows;
    }
    
    private List<List<String>> createDeveloperActivityRows() {
        LOGGER.debug("Getting developer activity between " + startDate + " and " + endDate);
        List<QuestionableActivityDeveloperDTO> developerActivities =
                qaDao.findDeveloperActivityBetweenDates(startDate, endDate);
        //create csv rows for developer activities (they will go on the same row)
        //create a bucket for each activity timestamp
        Map<Date, List<QuestionableActivityDeveloperDTO>> developerActivityByDate = 
                new HashMap<Date, List<QuestionableActivityDeveloperDTO>>();
        for(QuestionableActivityDeveloperDTO developerActivity : developerActivities) {
            if(developerActivityByDate.get(developerActivity.getActivityDate()) == null) {
                List<QuestionableActivityDeveloperDTO> activitiesForDate = 
                        new ArrayList<QuestionableActivityDeveloperDTO>();
                activitiesForDate.add(developerActivity);
                developerActivityByDate.put(developerActivity.getActivityDate(), activitiesForDate);
            } else {
                List<QuestionableActivityDeveloperDTO> existingActivitiesForDate = 
                        developerActivityByDate.get(developerActivity.getActivityDate());
                existingActivitiesForDate.add(developerActivity);
            }
        }        
        LOGGER.debug("Found " + developerActivityByDate.keySet().size() + " dates with questionable developer activity");

        List<List<String>> developerActivityRows = new ArrayList<List<String>>();
        Set<Date> developerActivityDates = developerActivityByDate.keySet();
        for(Date activityDate : developerActivityDates) {
           List<String> currRow = createEmptyRow();
           currRow.set(7, Util.timestampFormatter.format(activityDate));

           List<QuestionableActivityDeveloperDTO> developerActivityForDate = 
                   developerActivityByDate.get(activityDate);
           putDeveloperActivitiesInRow(developerActivityForDate, currRow);
           developerActivityRows.add(currRow);
        }
        
        return developerActivityRows;
    }
    
    private List<List<String>> createProductActivityRows() {
        LOGGER.debug("Getting product activity between " + startDate + " and " + endDate);

        List<QuestionableActivityProductDTO> productActivities = 
                qaDao.findProductActivityBetweenDates(startDate, endDate);
        //create csv rows for product activities (they will go on the same row)
        //create a bucket for each activity timestamp
        Map<Date, List<QuestionableActivityProductDTO>> productActivityByDate = 
                new HashMap<Date, List<QuestionableActivityProductDTO>>();
        for(QuestionableActivityProductDTO productActivity : productActivities) {
            if(productActivityByDate.get(productActivity.getActivityDate()) == null) {
                List<QuestionableActivityProductDTO> activitiesForDate = 
                        new ArrayList<QuestionableActivityProductDTO>();
                activitiesForDate.add(productActivity);
                productActivityByDate.put(productActivity.getActivityDate(), activitiesForDate);
            } else {
                List<QuestionableActivityProductDTO> existingActivitiesForDate = 
                        productActivityByDate.get(productActivity.getActivityDate());
                existingActivitiesForDate.add(productActivity);
            }
        }
        LOGGER.debug("Found " + productActivityByDate.keySet().size() + " dates with questionable product activity");
        
        List<List<String>> productActivityRows = new ArrayList<List<String>>();
        Set<Date> productActivityDates = productActivityByDate.keySet();
        for(Date activityDate : productActivityDates) {
           List<String> currRow = createEmptyRow();
           currRow.set(7, Util.timestampFormatter.format(activityDate));

           List<QuestionableActivityProductDTO> productActivityForDate = 
                   productActivityByDate.get(activityDate);
           putProductActivitiesInRow(productActivityForDate, currRow);
           productActivityRows.add(currRow);
        }
        return productActivityRows;
    }
    
    private List<List<String>> createVersionActivityRows() {
        LOGGER.debug("Getting version activity between " + startDate + " and " + endDate);
        List<QuestionableActivityVersionDTO> versionActivities = 
                qaDao.findVersionActivityBetweenDates(startDate, endDate);
        //create csv rows for version activities (they will go on the same row)
        //create a bucket for each activity timestamp
        Map<Date, List<QuestionableActivityVersionDTO>> versionActivityByDate = 
                new HashMap<Date, List<QuestionableActivityVersionDTO>>();
        for(QuestionableActivityVersionDTO versionActivity : versionActivities) {
            if(versionActivityByDate.get(versionActivity.getActivityDate()) == null) {
                List<QuestionableActivityVersionDTO> activitiesForDate = 
                        new ArrayList<QuestionableActivityVersionDTO>();
                activitiesForDate.add(versionActivity);
                versionActivityByDate.put(versionActivity.getActivityDate(), activitiesForDate);
            } else {
                List<QuestionableActivityVersionDTO> existingActivitiesForDate = 
                        versionActivityByDate.get(versionActivity.getActivityDate());
                existingActivitiesForDate.add(versionActivity);
            }
        }
        LOGGER.debug("Found " + versionActivityByDate.keySet().size() + " dates with questionable version activity");

        List<List<String>> versionActivityRows = new ArrayList<List<String>>();
        Set<Date> versionActivityDates = versionActivityByDate.keySet();
        for(Date activityDate : versionActivityDates) {
           List<String> currRow = createEmptyRow();
           currRow.set(7, Util.timestampFormatter.format(activityDate));

           List<QuestionableActivityVersionDTO> versionActivityForDate = 
                   versionActivityByDate.get(activityDate);
           putVersionActivitiesInRow(versionActivityForDate, currRow);
           versionActivityRows.add(currRow);
        }
        return versionActivityRows;
    }
    
    private void putListingActivityInRow(QuestionableActivityListingDTO activity,
        List<String> currRow) throws IOException {
        //fill in info about the listing that will be the same for every
        //activity found in this date bucket
        currRow.set(0, activity.getListing().getCertificationBodyName());
        currRow.set(1, activity.getListing().getDeveloper().getName());
        currRow.set(2, activity.getListing().getProduct().getName());
        currRow.set(3, activity.getListing().getVersion().getVersion());
        currRow.set(4, activity.getListing().getChplProductNumber());
        currRow.set(5, activity.getListing().getCertificationStatusName());
        currRow.set(6, this.getProperties().getProperty("chplUrlBegin") + "/#/admin/reports/" + activity.getListing().getId());
        currRow.set(8, activity.getUser().getSubjectName());
        
        if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CRITERIA_ADDED.getName())) {
            int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CRITERIA_ADDED);
            String currActivityRowValue = currRow.get(index);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            } 
            currActivityRowValue += activity.getAfter();
            currRow.set(index, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CRITERIA_REMOVED.getName())) {
            int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CRITERIA_REMOVED);
            String currActivityRowValue = currRow.get(index);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            } 
            currActivityRowValue += activity.getBefore();   
            currRow.set(index, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CQM_ADDED.getName())) {
            int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CQM_ADDED);
            String currActivityRowValue = currRow.get(index);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            } 
            currActivityRowValue += activity.getAfter();
            currRow.set(index, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CQM_REMOVED.getName())) {
            int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CQM_REMOVED);
            String currActivityRowValue = currRow.get(index);
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += "; "; 
            } 
            currActivityRowValue += activity.getBefore();
            currRow.set(index, currActivityRowValue);
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.SURVEILLANCE_REMOVED.getName())) {
            currRow.set(
                    triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.SURVEILLANCE_REMOVED),
                    "TRUE");
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.EDITION_2011_EDITED.getName())) {
            currRow.set(
                    triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.EDITION_2011_EDITED),
                    "TRUE");
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED.getName())) {
            currRow.set(
                    triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED),
                    "From " + activity.getBefore() + " to " + activity.getAfter());
        }
    }
    
    private void putCertResultActivityInRow(QuestionableActivityCertificationResultDTO activity,
            List<String> currRow) throws IOException {
            //fill in info about the listing that will be the same for every
            //activity found in this date bucket
            currRow.set(0, activity.getListing().getCertificationBodyName());
            currRow.set(1, activity.getListing().getDeveloper().getName());
            currRow.set(2, activity.getListing().getProduct().getName());
            currRow.set(3, activity.getListing().getVersion().getVersion());
            currRow.set(4, activity.getListing().getChplProductNumber());
            currRow.set(5, activity.getListing().getCertificationStatusName());
            currRow.set(6, this.getProperties().getProperty("chplUrlBegin") + "/#/admin/reports/" + activity.getListing().getId());
            currRow.set(8, activity.getUser().getSubjectName());
            
            if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED);
                String currActivityRowValue = currRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += activity.getCertResult().getNumber() + ": from " + 
                            activity.getBefore() + " to " + activity.getAfter();
                currRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G1_MEASURE_ADDED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G1_MEASURE_ADDED);
                String currActivityRowValue = currRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getAfter();
                currRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED);
                String currActivityRowValue = currRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getBefore();
                currRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED);
                String currActivityRowValue = currRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += activity.getCertResult().getNumber() + ": from " + 
                            activity.getBefore() + " to " + activity.getAfter();
                currRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G2_MEASURE_ADDED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G2_MEASURE_ADDED);
                String currActivityRowValue = currRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getAfter();
                currRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED);
                String currActivityRowValue = currRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += activity.getCertResult().getNumber() + ": " + activity.getBefore();
                currRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.GAP_EDITED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.GAP_EDITED);
                String currActivityRowValue = currRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += activity.getCertResult().getNumber() + " from " + 
                            activity.getBefore() + " to " + activity.getAfter();
                currRow.set(index, currActivityRowValue);
            }
        }
    
    private void putDeveloperActivitiesInRow(List<QuestionableActivityDeveloperDTO> developerActivityForDate,
            List<String> activityRow) {
        for(QuestionableActivityDeveloperDTO activity : developerActivityForDate) {
            activityRow.set(1, activity.getDeveloper().getName());
            activityRow.set(8, activity.getUser().getSubjectName()); 
            
            if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED.getName())) {
                activityRow.set(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED),
                        "From " + activity.getBefore() + " to " + activity.getAfter());
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED);
                String currActivityRowValue = activityRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += "From " + activity.getBefore() + " to " + activity.getAfter();
                activityRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED);
                String currActivityRowValue = activityRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += "Added status " + activity.getAfter();
                activityRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED);
                String currActivityRowValue = activityRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += "Removed status " + activity.getBefore();
                activityRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED);
                String currActivityRowValue = activityRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += "Changed status from " + activity.getBefore() + " to " + activity.getAfter();
                activityRow.set(index, currActivityRowValue);
            }
        }
    }
    
    private void putProductActivitiesInRow(List<QuestionableActivityProductDTO> productActivityForDate,
            List<String> activityRow) {
        for(QuestionableActivityProductDTO activity : productActivityForDate) {
            activityRow.set(1, activity.getProduct().getDeveloperName());
            activityRow.set(2, activity.getProduct().getName());
            activityRow.set(8, activity.getUser().getSubjectName()); 
            
            if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED.getName())) {
                activityRow.set(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED),
                        "From " + activity.getBefore() + " to " + activity.getAfter());
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED);
                String currActivityRowValue = activityRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += "From " + activity.getBefore() + " to " + activity.getAfter();
                activityRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED);
                String currActivityRowValue = activityRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += "Added owner " + activity.getAfter();
                activityRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED);
                String currActivityRowValue = activityRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += "Removed owner " + activity.getBefore();
                activityRow.set(index, currActivityRowValue);
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
                int index = triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED);
                String currActivityRowValue = activityRow.get(index);
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; "; 
                }
                currActivityRowValue += "Changed owner from " + activity.getBefore() + " to " + activity.getAfter();
                activityRow.set(index, currActivityRowValue);
            }
        }
    }
    
    private void putVersionActivitiesInRow(List<QuestionableActivityVersionDTO> versionActivityForDate,
            List<String> activityRow) {
        for(QuestionableActivityVersionDTO activity : versionActivityForDate) {
            activityRow.set(1, activity.getVersion().getDeveloperName());
            activityRow.set(2, activity.getVersion().getProductName());
            activityRow.set(3, activity.getVersion().getVersion());
            activityRow.set(8, activity.getUser().getSubjectName()); 
            
            if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.VERSION_NAME_EDITED.getName())) {
                activityRow.set(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.VERSION_NAME_EDITED),
                        "From " + activity.getBefore() + " to " + activity.getAfter());
            }
        }
    }
    
    private List<String> createHeader() {
        List<String> rows = new ArrayList<String>();
        rows.add("ACB");
        rows.add("Developer");
        rows.add("Product");
        rows.add("Version");
        rows.add("CHPL Product Number");
        rows.add("Current Certification Status");
        rows.add("Link");
        rows.add("Activity Timestamp");
        rows.add("Responsible User");
        rows.add("Added Certification Criteria");
        rows.add("Removed Certification Criteria");
        rows.add("Added CQMs");
        rows.add("Removed CQMs");
        rows.add("Edited Measure Successfully Tested for G1");
        rows.add("Added Measures Successfully Tested for G1");
        rows.add("Removed Measures Successfully Tested for G1");
        rows.add("Edited Measure Successfully Tested for G2");
        rows.add("Added Measures Successfully Tested for G2");
        rows.add("Removed Measures Successfully Tested for G2");
        rows.add("Edit GAP Status");
        rows.add("Deleted Surveillance");
        rows.add("Edited 2011 Edition");
        rows.add("Change Certification Status");
        rows.add("Change Developer Name");
        rows.add("Change Developer Status Including History");
        rows.add("Change Product Name");
        rows.add("Change Product Owner Including History");
        rows.add("Change Version Name");
        return rows;
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
