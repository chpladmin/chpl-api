package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingAtlDuplicateReviewer")
public class AtlDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public AtlDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing) {

        DuplicateReviewResult<PendingCertifiedProductTestingLabDTO> atlDuplicateResults =
                new DuplicateReviewResult<PendingCertifiedProductTestingLabDTO>(getPredicate());

        if (listing.getTestingLabs() != null) {
            for (PendingCertifiedProductTestingLabDTO dto : listing.getTestingLabs()) {
                atlDuplicateResults.addObject(dto);
            }
        }

        if (atlDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(atlDuplicateResults.getDuplicateList()));
            listing.setTestingLabs(atlDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertifiedProductTestingLabDTO> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertifiedProductTestingLabDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateTestingLab", duplicate.getTestingLabName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertifiedProductTestingLabDTO, PendingCertifiedProductTestingLabDTO> getPredicate() {
        return new BiPredicate<PendingCertifiedProductTestingLabDTO, PendingCertifiedProductTestingLabDTO>() {
            @Override
            public boolean test(PendingCertifiedProductTestingLabDTO dto1,
                    PendingCertifiedProductTestingLabDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getTestingLabName(), dto2.getTestingLabName())
                        && dto1.getTestingLabName().equals(dto2.getTestingLabName());
            }
        };
    }

}
