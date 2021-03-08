package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadCertificationDateReviewer")
@Log4j2
public class CertifiedDateCodeReviewer  {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertifiedDateCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
            ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        String chplProductNumber = listing.getChplProductNumber();
        if (StringUtils.isEmpty(chplProductNumber)
                || chplProductNumberUtil.isLegacyChplProductNumberStyle(chplProductNumber)
                || !chplProductNumberUtil.isCurrentChplProductNumberStyle(chplProductNumber)) {
            return;
        }

        if (listing.getCertificationDate() != null) {
            SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
            idDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String listingCertificationDate = idDateFormat.format(listing.getCertificationDate());
            String certifiedDateCode = null;
            try {
                certifiedDateCode = chplProductNumberUtil.getCertificationDateCode(chplProductNumber);
            } catch (Exception ex) {
                LOGGER.warn("Cannot find certified date code in " + chplProductNumber);
            }

            if (StringUtils.isNoneEmpty(listingCertificationDate, certifiedDateCode)
                    && !listingCertificationDate.equals(certifiedDateCode)) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.certificationDateMismatch", certifiedDateCode, listingCertificationDate));
            }
        }
    }
}
