package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;

@Component
public class UpdatedG1SuccessActivity implements CertificationResultActivity {

    @Override
     public List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult) {
        List<QuestionableActivityCertificationResult> updatedG1Activities = new ArrayList<QuestionableActivityCertificationResult>();
        if (origCertResult.getG1Success() != null || newCertResult.getG1Success() != null) {
            if ((origCertResult.getG1Success() == null && newCertResult.getG1Success() != null)
                    || (!origCertResult.getG1Success() && newCertResult.getG1Success())) {
                //g1 success changed to true
                QuestionableActivityCertificationResult activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.FALSE.toString());
                activity.setAfter(Boolean.TRUE.toString());
                updatedG1Activities.add(activity);
            } else if ((origCertResult.getG1Success() != null && newCertResult.getG1Success() == null)
                    || (origCertResult.getG1Success() && !newCertResult.getG1Success())) {
                //g1 success changed to false
                QuestionableActivityCertificationResult activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.TRUE.toString());
                activity.setAfter(Boolean.FALSE.toString());
                updatedG1Activities.add(activity);
            }
        }
        return updatedG1Activities;
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.G1_SUCCESS_EDITED;
    }

}
