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
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("optionalStandardDuplicateReviewer")
public class OptionalStandardDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public OptionalStandardDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing, CertificationResult certificationResult) {

        DuplicateReviewResult<CertificationResultOptionalStandard> optionalStandardDuplicateResults =
                new DuplicateReviewResult<CertificationResultOptionalStandard>(getPredicate());


        if (certificationResult.getOptionalStandards() != null) {
            for (CertificationResultOptionalStandard os : certificationResult.getOptionalStandards()) {
                optionalStandardDuplicateResults.addObject(os);
            }
        }

        if (optionalStandardDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(optionalStandardDuplicateResults.getDuplicateList(),
                            Util.formatCriteriaNumber(certificationResult.getCriterion())));
            certificationResult.setOptionalStandards(optionalStandardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertificationResultOptionalStandard> duplicates, String criteria) {
        List<String> warnings = new ArrayList<String>();
        for (CertificationResultOptionalStandard duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.criteria.duplicateOptionalStandard",
                    criteria, duplicate.getCitation());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertificationResultOptionalStandard, CertificationResultOptionalStandard> getPredicate() {
        return new BiPredicate<CertificationResultOptionalStandard, CertificationResultOptionalStandard>() {
            @Override
            public boolean test(CertificationResultOptionalStandard os1,
                    CertificationResultOptionalStandard os2) {
                return (ObjectUtils.allNotNull(os1.getOptionalStandardId(), os2.getOptionalStandardId())
                        && Objects.equals(os1.getOptionalStandardId(),  os2.getOptionalStandardId()))
                    || Objects.equals(os1.getCitation(), os2.getCitation());
            }
        };
    }
}
