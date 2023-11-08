package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;

@Component
public class RemovedFunctionalityTestedAddedActivity implements CertificationResultActivity {

    @Override
     public List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult) {
        List<CertificationResultFunctionalityTested> addedFunctionalitiesTested = subtractCertificationResultFunctionalityTestedLists(
                newCertResult.getFunctionalitiesTested() == null ? new ArrayList<CertificationResultFunctionalityTested>() : newCertResult.getFunctionalitiesTested(),
                origCertResult.getFunctionalitiesTested() == null ? new ArrayList<CertificationResultFunctionalityTested>() : origCertResult.getFunctionalitiesTested());

        return addedFunctionalitiesTested.stream()
                .filter(crft -> crft.getFunctionalityTested().isRetired())
                .map(crft -> {
                    QuestionableActivityCertificationResult questAct = new QuestionableActivityCertificationResult();
                    questAct.setAfter(crft.getFunctionalityTested().getValue());
                    return questAct;
                })
                .collect(Collectors.toList());
    }

    private List<CertificationResultFunctionalityTested> subtractCertificationResultFunctionalityTestedLists(List<CertificationResultFunctionalityTested> listA,
            List<CertificationResultFunctionalityTested> listB) {

        Predicate<CertificationResultFunctionalityTested> notInListB = functionalityTestedFromA -> !listB.stream()
                .anyMatch(funcTested -> functionalityTestedFromA.getFunctionalityTested().getId().equals(funcTested.getFunctionalityTested().getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REMOVED_FUNCTIONALITY_TESTED_ADDED;
    }

}
