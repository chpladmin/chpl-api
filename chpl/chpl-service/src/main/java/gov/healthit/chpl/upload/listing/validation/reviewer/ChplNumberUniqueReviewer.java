package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.CertifiedProductUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("chplNumberUniqueReviewer")
public class ChplNumberUniqueReviewer implements Reviewer {
    private CertifiedProductUtil certifiedProductUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplNumberUniqueReviewer(CertifiedProductUtil certifiedProductUtil,
            ErrorMessageUtil msgUtil) {
        this.certifiedProductUtil = certifiedProductUtil;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        String chplProductNumber = listing.getChplProductNumber();
        if (StringUtils.isEmpty(chplProductNumber)) {
            return;
        }

        CertifiedProduct listingWithThisChplProductNumber = certifiedProductUtil.getListing(chplProductNumber);
        if (listingWithThisChplProductNumber != null && listingWithThisChplProductNumber.getId() != listing.getId()) {
            listing.addDataErrorMessage(msgUtil.getMessage(
                    "listing.chplProductNumber.notUnique", chplProductNumber));
        }
    }
}
