package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.codeset.CertificationResultCodeSet;
import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("conformanceDuplicateReviewer")
public class CodeSetDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public CodeSetDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultCodeSet> codeSetDuplicateResults = new DuplicateReviewResult<CertificationResultCodeSet>(duplicatePredicate());
        if (certificationResult.getCodeSets() != null) {
            for (CertificationResultCodeSet cs : certificationResult.getCodeSets()) {
                codeSetDuplicateResults.addObject(cs);
            }
        }
        if (codeSetDuplicateResults.duplicatesExist()) {
            listing.addAllWarningMessages(
                    getWarnings(codeSetDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())).stream()
                    .collect(Collectors.toSet()));
            certificationResult.setCodeSets(codeSetDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultCodeSet> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultCodeSet duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateCodeSetName",
                        criteria, duplicate.getCodeSet().getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultCodeSet, CertificationResultCodeSet> duplicatePredicate() {
        return new BiPredicate<CertificationResultCodeSet, CertificationResultCodeSet>() {
            @Override
            public boolean test(CertificationResultCodeSet cs1,
                    CertificationResultCodeSet cs2) {
                return ObjectUtils.allNotNull(cs1.getCodeSet(), cs1.getCodeSet().getId(),
                        cs2.getCodeSet(), cs2.getCodeSet().getId())
                        && Objects.equals(cs1.getCodeSet().getId(), cs2.getCodeSet().getId());
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
