package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;

@Component
public class UpdatedGapActivity implements CertificationResultActivity {

    @Override
     public List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult) {
        List<QuestionableActivityCertificationResult> updatedGapActivities = new ArrayList<QuestionableActivityCertificationResult>();
        if (origCertResult.isGap() != null || newCertResult.isGap() != null) {
            if ((origCertResult.isGap() == null && newCertResult.isGap() != null)
                    ||  (!origCertResult.isGap() && newCertResult.isGap())) {
                //gap changed to true
                QuestionableActivityCertificationResult activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.FALSE.toString());
                activity.setAfter(Boolean.TRUE.toString());
                updatedGapActivities.add(activity);
            } else if ((origCertResult.isGap() != null && newCertResult.isGap() == null)
                    || (origCertResult.isGap() && !newCertResult.isGap())) {
                //gap changed to false
                QuestionableActivityCertificationResult activity = new QuestionableActivityCertificationResult();
                activity.setBefore(Boolean.TRUE.toString());
                activity.setAfter(Boolean.FALSE.toString());
                updatedGapActivities.add(activity);
            }
        }
        return updatedGapActivities;
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.GAP_EDITED;
    }

}
