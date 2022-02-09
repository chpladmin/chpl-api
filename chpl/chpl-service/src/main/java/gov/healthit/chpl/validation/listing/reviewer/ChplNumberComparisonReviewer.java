package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.CertifiedProductUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("chplNumberComparisonReviewer")
public class ChplNumberComparisonReviewer implements ComparisonReviewer {
    private CertifiedProductUtil certifiedProductUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplNumberComparisonReviewer(CertifiedProductUtil certifiedProductUtil,
            ErrorMessageUtil msgUtil) {
        this.certifiedProductUtil = certifiedProductUtil;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        if (!existingListing.getChplProductNumber().equals(updatedListing.getChplProductNumber())) {
            try {
                boolean isDup = certifiedProductUtil.chplIdExists(updatedListing.getChplProductNumber());
                if (isDup) {
                    updatedListing.getErrorMessages()
                            .add(msgUtil.getMessage("listing.chplProductNumber.changedNotUnique",
                                    updatedListing.getChplProductNumber()));
                }
            } catch (final EntityRetrievalException ex) {
            }
        }
    }
}
