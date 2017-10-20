package gov.healthit.chpl.quesitonableActivity;

import java.util.Date;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityVersionDTO;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.util.CertificationResultRules;

@Aspect
public class QuestionableActivityAspect {
    @Autowired private Environment env;
    @Autowired private CertificationResultRules certResultRules;
    @Autowired private QuestionableActivityDAO questionableActivityDao;
    @Autowired private DeveloperQuestionableActivityProvider developerQuestionableActivityProvider;
    @Autowired private ProductQuestionableActivityProvider productQuestionableActivityProvider;
    @Autowired private VersionQuestionableActivityProvider versionQuestionableActivityProvider;
    @Autowired private ListingQuestionableActivityProvider listingQuestionableActivityProvider;
    @Autowired private CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider;

    private long listingActivityThresholdMillis = -1;
    public QuestionableActivityAspect() {
        // load the different trigger types
        String activityThresholdDaysStr = env.getProperty("questionableActivityThresholdDays");
        int activityThresholdDays = new Integer(activityThresholdDaysStr).intValue();
        listingActivityThresholdMillis = activityThresholdDays * 24 * 60 * 60 * 1000;
    }
    
    @After("execution(* gov.healthit.chpl.manager.impl.ActivityManagerImpl.addActivity(..) && "
            + "args(originalData,newData,..))")
    public void checkQuestionableActivity(JoinPoint joinPoint, Object originalData, Object newData) {
        if(originalData == null || newData == null || 
                !originalData.getClass().equals(newData.getClass())) {
            return;
        }
        
        //all questionable activity from this action should have the exact same date and user id
        Date activityDate = new Date();
        Long activityUser = Util.getCurrentUser().getId();
        
        if(originalData instanceof CertifiedProductSearchDetails && 
                newData instanceof CertifiedProductSearchDetails) {
            CertifiedProductSearchDetails origListing = (CertifiedProductSearchDetails)originalData;
            CertifiedProductSearchDetails newListing = (CertifiedProductSearchDetails)newData;
            
            //look for any of the listing questionable activity
            checkListingQuestionableActivity(origListing, newListing, activityDate, activityUser);

            //look for certification result questionable activity
            if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0 && 
                newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
                
                //all cert results are in the details so find matches based on the 
                //original and new criteira number fields
                for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                    for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                        if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                            checkCertificationResultQuestionableActivity(origCertResult, newCertResult, 
                                    activityDate, activityUser);
                        }
                    }
                }
            }
        } else if(originalData instanceof DeveloperDTO && newData instanceof DeveloperDTO) {
            DeveloperDTO origDeveloper = (DeveloperDTO)originalData;
            DeveloperDTO newDeveloper = (DeveloperDTO)newData;
            QuestionableActivityDeveloperDTO activity = 
                    developerQuestionableActivityProvider.checkNameUpdated(origDeveloper, newDeveloper);
            if(activity != null) {
                createDeveloperActivity(activity, newDeveloper.getId(), activityDate, activityUser, null);
            }
            //TODO: status updates
        } else if(originalData instanceof ProductDTO && newData instanceof ProductDTO) {
            ProductDTO origProduct = (ProductDTO)originalData;
            ProductDTO newProduct = (ProductDTO)newData;
            QuestionableActivityProductDTO activity = 
                    productQuestionableActivityProvider.checkNameUpdated(origProduct, newProduct);
            if(activity != null) {
                createProductActivity(activity, newProduct.getId(), activityDate, activityUser, null);
            }
            //TODO: owner updates
        } else if(originalData instanceof ProductVersionDTO && newData instanceof ProductVersionDTO) {
            ProductVersionDTO origVersion = (ProductVersionDTO)originalData;
            ProductVersionDTO newVersion = (ProductVersionDTO)newData;
            QuestionableActivityVersionDTO activity = 
                    versionQuestionableActivityProvider.checkNameUpdated(origVersion, newVersion);
            if(activity != null) {
                createVersionActivity(activity, origVersion.getId(), activityDate, activityUser, null);
            }
        }
    }    
    
    private void checkListingQuestionableActivity(CertifiedProductSearchDetails origListing, 
            CertifiedProductSearchDetails newListing, Date activityDate, Long activityUser) {
        QuestionableActivityListingDTO activity = listingQuestionableActivityProvider.check2011EditionUpdated(origListing, newListing);
        if(activity != null) {
            createListingActivity(activity, origListing.getId(), activityDate, activityUser, null);
        } else {
            //if it wasn't a 2011 update, check for other changes outside 
            //of the acceptable activity threshold
            if (origListing.getCertificationDate() != null && newListing.getCertificationDate() != null
                    && (newListing.getLastModifiedDate().longValue()
                            - origListing.getCertificationDate().longValue() > listingActivityThresholdMillis)) {
                activity = listingQuestionableActivityProvider.checkCertificationStatusUpdated(origListing, newListing);
                if(activity != null) {
                    createListingActivity(activity, origListing.getId(), activityDate, activityUser, null);
                }
                
                activity = listingQuestionableActivityProvider.checkSurveillanceDeleted(origListing, newListing);
                if(activity != null) {
                    createListingActivity(activity, origListing.getId(), activityDate, activityUser, null);
                }
                
                List<QuestionableActivityListingDTO> activities = listingQuestionableActivityProvider.checkCqmsAdded(origListing, newListing);
                if(activities != null && activities.size() > 0) {
                    for(QuestionableActivityListingDTO currActivity : activities) {
                        createListingActivity(currActivity, origListing.getId(), activityDate, activityUser, null);
                    }
                }
                
                activities = listingQuestionableActivityProvider.checkCqmsRemoved(origListing, newListing);
                if(activities != null && activities.size() > 0) {
                    for(QuestionableActivityListingDTO currActivity : activities) {
                        createListingActivity(currActivity, origListing.getId(), activityDate, activityUser, null);
                    }
                }

                activities = listingQuestionableActivityProvider.checkCertificationsAdded(origListing, newListing);
                if(activities != null && activities.size() > 0) {
                    for(QuestionableActivityListingDTO currActivity : activities) {
                        createListingActivity(currActivity, origListing.getId(), activityDate, activityUser, null);
                    }
                }
                
                activities = listingQuestionableActivityProvider.checkCertificationsRemoved(origListing, newListing);
                if(activities != null && activities.size() > 0) {
                    for(QuestionableActivityListingDTO currActivity : activities) {
                        createListingActivity(currActivity, origListing.getId(), activityDate, activityUser, null);
                    }
                }
            }
        }
    }
    
    private void checkCertificationResultQuestionableActivity(CertificationResult origCertResult, CertificationResult newCertResult,
            Date activityDate, Long activityUser) {
        QuestionableActivityCertificationResultDTO certActivity = null;
        List<QuestionableActivityCertificationResultDTO> certActivities = null;
        
        if(certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G1_SUCCESS)) {
            certActivity = certResultQuestionableActivityProvider.checkG1SuccessUpdated(origCertResult, newCertResult);
            if(certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activityDate, activityUser, null);
            }
        }
        if(certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G2_SUCCESS)) {
            certActivity = certResultQuestionableActivityProvider.checkG2SuccessUpdated(origCertResult, newCertResult);
            if(certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activityDate, activityUser, null);
            }
        }
        if(certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.GAP)) {
            certActivity = certResultQuestionableActivityProvider.checkGapUpdated(origCertResult, newCertResult);
            if(certActivity != null) {
                createCertificationActivity(certActivity, origCertResult.getId(), activityDate, activityUser, null);
            }
        }
        if(certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G1_MACRA)) {
            certActivities = certResultQuestionableActivityProvider.checkG1MacraMeasuresAdded(origCertResult, newCertResult);
            if(certActivities != null && certActivities.size() > 0) {
                for(QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                    if(certActivity != null) {
                        createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate, activityUser, null);
                    }
                }
            }
        }
        if(certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G1_MACRA)) {
            certActivities = certResultQuestionableActivityProvider.checkG1MacraMeasuresRemoved(origCertResult, newCertResult);
            if(certActivities != null && certActivities.size() > 0) {
                for(QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                    if(certActivity != null) {
                        createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate, activityUser, null);
                    }
                }
            }
        }
        if(certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G2_MACRA)) {
            certActivities = certResultQuestionableActivityProvider.checkG2MacraMeasuresAdded(origCertResult, newCertResult);
            if(certActivities != null && certActivities.size() > 0) {
                for(QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                    if(certActivity != null) {
                        createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate, activityUser, null);
                    }
                }
            }
        }
        if(certResultRules.hasCertOption(origCertResult.getNumber(), CertificationResultRules.G2_MACRA)) {
            certActivities = certResultQuestionableActivityProvider.checkG2MacraMeasuresRemoved(origCertResult, newCertResult);
            if(certActivities != null && certActivities.size() > 0) {
                for(QuestionableActivityCertificationResultDTO currCertActivity : certActivities) {
                    if(certActivity != null) {
                        createCertificationActivity(currCertActivity, origCertResult.getId(), activityDate, activityUser, null);
                    }
                }
            }
        }
    }
    
    private void createListingActivity(QuestionableActivityListingDTO activity, Long listingId, 
            Date activityDate, Long activityUser, Long triggerId) {
        activity.setListingId(listingId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setTriggerId(triggerId);
        questionableActivityDao.create(activity);
    }
    
    private void createCertificationActivity(QuestionableActivityCertificationResultDTO activity, Long certResultId, 
            Date activityDate, Long activityUser, Long triggerId) {
        activity.setCertResultId(certResultId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setTriggerId(triggerId);
        questionableActivityDao.create(activity);
    }
    
    private void createDeveloperActivity(QuestionableActivityDeveloperDTO activity, Long developerId, 
            Date activityDate, Long activityUser, Long triggerId) {
        activity.setDeveloperId(developerId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setTriggerId(triggerId);
        questionableActivityDao.create(activity);
    }
    
    private void createProductActivity(QuestionableActivityProductDTO activity, Long productId, 
            Date activityDate, Long activityUser, Long triggerId) {
        activity.setProductId(productId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setTriggerId(triggerId);
        questionableActivityDao.create(activity);
    }
    
    private void createVersionActivity(QuestionableActivityVersionDTO activity, Long versionId, 
            Date activityDate, Long activityUser, Long triggerId) {
        activity.setVersionId(versionId);
        activity.setActivityDate(activityDate);
        activity.setUserId(activityUser);
        activity.setTriggerId(triggerId);
        questionableActivityDao.create(activity);
    }
}
