package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingTargetedUserDuplicateReviewer")
public class TargetedUserDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public TargetedUserDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing) {
        DuplicateReviewResult<PendingCertifiedProductTargetedUserDTO> targetedUserDuplicateResults =
                new DuplicateReviewResult<PendingCertifiedProductTargetedUserDTO>(getPredicate());

        if (listing.getTargetedUsers() != null) {
            for (PendingCertifiedProductTargetedUserDTO dto : listing.getTargetedUsers()) {
                targetedUserDuplicateResults.addObject(dto);
            }
        }
        if (targetedUserDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(
                    getWarnings(targetedUserDuplicateResults.getDuplicateList()));
            listing.setTargetedUsers(targetedUserDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertifiedProductTargetedUserDTO> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertifiedProductTargetedUserDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateTargetedUser", duplicate.getName());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertifiedProductTargetedUserDTO, PendingCertifiedProductTargetedUserDTO> getPredicate() {
        return new BiPredicate<PendingCertifiedProductTargetedUserDTO, PendingCertifiedProductTargetedUserDTO>() {
            @Override
            public boolean test(PendingCertifiedProductTargetedUserDTO dto1,
                    PendingCertifiedProductTargetedUserDTO dto2) {
                return (ObjectUtils.allNotNull(dto1.getTargetedUserId(), dto2.getTargetedUserId())
                        && dto1.getTargetedUserId().equals(dto2.getTargetedUserId()))
                        || (dto1.getTargetedUserId() == null && dto2.getTargetedUserId() == null
                        && ObjectUtils.allNotNull(dto1.getName(), dto2.getName())
                        && dto1.getName().equals(dto2.getName()));
            }
        };
    }
}
