package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("testFunctionalityDuplicateReviewer")
public class TestFunctionalityDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestFunctionalityDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultFunctionalityTested> testFunctionalityDuplicateResults =
                new DuplicateReviewResult<CertificationResultFunctionalityTested>(getPredicate());

        if (certificationResult.getFunctionalitiesTested() != null) {
            for (CertificationResultFunctionalityTested dto : certificationResult.getFunctionalitiesTested()) {
                testFunctionalityDuplicateResults.addObject(dto);
            }
        }

        if (testFunctionalityDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(
                            testFunctionalityDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setFunctionalitiesTested(testFunctionalityDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultFunctionalityTested> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultFunctionalityTested duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestFunctionality",
                    criteria, duplicate.getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<
    CertificationResultFunctionalityTested, CertificationResultFunctionalityTested> getPredicate() {
        return
                new BiPredicate<
                CertificationResultFunctionalityTested, CertificationResultFunctionalityTested>() {
            @Override
            public boolean test(CertificationResultFunctionalityTested tf1, CertificationResultFunctionalityTested tf2) {
                return (ObjectUtils.allNotNull(tf1.getFunctionalityTestedId(), tf2.getFunctionalityTestedId())
                        && Objects.equals(tf1.getFunctionalityTestedId(),  tf2.getFunctionalityTestedId()))
                    || Objects.equals(tf1.getName(), tf2.getName());
            }
        };
    }
}
