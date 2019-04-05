package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.DuplicateReviewResult;

@Component("icsSource2015DuplicateReviewer")
public class IcsSource2015DuplicateReviewer {

    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public IcsSource2015DuplicateReviewer(final ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public void review(final PendingCertifiedProductDTO listing) {

        DuplicateReviewResult<CertifiedProductDetailsDTO> icsSourceDuplicateResults =
                new DuplicateReviewResult<CertifiedProductDetailsDTO>(getPredicate());

        if (listing.getIcsParents() != null) {
            for (CertifiedProductDetailsDTO dto : listing.getIcsParents()) {
                icsSourceDuplicateResults.addObject(dto);
            }
        }

        if (icsSourceDuplicateResults.duplicatesExist()) {
            listing.getWarningMessages().addAll(getWarnings(icsSourceDuplicateResults.getDuplicateList()));
            listing.setIcsParents(icsSourceDuplicateResults.getUniqueList());
        }
    }

    private List<String> getWarnings(final List<CertifiedProductDetailsDTO> duplicates) {
        List<String> warnings = new ArrayList<String>();
        for (CertifiedProductDetailsDTO duplicate : duplicates) {
            String warning = errorMessageUtil.getMessage("listing.duplicateIcsSource.2015",
                    duplicate.getChplProductNumber());
            warnings.add(warning);
        }
        return warnings;
    }

    private BiPredicate<CertifiedProductDetailsDTO, CertifiedProductDetailsDTO> getPredicate() {
        return new BiPredicate<CertifiedProductDetailsDTO, CertifiedProductDetailsDTO>() {
            @Override
            public boolean test(final CertifiedProductDetailsDTO dto1,
                    final CertifiedProductDetailsDTO dto2) {
                return ObjectUtils.allNotNull(dto1.getChplProductNumber(), dto2.getChplProductNumber())
                        && dto1.getChplProductNumber().equals(dto2.getChplProductNumber());
            }
        };
    }
}
