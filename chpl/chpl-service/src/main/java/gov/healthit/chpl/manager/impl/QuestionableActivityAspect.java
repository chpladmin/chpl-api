package gov.healthit.chpl.manager.impl;

import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.manager.SurveillanceManager;

@Aspect
public class QuestionableActivityAspect {
    @Autowired SurveillanceManager survManager;
    
    @After("execution(* gov.healthit.chpl.manager.impl.ActivityManagerImpl.addActivity(..) && "
            + "args(originalData,newData,..))")
    public void logBefore(JoinPoint joinPoint, Object originalData, Object newData) {
        if(originalData == null || newData == null) {
            return;
        }
        if(originalData instanceof CertifiedProductSearchDetails && 
                newData instanceof CertifiedProductSearchDetails) {
            //TODO: look for any of the listing triggers
        }
        
        Object[] args = joinPoint.getArgs();
        for(Object arg : args) {
            if(arg instanceof Surveillance) {
                //this is the surveillance that was deleted
                QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                activity.setActivityDate(new Date());
                activity.setUserId(Util.getCurrentUser().getId());
                Surveillance surv = (Surveillance)arg;
                activity.setListingId(surv.getCertifiedProduct().getId());
                activity.setMessage("TRUE");
                activity.setTriggerId(1L);
                break;
            }
        }
    }

}
