package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
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

        DuplicateReviewResult<CertificationResultTestProcedure> testProcedureDuplicateResults = new DuplicateReviewResult<CertificationResultTestProcedure>(duplicatePredicate());
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

        DuplicateReviewResult<CertificationResultTestProcedure> testProcedureDuplicateIdResults = new DuplicateReviewResult<CertificationResultTestProcedure>(duplicateIdPredicate());
        if (certificationResult.getTestProcedures() != null) {
            for (CertificationResultTestProcedure dto : certificationResult.getTestProcedures()) {
                testProcedureDuplicateIdResults.addObject(dto);
            }
        }
        if (testProcedureDuplicateIdResults.duplicatesExist()) {
            listing.addAllBusinessErrorMessages(
                    getErrors(testProcedureDuplicateIdResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private Set<String> getErrors(List<CertificationResultTestProcedure> duplicates,
            String criteria) {
        Set<String> errors = new HashSet<String>();
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
            public boolean test(CertificationResultTestProcedure tp1,
                    CertificationResultTestProcedure tp2) {
                return ObjectUtils.allNotNull(tp1.getTestProcedure(), tp1.getTestProcedure().getId(),
                        tp2.getTestProcedure(), tp2.getTestProcedure().getId())
                        && Objects.equals(tp1.getTestProcedure().getId(), tp2.getTestProcedure().getId())
                        && Objects.equals(tp1.getTestProcedureVersion(), tp2.getTestProcedureVersion());
            }
        };
    }

    private BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure> duplicateIdPredicate() {
        return new BiPredicate<CertificationResultTestProcedure, CertificationResultTestProcedure>() {
            @Override
            public boolean test(CertificationResultTestProcedure tp1,
                    CertificationResultTestProcedure tp2) {
                return ObjectUtils.allNotNull(tp1.getTestProcedure(), tp1.getTestProcedure().getId(),
                        tp2.getTestProcedure(), tp2.getTestProcedure().getId())
                        && Objects.equals(tp1.getTestProcedure().getId(), tp2.getTestProcedure().getId())
                        && !Objects.equals(tp1.getTestProcedureVersion(), tp2.getTestProcedureVersion());
            }
        };
    }
}
