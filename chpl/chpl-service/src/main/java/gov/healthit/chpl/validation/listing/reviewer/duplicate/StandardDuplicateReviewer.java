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
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.standard.CertificationResultStandard;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component(value = "standardDuplicateReviewer")
public class StandardDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public StandardDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultStandard> standardDuplicateResults =
                new DuplicateReviewResult<CertificationResultStandard>(getPredicate());

        if (certificationResult.getStandards() != null) {
            for (CertificationResultStandard crs : certificationResult.getStandards()) {
                standardDuplicateResults.addObject(crs);
            }
        }

        if (standardDuplicateResults.duplicatesExist()) {
            listing.addAllWarningMessages(
                    getWarnings(
                            standardDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion()))
                    .stream()
                    .collect(Collectors.toSet()));
            certificationResult.setStandards(standardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultStandard> duplicates,
            String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultStandard duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateStandard",
                    criteria, duplicate.getStandard().getRegulatoryTextCitation());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultStandard, CertificationResultStandard> getPredicate() {
        return new BiPredicate<CertificationResultStandard, CertificationResultStandard>() {
            @Override
            public boolean test(CertificationResultStandard s1, CertificationResultStandard s2) {
                return (ObjectUtils.allNotNull(s1.getStandard().getId(), s2.getStandard().getId())
                        && Objects.equals(s1.getStandard().getId(), s2.getStandard().getId()))
                    || Objects.equals(s1.getStandard().getRegulatoryTextCitation(), s2.getStandard().getRegulatoryTextCitation());
            }
        };
    }

}
