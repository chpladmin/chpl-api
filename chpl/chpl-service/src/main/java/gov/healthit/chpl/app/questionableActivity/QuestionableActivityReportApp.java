package gov.healthit.chpl.app.questionableActivity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.app.App;
import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;

public class QuestionableActivityReportApp extends App {
    private static final Logger LOGGER = LogManager.getLogger(QuestionableActivityReportApp.class);
    private Map<QuestionableActivityTriggerConcept, Integer> triggerColumnIndexMap;
    private Date startDate, endDate;
    
    protected SimpleDateFormat timestampFormat;
    protected QuestionableActivityDAO qaDao;

    public QuestionableActivityReportApp() {
        timestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
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
    }
    
    protected void runJob() {
        List<List<String>> listingActivityRows = createListingActivityRows();
        List<List<String>> developerActivityRows = createDeveloperActivityRows();
        List<List<String>> productActivityRows = createProductActivityRows();
        List<List<String>> versionActivityRows = createVersionActivityRows();
        
        //TODO: write out listing, developer, product, and version activities
    }
    
    private List<List<String>> createListingActivityRows() {
        List<QuestionableActivityListingDTO> listingActivities = 
                qaDao.findListingActivityBetweenDates(startDate, endDate);
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
        
        //For each activity date, multiple activities could have occurred.
        //For example, a user could change multiple GAP criteria fields, add a criteria, 
        //and change the certification status of a listing all in one action but that
        //should appear as one line in our questionable activity file.
        //Create a list of strings that will eventually represent a row in the 
        //questionable activity file for this particular activity
        List<List<String>> listingActivityRows = new ArrayList<List<String>>();
        Set<Date> activityDates = listingActivityByDate.keySet();
        for(Date activityDate : activityDates) {
           List<String> currRow = new ArrayList<String>();
           currRow.add(timestampFormat.format(activityDate));

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
        
        List<List<String>> developerActivityRows = new ArrayList<List<String>>();
        Set<Date> developerActivityDates = developerActivityByDate.keySet();
        for(Date activityDate : developerActivityDates) {
           List<String> currRow = new ArrayList<String>();
           currRow.add(timestampFormat.format(activityDate));

           List<QuestionableActivityDeveloperDTO> developerActivityForDate = 
                   developerActivityByDate.get(activityDate);
           putDeveloperActivitiesInRow(developerActivityForDate, currRow);
           developerActivityRows.add(currRow);
        }
        
        return developerActivityRows;
    }
    
    private List<List<String>> createProductActivityRows() {
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
        
        List<List<String>> productActivityRows = new ArrayList<List<String>>();
        Set<Date> productActivityDates = productActivityByDate.keySet();
        for(Date activityDate : productActivityDates) {
           List<String> currRow = new ArrayList<String>();
           currRow.add(timestampFormat.format(activityDate));

           List<QuestionableActivityProductDTO> productActivityForDate = 
                   productActivityByDate.get(activityDate);
           putProductActivitiesInRow(productActivityForDate, currRow);
           productActivityRows.add(currRow);
        }
        return productActivityRows;
    }
    
    private List<List<String>> createVersionActivityRows() {
      //create rows for version activities
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
        
        List<List<String>> versionActivityRows = new ArrayList<List<String>>();
        Set<Date> versionActivityDates = versionActivityByDate.keySet();
        for(Date activityDate : versionActivityDates) {
           List<String> currRow = new ArrayList<String>();
           currRow.add(timestampFormat.format(activityDate));

           List<QuestionableActivityVersionDTO> versionActivityForDate = 
                   versionActivityByDate.get(activityDate);
           putVersionActivitiesInRow(versionActivityForDate, currRow);
           versionActivityRows.add(currRow);
        }
        return versionActivityRows;
    }
    
    private void putListingActivityInRow(QuestionableActivityListingDTO activity,
        List<String> currRow) {
        if(currRow.size() == 0) {
            //fill in info about the listing that will be the same for every
            //activity found in this date bucket
            currRow.add(activity.getListing().getCertificationBodyName());
            currRow.add(activity.getListing().getDeveloper().getName());
            currRow.add(activity.getListing().getProduct().getName());
            currRow.add(activity.getListing().getVersion().getVersion());
            currRow.add(activity.getListing().getChplProductNumber());
            currRow.add(activity.getListing().getCertificationStatusName());
            currRow.add("LINK_TO_LISTING_ACTIVITY_REPORT");
            currRow.add(activity.getUser().getSubjectName());
        } 
        if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CRITERIA_ADDED.getName())) {
            String currActivityRowValue = currRow.get(
                    triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CRITERIA_ADDED));
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += ";" + activity.getAfter();
            } else {
                currActivityRowValue = activity.getAfter();
                currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CRITERIA_ADDED),
                        currActivityRowValue);
            }
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CRITERIA_REMOVED.getName())) {
            String currActivityRowValue = currRow.get(
                    triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CRITERIA_REMOVED));
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += ";" + activity.getBefore();
            } else {
                currActivityRowValue = activity.getBefore();
                currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CRITERIA_REMOVED),
                        currActivityRowValue);
            }
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CQM_ADDED.getName())) {
            String currActivityRowValue = currRow.get(
                    triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CQM_ADDED));
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += ";" + activity.getAfter();
            } else {
                currActivityRowValue = activity.getAfter();
                currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CQM_ADDED),
                        currActivityRowValue);
            }
        } else if(activity.getTrigger().getName().equals(
                QuestionableActivityTriggerConcept.CQM_REMOVED.getName())) {
            String currActivityRowValue = currRow.get(
                    triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CQM_REMOVED));
            if(!StringUtils.isEmpty(currActivityRowValue)) {
                currActivityRowValue += ";" + activity.getBefore();
            } else {
                currActivityRowValue = activity.getBefore();
                currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.CQM_REMOVED),
                        currActivityRowValue);
            }
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
            List<String> currRow) {
            if(currRow.size() == 0) {
                //fill in info about the listing that will be the same for every
                //activity found in this date bucket
                currRow.add(activity.getListing().getCertificationBodyName());
                currRow.add(activity.getListing().getDeveloper().getName());
                currRow.add(activity.getListing().getProduct().getName());
                currRow.add(activity.getListing().getVersion().getVersion());
                currRow.add(activity.getListing().getChplProductNumber());
                currRow.add(activity.getListing().getCertificationStatusName());
                currRow.add("LINK_TO_LISTING_ACTIVITY_REPORT");
                currRow.add(activity.getUser().getSubjectName());
            } 
            if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED.getName())) {
                String currActivityRowValue = currRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += ";" + activity.getCertResult().getNumber() + " from " + 
                            activity.getBefore() + " to " + activity.getAfter();
                } else {
                    currActivityRowValue = activity.getCertResult().getNumber() + " from " + 
                            activity.getBefore() + " to " + activity.getAfter();
                    currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G1_MEASURE_ADDED.getName())) {
                String currActivityRowValue = currRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G1_MEASURE_ADDED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += ";" + activity.getCertResult().getNumber() + ": " + activity.getAfter();
                } else {
                    currActivityRowValue = activity.getCertResult().getNumber() + ": " + activity.getAfter();
                    currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G1_MEASURE_ADDED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED.getName())) {
                String currActivityRowValue = currRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += ";" + activity.getCertResult().getNumber() + ": " + activity.getBefore();
                } else {
                    currActivityRowValue = activity.getCertResult().getNumber() + ": " + activity.getBefore();
                    currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G1_MEASURE_REMOVED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED.getName())) {
                String currActivityRowValue = currRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += ";" + activity.getCertResult().getNumber() + " from " + 
                            activity.getBefore() + " to " + activity.getAfter();
                } else {
                    currActivityRowValue = activity.getCertResult().getNumber() + " from " + 
                            activity.getBefore() + " to " + activity.getAfter();
                    currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G2_MEASURE_ADDED.getName())) {
                String currActivityRowValue = currRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G2_MEASURE_ADDED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += ";" + activity.getCertResult().getNumber() + ": " + activity.getAfter();
                } else {
                    currActivityRowValue = activity.getCertResult().getNumber() + ": " + activity.getAfter();
                    currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G2_MEASURE_ADDED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED.getName())) {
                String currActivityRowValue = currRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += ";" + activity.getCertResult().getNumber() + ": " + activity.getBefore();
                } else {
                    currActivityRowValue = activity.getCertResult().getNumber() + ": " + activity.getBefore();
                    currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.G2_MEASURE_REMOVED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.GAP_EDITED.getName())) {
                String currActivityRowValue = currRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.GAP_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += ";" + activity.getCertResult().getNumber() + " from " + 
                            activity.getBefore() + " to " + activity.getAfter();
                } else {
                    currActivityRowValue = activity.getCertResult().getNumber() + " from " + 
                            activity.getBefore() + " to " + activity.getAfter();
                    currRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.GAP_EDITED),
                            currActivityRowValue);
                }
            }
        }
    
    private void putDeveloperActivitiesInRow(List<QuestionableActivityDeveloperDTO> developerActivityForDate,
            List<String> activityRow) {
        for(QuestionableActivityDeveloperDTO activity : developerActivityForDate) {
            if(activityRow.size() == 0) {
                //fill in info about the developer that will be the same for every
                //activity found in this date bucket
                activityRow.add("");
                activityRow.add(activity.getDeveloper().getName());
                activityRow.add("");
                activityRow.add("");
                activityRow.add("");
                activityRow.add("");
                activityRow.add("LINK_TO_DEVELOPER_ACTIVITY_REPORT");
                activityRow.add(activity.getUser().getSubjectName());
            } 
            
            if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED.getName())) {
                activityRow.set(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_NAME_EDITED),
                        "From " + activity.getBefore() + " to " + activity.getAfter());
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED.getName())) {
                String currActivityRowValue = activityRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; From " + activity.getBefore() + " to " + activity.getAfter();
                } else {
                    currActivityRowValue = "From " + activity.getBefore() + " to " + activity.getAfter();
                    activityRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_ADDED.getName())) {
                String currActivityRowValue = activityRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; Added status " + activity.getAfter();
                } else {
                    currActivityRowValue = "Added status " + activity.getAfter();
                    activityRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
                String currActivityRowValue = activityRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; Removed status " + activity.getBefore();
                } else {
                    currActivityRowValue = "Removed status " + activity.getBefore();
                    activityRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
                String currActivityRowValue = activityRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; Changed status from " + activity.getBefore() + " to " + activity.getAfter();
                } else {
                    currActivityRowValue = "Changed status from " + activity.getBefore() + " to " + activity.getAfter();
                    activityRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.DEVELOPER_STATUS_EDITED),
                            currActivityRowValue);
                }
            }
        }
    }
    
    private void putProductActivitiesInRow(List<QuestionableActivityProductDTO> productActivityForDate,
            List<String> activityRow) {
        for(QuestionableActivityProductDTO activity : productActivityForDate) {
            if(activityRow.size() == 0) {
                //fill in info about the developer that will be the same for every
                //activity found in this date bucket
                activityRow.add("");
                activityRow.add(activity.getProduct().getDeveloperName());
                activityRow.add(activity.getProduct().getName());
                activityRow.add("");
                activityRow.add("");
                activityRow.add("");
                activityRow.add("LINK_TO_PRODUCT_ACTIVITY_REPORT");
                activityRow.add(activity.getUser().getSubjectName());
            } 
            
            if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED.getName())) {
                activityRow.set(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_NAME_EDITED),
                        "From " + activity.getBefore() + " to " + activity.getAfter());
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED.getName())) {
                String currActivityRowValue = activityRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; From " + activity.getBefore() + " to " + activity.getAfter();
                } else {
                    currActivityRowValue = "From " + activity.getBefore() + " to " + activity.getAfter();
                    activityRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.PRODUCT_OWNER_HISTORY_ADDED.getName())) {
                String currActivityRowValue = activityRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; Added owner " + activity.getAfter();
                } else {
                    currActivityRowValue = "Added owner " + activity.getAfter();
                    activityRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_REMOVED.getName())) {
                String currActivityRowValue = activityRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; Removed owner " + activity.getBefore();
                } else {
                    currActivityRowValue = "Removed owner " + activity.getBefore();
                    activityRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED),
                            currActivityRowValue);
                }
            } else if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.DEVELOPER_STATUS_HISTORY_EDITED.getName())) {
                String currActivityRowValue = activityRow.get(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED));
                if(!StringUtils.isEmpty(currActivityRowValue)) {
                    currActivityRowValue += "; Changed owner from " + activity.getBefore() + " to " + activity.getAfter();
                } else {
                    currActivityRowValue = "Changed owner from " + activity.getBefore() + " to " + activity.getAfter();
                    activityRow.set(triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.PRODUCT_OWNER_EDITED),
                            currActivityRowValue);
                }
            }
        }
    }
    
    private void putVersionActivitiesInRow(List<QuestionableActivityVersionDTO> versionActivityForDate,
            List<String> activityRow) {
        for(QuestionableActivityVersionDTO activity : versionActivityForDate) {
            if(activityRow.size() == 0) {
                //fill in info about the developer that will be the same for every
                //activity found in this date bucket
                activityRow.add("");
                activityRow.add("GET DEVELOPER NAME");
                activityRow.add(activity.getVersion().getProductName());
                activityRow.add(activity.getVersion().getVersion());
                activityRow.add("");
                activityRow.add("");
                activityRow.add("LINK_TO_VERSION_ACTIVITY_REPORT");
                activityRow.add(activity.getUser().getSubjectName());
            } 
            
            if(activity.getTrigger().getName().equals(
                    QuestionableActivityTriggerConcept.VERSION_NAME_EDITED.getName())) {
                activityRow.set(
                        triggerColumnIndexMap.get(QuestionableActivityTriggerConcept.VERSION_NAME_EDITED),
                        "From " + activity.getBefore() + " to " + activity.getAfter());
            }
        }
    }
    
    public QuestionableActivityDAO getQaDao() {
        return qaDao;
    }

    public void setQaDao(QuestionableActivityDAO qaDao) {
        this.qaDao = qaDao;
    }

    public SimpleDateFormat getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(final SimpleDateFormat timestampFormat) {
        this.timestampFormat = timestampFormat;
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
        } catch(ParseException ex) {
            LOGGER.error("Could not parse " + startDateStr + " as a date. Please make sure the " 
                    + " start date is in the format yyyy-MM-dd.");
            return;
        }
        
        try {
            endDate = startEndDateFormat.parse(endDateStr);
         } catch(ParseException ex) {
             LOGGER.error("Could not parse " + endDateStr + " as a date. Please make sure the " 
                     + " end date is in the format yyyy-MM-dd.");
             return;
         }
        
        QuestionableActivityReportApp app = new QuestionableActivityReportApp(startDate, endDate);
        app.setLocalContext();
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        app.initiateSpringBeans(context);
        app.runJob();
        context.close();
    }
}
