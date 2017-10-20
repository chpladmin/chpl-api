package gov.healthit.chpl.quesitonableActivity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;

@Component
public class CertificationResultQuestionableActivityProvider {
    
    public QuestionableActivityCertificationResultDTO checkG1SuccessUpdated(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        QuestionableActivityCertificationResultDTO activity = null;
        if (origCertResult.isG1Success() != null || newCertResult.isG1Success() != null) {
            if ((origCertResult.isG1Success() == null && newCertResult.isG1Success() != null) || 
                 (origCertResult.isG1Success() == Boolean.FALSE && newCertResult.isG1Success() == Boolean.TRUE)) {
                //g1 success changed to true
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("TRUE");
            } else if ((origCertResult.isG1Success() != null && newCertResult.isG1Success() == null) || 
                    (origCertResult.isG1Success() == Boolean.TRUE && newCertResult.isG1Success() == Boolean.FALSE)) {
                //g1 success changed to false
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("FALSE");
            }
        }
        return activity;
    }
    
    public QuestionableActivityCertificationResultDTO checkG2SuccessUpdated(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        QuestionableActivityCertificationResultDTO activity = null;
        if (origCertResult.isG2Success() != null || newCertResult.isG2Success() != null) {
            if ((origCertResult.isG2Success() == null && newCertResult.isG2Success() != null) || 
                 (origCertResult.isG2Success() == Boolean.FALSE && newCertResult.isG2Success() == Boolean.TRUE)) {
                //g2 success changed to true
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("TRUE");
            } else if ((origCertResult.isG2Success() != null && newCertResult.isG2Success() == null) || 
                    (origCertResult.isG2Success() == Boolean.TRUE && newCertResult.isG2Success() == Boolean.FALSE)) {
                //g2 success changed to false
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("FALSE");
            }
        }
        
        return activity;
    }
    
    public QuestionableActivityCertificationResultDTO checkGapUpdated(
            CertificationResult origCertResult, CertificationResult newCertResult) {
        
        QuestionableActivityCertificationResultDTO activity = null;
        if (origCertResult.isGap() != null || newCertResult.isGap() != null) {
            if ((origCertResult.isGap() == null && newCertResult.isGap() != null) || 
                 (origCertResult.isGap() == Boolean.FALSE && newCertResult.isGap() == Boolean.TRUE)) {
                //gap changed to true
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("TRUE");
            } else if ((origCertResult.isGap() != null && newCertResult.isGap() == null) || 
                    (origCertResult.isGap() == Boolean.TRUE && newCertResult.isGap() == Boolean.FALSE)) {
                //gap changed to false
                activity = new QuestionableActivityCertificationResultDTO();
                activity.setMessage("FALSE");
            }
        }
        return activity;
    }
}
