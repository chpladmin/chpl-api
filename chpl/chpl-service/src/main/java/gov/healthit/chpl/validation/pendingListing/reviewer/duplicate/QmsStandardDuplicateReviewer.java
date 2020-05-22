package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.DuplicateReviewResult;

@Component("pendingQmsStandardDuplicateReviewer")
public class QmsStandardDuplicateReviewer {
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public QmsStandardDuplicateReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(PendingCertifiedProductDTO listing) {

        DuplicateReviewResult<PendingCertifiedProductQmsStandardDTO> qmsStandardDuplicateResults =
                new DuplicateReviewResult<PendingCertifiedProductQmsStandardDTO>(getPredicate());

        if (listing.getQmsStandards() != null) {
            for (PendingCertifiedProductQmsStandardDTO dto : listing.getQmsStandards()) {
                qmsStandardDuplicateResults.addObject(dto);
            }
        }

        if (qmsStandardDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(qmsStandardDuplicateResults.getDuplicateList()));
            listing.setQmsStandards(qmsStandardDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(List<PendingCertifiedProductQmsStandardDTO> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (PendingCertifiedProductQmsStandardDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateQmsStandard",
                    duplicate.getName(),
                    duplicate.getApplicableCriteria() == null ? "" : duplicate.getApplicableCriteria(),
                    duplicate.getModification() == null ? "" : duplicate.getModification());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<PendingCertifiedProductQmsStandardDTO, PendingCertifiedProductQmsStandardDTO> getPredicate() {
        return new BiPredicate<PendingCertifiedProductQmsStandardDTO, PendingCertifiedProductQmsStandardDTO>() {
            @Override
            public boolean test(PendingCertifiedProductQmsStandardDTO dto1,
                    PendingCertifiedProductQmsStandardDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getName(), dto2.getName())
                        && Objects.equals(dto1.getName(), dto2.getName())
                        && Objects.equals(dto1.getApplicableCriteria(), dto2.getApplicableCriteria())
                        && Objects.equals(dto1.getModification(), dto2.getModification());
            }
        };
    }

}
