package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("testProcedureDuplicateReviewer")
public class TestProcedureDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TestProcedureDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
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

        DuplicateReviewResult<CertificationResultTestProcedure> testProcedureDuplicateIdResults =
                new DuplicateReviewResult<CertificationResultTestProcedure>(duplicateIdPredicate());
        if (certificationResult.getTestProcedures() != null) {
            for (CertificationResultTestProcedure dto : certificationResult.getTestProcedures()) {
                testProcedureDuplicateIdResults.addObject(dto);
            }
        }
        if (testProcedureDuplicateIdResults.duplicatesExist()) {
            listing.getErrorMessages().addAll(
                    getErrors(testProcedureDuplicateIdResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private List<String> getErrors(List<CertificationResultTestProcedure> duplicates,
            String criteria) {
        List<String> errors = new ArrayList<String>();
        for (CertificationResultTestProcedure duplicate : duplicates) {
            String error = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedureName",
                        criteria, duplicate.getTestProcedure().getName());
            errors.add(error);
        }
        return errors;
    }

    private List<String> getWarnings(List<CertificationResultTestProcedure> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultTestProcedure duplicate : duplicates) {
            String warning = "";
            if (StringUtils.isEmpty(duplicate.getTestProcedureVersion())) {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedureNameAndVersion",
                        criteria, duplicate.getTestProcedure().getName(), "");
            } else {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedureNameAndVersion",
                    criteria, duplicate.getTestProcedure().getName(), duplicate.getTestProcedureVersion());
            }
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure> duplicatePredicate() {
        return new BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure>() {
            @Override
            public boolean test(CertificationResultTestProcedure dto1,
                    CertificationResultTestProcedure dto2) {
                return Objects.equals(dto1.getTestProcedure().getId(), dto2.getTestProcedure().getId())
                        && Objects.equals(dto1.getTestProcedureVersion(), dto2.getTestProcedureVersion());
            }
        };
    }

    private BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure> duplicateIdPredicate() {
        return new BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure>() {
            @Override
            public boolean test(CertificationResultTestProcedure dto1,
                    CertificationResultTestProcedure dto2) {
                return Objects.equals(dto1.getTestProcedure().getId(), dto2.getTestProcedure().getId())
                        && !Objects.equals(dto1.getTestProcedureVersion(), dto2.getTestProcedureVersion());
            }
        };
    }
}
