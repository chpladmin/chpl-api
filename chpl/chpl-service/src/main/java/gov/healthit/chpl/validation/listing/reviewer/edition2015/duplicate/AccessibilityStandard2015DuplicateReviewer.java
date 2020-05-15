package gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("accessibilityStandard2015DuplicateReviewer")
public class AccessibilityStandard2015DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AccessibilityStandard2015DuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {

        DuplicateReviewResult<CertifiedProductAccessibilityStandard> accessibilityStandardDuplicateResults =
                new DuplicateReviewResult<CertifiedProductAccessibilityStandard>(getPredicate());


        if (listing.getAccessibilityStandards() != null) {
            for (CertifiedProductAccessibilityStandard dto : listing.getAccessibilityStandards()) {
                accessibilityStandardDuplicateResults.addObject(dto);
            }
        }

        if (accessibilityStandardDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(accessibilityStandardDuplicateResults.getDuplicateList()));
            listing.setAccessibilityStandards(accessibilityStandardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<CertifiedProductAccessibilityStandard> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (CertifiedProductAccessibilityStandard duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateAccessibilityStandard.2015",
                    duplicate.getAccessibilityStandardName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<
    CertifiedProductAccessibilityStandard, CertifiedProductAccessibilityStandard> getPredicate() {
        return new BiPredicate<
                CertifiedProductAccessibilityStandard, CertifiedProductAccessibilityStandard>() {
            @Override
            public boolean test(CertifiedProductAccessibilityStandard dto1,
                    CertifiedProductAccessibilityStandard dto2) {
                return ObjectUtils.allNotNull(dto1.getAccessibilityStandardName(), dto2.getAccessibilityStandardName())
                        && dto1.getAccessibilityStandardName().equals(dto2.getAccessibilityStandardName());
            }
        };
    }
}
