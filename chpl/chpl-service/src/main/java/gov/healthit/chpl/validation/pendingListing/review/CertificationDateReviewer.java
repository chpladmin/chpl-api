package gov.healthit.chpl.validation.pendingListing.review;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CertificationDateReviewer implements Reviewer {
    @Autowired private ErrorMessageUtil msgUtil;
    
    @Override
    public void review(PendingCertifiedProductDTO listing) {
        if (listing.getCertificationDate().getTime() > new Date().getTime()) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.futureCertificationDate"));
        }
    }
}
