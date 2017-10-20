package gov.healthit.chpl.quesitonableActivity;

import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.manager.SurveillanceManager;

@Aspect
public class QuestionableActivityAspect {
    @Autowired Environment env;
    @Autowired SurveillanceManager survManager;
    @Autowired ListingQuestionableActivityProvider listingQuestionableActivityProvider;
    @Autowired CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider;

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
            //TODO: look for any of the listing triggers
            
            QuestionableActivityListingDTO activity = listingQuestionableActivityProvider.check2011EditionUpdated(origListing, newListing);
            if(activity != null) {
                //TODO insert
            } else {
                //if it wasn't a 2011 update, check for other changes outside 
                //of the acceptable activity threshold
                if (origListing.getCertificationDate() != null && newListing.getCertificationDate() != null
                        && (newListing.getLastModifiedDate().longValue()
                                - origListing.getCertificationDate().longValue() > listingActivityThresholdMillis)) {
                    activity = listingQuestionableActivityProvider.checkCertificationStatusUpdated(origListing, newListing);
                    if(activity != null) {
                        //TODO insert
                        
                    }
                    
                    activity = listingQuestionableActivityProvider.checkSurveillanceDeleted(origListing, newListing);
                    if(activity != null) {
                        //TODO insert
                    }
                    
                    activity = listingQuestionableActivityProvider.checkCqmsAdded(origListing, newListing);
                    if(activity != null) {
                        //TODO insert
                    }
                    
                    activity = listingQuestionableActivityProvider.checkCqmsRemoved(origListing, newListing);
                    if(activity != null) {
                        //TODO insert
                    }
                    
                    activity = listingQuestionableActivityProvider.checkCertificationsAdded(origListing, newListing);
                    if(activity != null) {
                        //TODO insert
                    }
                    
                    activity = listingQuestionableActivityProvider.checkCertificationsRemoved(origListing, newListing);
                    if(activity != null) {
                        //TODO insert
                    }
                    
                    
                    //look for certification result questionable activity
                    if (origListing.getCertificationResults() != null && origListing.getCertificationResults().size() > 0 && 
                        newListing.getCertificationResults() != null && newListing.getCertificationResults().size() > 0) {
                        //all cert results are in the details so find matches based on the 
                        //original and new criteira number fields
                        for (CertificationResult origCertResult : origListing.getCertificationResults()) {
                            for (CertificationResult newCertResult : newListing.getCertificationResults()) {
                                if (origCertResult.getNumber().equals(newCertResult.getNumber())) {
                                    certResultQuestionableActivityProvider.checkG1SuccessUpdated(origCertResult, newCertResult);
                                    certResultQuestionableActivityProvider.checkG2SuccessUpdated(origCertResult, newCertResult);
                                    certResultQuestionableActivityProvider.checkG1MacraMeasuresAdded(origCertResult, newCertResult);
                                    certResultQuestionableActivityProvider.checkG1MacraMeasuresRemoved(origCertResult, newCertResult);
                                    certResultQuestionableActivityProvider.checkG2MacraMeasuresAdded(origCertResult, newCertResult);
                                    certResultQuestionableActivityProvider.checkG2MacraMeasuresRemoved(origCertResult, newCertResult);
                                    certResultQuestionableActivityProvider.checkGapUpdated(origCertResult, newCertResult);
                                }
                            }
                        }
                    }
                }
            }  
        }
    }
}
