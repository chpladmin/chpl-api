package gov.healthit.chpl.validation.listing.reviewer;

import java.util.Date;

import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("certificationDateReviewer")
public class CertificationDateReviewer implements Reviewer {
    private ListingUploadHandlerUtil uploadHandlerUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertificationDateReviewer(ListingUploadHandlerUtil uploadHandlerUtil, ErrorMessageUtil msgUtil) {
        this.uploadHandlerUtil = uploadHandlerUtil;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (isCertificationDateMissing(listing)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.certificationDateMissing"));
        } else if (isSuppliedCertificationDateFormatInvalid(listing)) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.badCertificationDate", listing.getCertificationDateStr()));
        }

        if (listing.getCertificationDate() != null && listing.getCertificationDate() > System.currentTimeMillis()) {
            listing.addBusinessErrorMessage(msgUtil.getMessage("listing.futureCertificationDate"));
        }
    }

    private boolean isCertificationDateMissing(CertifiedProductSearchDetails listing) {
        return listing.getCertificationDate() == null && StringUtils.isEmpty(listing.getCertificationDateStr());
    }

    private boolean isSuppliedCertificationDateFormatInvalid(CertifiedProductSearchDetails listing) {
        return !StringUtils.isEmpty(listing.getCertificationDateStr())
                && (listing.getCertificationDate() == null || !isValidDate(listing.getCertificationDateStr()));
    }

    private boolean isValidDate(String dateStr) {
        Date parsedDate = null;
        try {
            parsedDate = uploadHandlerUtil.parseDate(dateStr);
        } catch (ValidationException ex) {
            parsedDate = null;
        }
        return parsedDate != null;
    }
}
