package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("chplNumberUniqueReviewer")
public class ChplNumberUniqueReviewer {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplNumberUniqueReviewer(ChplProductNumberUtil chplProductNumberUtil,
            ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        String chplProductNumber = listing.getChplProductNumber();
        if (StringUtils.isEmpty(chplProductNumber)) {
            return;
        }

        CertifiedProduct listingWithThisChplProductNumber = chplProductNumberUtil.getListing(chplProductNumber);
        if (listingWithThisChplProductNumber != null && listingWithThisChplProductNumber.getId() != listing.getId()) {
            listing.getErrorMessages().add(msgUtil.getMessage(
                    "listing.chplProductNumber.notUnique", chplProductNumber));
        }
    }
}
