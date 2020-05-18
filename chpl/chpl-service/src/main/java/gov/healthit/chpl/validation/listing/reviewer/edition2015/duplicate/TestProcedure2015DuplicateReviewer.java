package gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate;

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
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("testProcedure2015DuplicateReviewer")
public class TestProcedure2015DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestProcedure2015DuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultTestProcedure> testProcedureDuplicateResults =
                new DuplicateReviewResult<CertificationResultTestProcedure>(duplicatePredicate());
        if (certificationResult.getTestProcedures() != null) {
            for (CertificationResultTestProcedure dto : certificationResult.getTestProcedures()) {
                testProcedureDuplicateResults.addObject(dto);
            }
        }
        if (testProcedureDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(testProcedureDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setTestProcedures(testProcedureDuplicateResults.getUniqueList());
        }

        DuplicateReviewResult<CertificationResultTestProcedure> testProcedureDuplicateNameResults =
                new DuplicateReviewResult<CertificationResultTestProcedure>(duplicateNamePredicate());
        if (certificationResult.getTestProcedures() != null) {
            for (CertificationResultTestProcedure dto : certificationResult.getTestProcedures()) {
                testProcedureDuplicateNameResults.addObject(dto);
            }
        }
        if (testProcedureDuplicateNameResults.duplicatesExist()) {
            listing.getErrorMessages().addAll(
                    getErrors(testProcedureDuplicateNameResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private List<String> getErrors(List<CertificationResultTestProcedure> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestProcedure duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedureName.2015",
                    criteria, duplicate.getTestProcedure().getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private List<String> getWarnings(List<CertificationResultTestProcedure> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestProcedure duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedure.2015",
                    criteria, duplicate.getTestProcedure().getName(), duplicate.getTestProcedureVersion());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure> duplicatePredicate() {
        return new BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure>() {
            @Override
            public boolean test(CertificationResultTestProcedure dto1,
                    CertificationResultTestProcedure dto2) {
                return ObjectUtils.allNotNull(dto1.getTestProcedure().getName(), dto2.getTestProcedure().getName(),
                        dto1.getTestProcedureVersion(), dto2.getTestProcedureVersion())
                        && dto1.getTestProcedure().getName().equals(dto2.getTestProcedure().getName())
                        && dto1.getTestProcedureVersion().equals(dto2.getTestProcedureVersion());
            }
        };
    }

    private BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure> duplicateNamePredicate() {
        return new BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure>() {
            @Override
            public boolean test(CertificationResultTestProcedure dto1,
                    CertificationResultTestProcedure dto2) {
                return ObjectUtils.allNotNull(dto1.getTestProcedure().getName(), dto2.getTestProcedure().getName())
                        && dto1.getTestProcedure().getName().equals(dto2.getTestProcedure().getName());
            }
        };
    }
}
