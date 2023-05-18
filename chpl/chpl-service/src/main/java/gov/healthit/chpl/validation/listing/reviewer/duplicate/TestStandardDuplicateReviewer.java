package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("testStandardDuplicateReviewer")
public class TestStandardDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestStandardDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultTestStandard> testStandardDuplicateResults =
                new DuplicateReviewResult<CertificationResultTestStandard>(getPredicate());


        if (certificationResult.getTestStandards() != null) {
            for (CertificationResultTestStandard dto : certificationResult.getTestStandards()) {
                testStandardDuplicateResults.addObject(dto);
            }
        }

        if (testStandardDuplicateResults.duplicatesExist()) {
            listing.addAllWarningMessages(
                    getWarnings(testStandardDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion()))
                    .stream()
                    .collect(Collectors.toSet()));
            certificationResult.setTestStandards(testStandardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultTestStandard> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestStandard duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestStandard",
                    criteria, duplicate.getTestStandardName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultTestStandard, CertificationResultTestStandard> getPredicate() {
        return new BiPredicate<CertificationResultTestStandard, CertificationResultTestStandard>() {
            @Override
            public boolean test(CertificationResultTestStandard ts1,
                    CertificationResultTestStandard ts2) {
                return (ObjectUtils.allNotNull(ts1.getTestStandardId(), ts2.getTestStandardId())
                        && Objects.equals(ts1.getTestStandardId(),  ts2.getTestStandardId()))
                    || Objects.equals(ts1.getTestStandardName(), ts2.getTestStandardName());
            }
        };
    }
}
