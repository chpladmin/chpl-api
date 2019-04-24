package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Confirms that the certification date of the listing is not in the future.
 * @author kekey
 *
 */
@Component("pendingCertificationDateReviewer")
public class CertificationDateReviewer implements Reviewer {
    @Autowired private ErrorMessageUtil msgUtil;

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (listing.getCertificationDate().getTime() > new Date().getTime()) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.futureCertificationDate"));
        }
    }
}
