package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestFunctionality;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
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

        DuplicateReviewResult<CertificationResultTestFunctionality> testFunctionalityDuplicateResults =
                new DuplicateReviewResult<CertificationResultTestFunctionality>(getPredicate());

        if (certificationResult.getTestFunctionality() != null) {
            for (CertificationResultTestFunctionality dto : certificationResult.getTestFunctionality()) {
                testFunctionalityDuplicateResults.addObject(dto);
            }
        }

        if (testFunctionalityDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(
                            testFunctionalityDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setTestFunctionality(testFunctionalityDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultTestFunctionality> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestFunctionality duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestFunctionality",
                    criteria, duplicate.getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<
    CertificationResultTestFunctionality, CertificationResultTestFunctionality> getPredicate() {
        return
                new BiPredicate<
                CertificationResultTestFunctionality, CertificationResultTestFunctionality>() {
            @Override
            public boolean test(CertificationResultTestFunctionality tf1, CertificationResultTestFunctionality tf2) {
                return (ObjectUtils.allNotNull(tf1.getTestFunctionalityId(), tf2.getTestFunctionalityId())
                        && Objects.equals(tf1.getTestFunctionalityId(),  tf2.getTestFunctionalityId()))
                    || Objects.equals(tf1.getName(), tf2.getName());
            }
        };
    }
}
