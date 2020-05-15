package gov.healthit.chpl.validation.listing.reviewer.edition2014.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("testProcedure2014DuplicateReviewer")
public class TestProcedure2014DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestProcedure2014DuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultTestProcedure> testProcedureDuplicateResults =
                new DuplicateReviewResult<CertificationResultTestProcedure>(getPredicate());

        if (certificationResult.getTestProcedures() != null) {
            for (CertificationResultTestProcedure dto : certificationResult.getTestProcedures()) {
                testProcedureDuplicateResults.addObject(dto);
            }
        }

        if (testProcedureDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testProcedureDuplicateResults.getDuplicateList(),
                            certificationResult.getCriterion().getNumber()));
            certificationResult.setTestProcedures(testProcedureDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultTestProcedure> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestProcedure duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedure.2014",
                    criteria, duplicate.getTestProcedureVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure> getPredicate() {
        return new BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure>() {
            @Override
            public boolean test(CertificationResultTestProcedure dto1,
                    CertificationResultTestProcedure dto2) {
                return ObjectUtils.allNotNull(dto1.getTestProcedureVersion(), dto2.getTestProcedureVersion())
                        && dto1.getTestProcedureVersion().equals(dto2.getTestProcedureVersion());
            }
        };
    }
}
