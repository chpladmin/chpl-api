package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * If the CHPL number has changed (by the user or by the system)
 * make sure it is still unique.
 * @author kekey
 *
 */
@Component("chplNumberComparisonReviewer")
public class ChplNumberComparisonReviewer implements ComparisonReviewer {
    private CertifiedProductManager cpManager;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplNumberComparisonReviewer(final CertifiedProductManager cpManager,
            final ErrorMessageUtil msgUtil) {
        this.cpManager = cpManager;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(final CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        if (!existingListing.getChplProductNumber().equals(updatedListing.getChplProductNumber())) {
            try {
                boolean isDup = cpManager.chplIdExists(updatedListing.getChplProductNumber());
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
