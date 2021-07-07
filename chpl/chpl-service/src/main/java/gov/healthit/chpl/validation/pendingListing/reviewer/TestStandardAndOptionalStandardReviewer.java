package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("pendingTestStandardAndOptionalStandardReviewer")
public class TestStandardAndOptionalStandardReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestStandardAndOptionalStandardReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        listing.getCertificationCriterion().stream()
            .filter(cert -> (cert.getMeetsCriteria() != null && cert.getMeetsCriteria().equals(Boolean.TRUE)))
            .filter(cert -> (cert.getTestStandards() != null && cert.getTestStandards().size() > 0))
            .filter(cert -> (cert.getOptionalStandards() != null && cert.getOptionalStandards().size() > 0))
            .forEach(certResult -> addErrors(listing, certResult));
    }

    private void addErrors(PendingCertifiedProductDTO listing, PendingCertificationResultDTO certResult) {
        listing.getErrorMessages().add(
                msgUtil.getMessage("listing.criteria.hasBothTestAndOptionalStandards",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
    }
}
