package gov.healthit.chpl.questionableactivity.certificationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityCertificationResult;
import gov.healthit.chpl.testtool.CertificationResultTestTool;

@Component
public class RemovedTestToolAddedActivity implements CertificationResultActivity {

    @Override
     public List<QuestionableActivityCertificationResult> check(CertificationResult origCertResult, CertificationResult newCertResult) {
        List<CertificationResultTestTool> addedTestTools = subtractCertificationResultTestToolLists(
                newCertResult.getTestToolsUsed() == null ? new ArrayList<CertificationResultTestTool>() : newCertResult.getTestToolsUsed(),
                origCertResult.getTestToolsUsed() == null ? new ArrayList<CertificationResultTestTool>() : origCertResult.getTestToolsUsed());

        return addedTestTools.stream()
                .filter(crtt -> crtt.getTestTool().isRetired())
                .map(crtt -> {
                    QuestionableActivityCertificationResult questAct = new QuestionableActivityCertificationResult();
                    questAct.setAfter(crtt.getTestTool().getValue());
                    return questAct;
                })
                .collect(Collectors.toList());
    }

    private List<CertificationResultTestTool> subtractCertificationResultTestToolLists(List<CertificationResultTestTool> listA,
            List<CertificationResultTestTool> listB) {

        Predicate<CertificationResultTestTool> notInListB = testToolFromA -> !listB.stream()
                .anyMatch(testTool -> testToolFromA.getTestTool().getId().equals(testTool.getTestTool().getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REMOVED_TEST_TOOL_ADDED;
    }

}
