package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("productReviewer")
public class ProductReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ProductReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getProduct() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingProduct"));
        }
    }
}
