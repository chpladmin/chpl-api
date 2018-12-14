package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("accessibilityStandardDuplicateReviewer")
public class AccessibilityStandardDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AccessibilityStandardDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing) {

        DuplicateReviewResult<PendingCertifiedProductAccessibilityStandardDTO> accessibilityStandardDuplicateResults =
                new DuplicateReviewResult<PendingCertifiedProductAccessibilityStandardDTO>(getPredicate());


        if (listing.getAccessibilityStandards() != null) {
            for (PendingCertifiedProductAccessibilityStandardDTO dto : listing.getAccessibilityStandards()) {
                accessibilityStandardDuplicateResults.addObject(dto);
            }
        }

        if (accessibilityStandardDuplicateResults.getDuplicateList().size() > 0) {
            listing.getWarningMessages().addAll(getWarnings(accessibilityStandardDuplicateResults.getDuplicateList()));
            listing.setAccessibilityStandards(accessibilityStandardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertifiedProductAccessibilityStandardDTO> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertifiedProductAccessibilityStandardDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateAccessibilityStandard.2015", duplicate.getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertifiedProductAccessibilityStandardDTO, PendingCertifiedProductAccessibilityStandardDTO> getPredicate() {
        return new BiPredicate<PendingCertifiedProductAccessibilityStandardDTO, PendingCertifiedProductAccessibilityStandardDTO>() {
            @Override
            public boolean test(PendingCertifiedProductAccessibilityStandardDTO dto1, PendingCertifiedProductAccessibilityStandardDTO dto2) {
                if (dto1.getName() != null && dto2.getName() != null) {
                    return dto1.getName().equals(dto2.getName());
                } else {
                    return false;
                }
            }
        };
    }
}
