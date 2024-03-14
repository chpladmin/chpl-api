package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;

@Component
public class CodeSetRemovedActivity implements CertificationResultActivity {

    @Override
    public List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult) {
        List<QuestionableActivityCertificationResult> questionableActivityCertificationResults = new ArrayList<QuestionableActivityCertificationResult>();

        if (CollectionUtils.isNotEmpty(origCertResult.getCodeSets())) {
            questionableActivityCertificationResults = origCertResult.getCodeSets().stream()
                    .filter(csCertResult -> !doesCodeSetExistInCodeSetCertifcationResults(csCertResult.getCodeSet(), newCertResult.getCodeSets()))
                    .map(csCertResult -> {
                        QuestionableActivityCertificationResult questAct = new QuestionableActivityCertificationResult();
                        questAct.setBefore(csCertResult.getCodeSet().getRequiredDay().toString());
                        return questAct;
                    })
                    .toList();

        }
        return questionableActivityCertificationResults;
    }

    private Boolean doesCodeSetExistInCodeSetCertifcationResults(CodeSet codeSet, List<CertificationResultCodeSet> results) {
        if (CollectionUtils.isNotEmpty(results)) {
            return results.stream()
                    .filter(cs -> cs.getId().equals(codeSet.getId()))
                    .findAny()
                    .isPresent();
        } else {
            return false;
        }
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REMOVED_CODE_SET;
    }
}
