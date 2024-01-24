package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;

@Component
public class UpdatedCodeSetActivity implements CertificationResultActivity {

    @Override
    public List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult) {
        List<QuestionableActivityCertificationResult> results = new ArrayList<QuestionableActivityCertificationResult>();

        if (origCertResult.getCodeSets() != null && newCertResult.getCodeSets() != null
                && origCertResult.getCodeSets() && !newCertResult.getCodeSets()) {
            results.add(QuestionableActivityCertificationResult.builder()
                    .before(origCertResult.getCodeSets().toString())
                    .after(newCertResult.getCodeSets().toString())
                    .build());
        }
        return results;
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CODE_SET_EDITED;
    }

}
