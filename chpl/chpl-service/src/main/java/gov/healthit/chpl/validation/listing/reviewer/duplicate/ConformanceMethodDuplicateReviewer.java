package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("conformanceMethodDuplicateReviewer")
public class ConformanceMethodDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public ConformanceMethodDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultConformanceMethod> conformanceMethodDuplicateResults =
                new DuplicateReviewResult<CertificationResultConformanceMethod>(duplicatePredicate());
        if (certificationResult.getConformanceMethods() != null) {
            for (CertificationResultConformanceMethod cm : certificationResult.getConformanceMethods()) {
                conformanceMethodDuplicateResults.addObject(cm);
            }
        }
        if (conformanceMethodDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(conformanceMethodDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setConformanceMethods(conformanceMethodDuplicateResults.getUniqueList());
        }

        DuplicateReviewResult<CertificationResultConformanceMethod> conformanceMethodDuplicateIdResults =
                new DuplicateReviewResult<CertificationResultConformanceMethod>(duplicateIdPredicate());
        if (certificationResult.getConformanceMethods() != null) {
            for (CertificationResultConformanceMethod cm : certificationResult.getConformanceMethods()) {
                conformanceMethodDuplicateIdResults.addObject(cm);
            }
        }
        if (conformanceMethodDuplicateIdResults.duplicatesExist()) {
            listing.getErrorMessages().addAll(
                    getErrors(conformanceMethodDuplicateIdResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
        }
    }

    private List<String> getErrors(List<CertificationResultConformanceMethod> duplicates,
            String criteria) {
        List<String> errors = new ArrayList<String>();
        for (CertificationResultConformanceMethod duplicate : duplicates) {
            String error = errorMessageUtil.getMessage("listing.criteria.duplicateConformanceMethodName",
                        criteria, duplicate.getConformanceMethod().getName());
            errors.add(error);
        }
        return errors;
    }

    private List<String> getWarnings(List<CertificationResultConformanceMethod> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultConformanceMethod duplicate : duplicates) {
            String warning = "";
            if (StringUtils.isEmpty(duplicate.getConformanceMethodVersion())) {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateConformanceMethodNameAndVersion",
                        criteria, duplicate.getConformanceMethod().getName(), "");
            } else {
                warning = errorMessageUtil.getMessage("listing.criteria.duplicateConformanceMethodNameAndVersion",
                    criteria, duplicate.getConformanceMethod().getName(), duplicate.getConformanceMethodVersion());
            }
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultConformanceMethod, CertificationResultConformanceMethod> duplicatePredicate() {
        return new BiPredicate<CertificationResultConformanceMethod, CertificationResultConformanceMethod>() {
            @Override
            public boolean test(CertificationResultConformanceMethod cm1,
                    CertificationResultConformanceMethod cm2) {
                return ObjectUtils.allNotNull(cm1.getConformanceMethod(), cm1.getConformanceMethod().getId(),
                        cm2.getConformanceMethod(), cm2.getConformanceMethod().getId())
                        && Objects.equals(cm1.getConformanceMethod().getId(), cm2.getConformanceMethod().getId())
                        && Objects.equals(cm1.getConformanceMethodVersion(), cm2.getConformanceMethodVersion());
            }
        };
    }

    private BiPredicate<CertificationResultConformanceMethod, CertificationResultConformanceMethod> duplicateIdPredicate() {
        return new BiPredicate<CertificationResultConformanceMethod, CertificationResultConformanceMethod>() {
            @Override
            public boolean test(CertificationResultConformanceMethod cm1,
                    CertificationResultConformanceMethod cm2) {
                return ObjectUtils.allNotNull(cm1.getConformanceMethod(), cm1.getConformanceMethod().getId(),
                        cm2.getConformanceMethod(), cm2.getConformanceMethod().getId())
                        && Objects.equals(cm1.getConformanceMethod().getId(), cm2.getConformanceMethod().getId())
                        && !Objects.equals(cm1.getConformanceMethodVersion(), cm2.getConformanceMethodVersion());
            }
        };
    }
}
