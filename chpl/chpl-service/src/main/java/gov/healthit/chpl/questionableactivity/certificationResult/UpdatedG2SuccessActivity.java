package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;

@Component
public class UpdatedG2SuccessActivity implements CertificationResultActivity {

    @Override
     public List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult) {
        List<QuestionableActivityCertificationResult> updatedG2Activities = new ArrayList<QuestionableActivityCertificationResult>();
        if (origCertResult.isG2Success() != null || newCertResult.isG2Success() != null) {
            if ((origCertResult.isG2Success() == null && newCertResult.isG2Success() != null)
                    || (!origCertResult.isG2Success() && newCertResult.isG2Success())) {
                //G2 success changed to true
                QuestionableActivityCertificationResult activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.FALSE.toString());
                activity.setAfter(Boolean.TRUE.toString());
                updatedG2Activities.add(activity);
            } else if ((origCertResult.isG2Success() != null && newCertResult.isG2Success() == null)
                    || (origCertResult.isG2Success() && !newCertResult.isG2Success())) {
                //G2 success changed to false
                QuestionableActivityCertificationResult activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.TRUE.toString());
                activity.setAfter(Boolean.FALSE.toString());
                updatedG2Activities.add(activity);
            }
        }
        return updatedG2Activities;
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.G2_SUCCESS_EDITED;
    }

}
