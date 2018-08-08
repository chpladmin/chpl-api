package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CertificationDateReviewer implements Reviewer {
    @Autowired private ErrorMessageUtil msgUtil;
    
    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationDate() > new Date().getTime()) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.futureCertificationDate"));
        }
    }
}
