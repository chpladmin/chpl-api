package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.List;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;

public interface CertificationResultActivity {
    List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult);
    QuestionableActivityTriggerConcept getTriggerType();
}
