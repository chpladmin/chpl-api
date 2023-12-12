package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;
import gov.healthit.chpl.standard.CertificationResultStandard;

@Component
public class RetiredStandardAddedActivity implements CertificationResultActivity {

    @Override
     public List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult) {
        List<CertificationResultStandard> addedStandards = subtractCertificationResultStandardLists(
                newCertResult.getStandards() == null ? new ArrayList<CertificationResultStandard>() : newCertResult.getStandards(),
                origCertResult.getStandards() == null ? new ArrayList<CertificationResultStandard>() : origCertResult.getStandards());

        return addedStandards.stream()
                .filter(crs -> crs.getStandard().isRetired())
                .map(crs -> {
                    QuestionableActivityCertificationResult questAct = new QuestionableActivityCertificationResult();
                    questAct.setAfter(crs.getStandard().getValue());
                    return questAct;
                })
                .collect(Collectors.toList());
    }

    private List<CertificationResultStandard> subtractCertificationResultStandardLists(List<CertificationResultStandard> listA,
            List<CertificationResultStandard> listB) {

        Predicate<CertificationResultStandard> notInListB = standardFromA -> !listB.stream()
                .anyMatch(std -> standardFromA.getStandard().getId().equals(std.getStandard().getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.RETIRED_STANDARD_ADDED;
    }

}
