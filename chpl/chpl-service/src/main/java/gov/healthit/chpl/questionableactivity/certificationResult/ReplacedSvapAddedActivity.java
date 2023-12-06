package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;

@Component
public class ReplacedSvapAddedActivity implements CertificationResultActivity {

    @Override
     public List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult) {
        List<CertificationResultSvap> addedSvaps = subtractCertificationResultSvapLists(
                newCertResult.getSvaps() == null ? new ArrayList<CertificationResultSvap>() : newCertResult.getSvaps(),
                origCertResult.getSvaps() == null ? new ArrayList<CertificationResultSvap>() : origCertResult.getSvaps());

        return addedSvaps.stream()
                .filter(crs -> crs.isReplaced())
                .map(crs -> {
                    QuestionableActivityCertificationResult questAct = new QuestionableActivityCertificationResult();
                    questAct.setAfter(crs.getRegulatoryTextCitation() + ": " + crs.getApprovedStandardVersion());
                    return questAct;
                })
                .collect(Collectors.toList());
    }

    private List<CertificationResultSvap> subtractCertificationResultSvapLists(List<CertificationResultSvap> listA,
            List<CertificationResultSvap> listB) {

        Predicate<CertificationResultSvap> notInListB = svapFromA -> !listB.stream()
                .anyMatch(svap -> svapFromA.getSvapId().equals(svap.getSvapId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REPLACED_SVAP_ADDED;
    }

}
