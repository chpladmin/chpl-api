package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.DuplicateReviewResult;

@Component("accessibilityStandard2015DuplicateReviewer")
public class AccessibilityStandard2015DuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AccessibilityStandard2015DuplicateReviewer(final ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(final PendingCertifiedProductDTO listing) {

        DuplicateReviewResult<PendingCertifiedProductAccessibilityStandardDTO> accessibilityStandardDuplicateResults =
                new DuplicateReviewResult<PendingCertifiedProductAccessibilityStandardDTO>(getPredicate());


        if (listing.getAccessibilityStandards() != null) {
            for (PendingCertifiedProductAccessibilityStandardDTO dto : listing.getAccessibilityStandards()) {
                accessibilityStandardDuplicateResults.addObject(dto);
            }
        }

        if (accessibilityStandardDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(accessibilityStandardDuplicateResults.getDuplicateList()));
            listing.setAccessibilityStandards(accessibilityStandardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(final List<PendingCertifiedProductAccessibilityStandardDTO> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertifiedProductAccessibilityStandardDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateAccessibilityStandard.2015", duplicate.getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<
    PendingCertifiedProductAccessibilityStandardDTO, PendingCertifiedProductAccessibilityStandardDTO> getPredicate() {
        return new BiPredicate<
                PendingCertifiedProductAccessibilityStandardDTO, PendingCertifiedProductAccessibilityStandardDTO>() {
            @Override
            public boolean test(final PendingCertifiedProductAccessibilityStandardDTO dto1,
                    final PendingCertifiedProductAccessibilityStandardDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getName(), dto2.getName())
                        && dto1.getName().equals(dto2.getName());
            }
        };
    }
}
