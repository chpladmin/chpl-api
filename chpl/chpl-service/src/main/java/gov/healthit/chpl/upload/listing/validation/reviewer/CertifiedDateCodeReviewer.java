package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("certifiedDateCodeReviewer")
@Log4j2
public class CertifiedDateCodeReviewer implements Reviewer {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertifiedDateCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    @Override
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

            if (isValidCertifiedDateCode(listing.getChplProductNumber()) && !StringUtils.isEmpty(listingCertificationDate)
                    && !listingCertificationDate.equals(certifiedDateCode)) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.certificationDateMismatch", certifiedDateCode, listingCertificationDate));
            }
        }
    }

    private boolean isValidCertifiedDateCode(String chplProductNumber) {
        return validationUtils.chplNumberPartIsValid(chplProductNumber,
                ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX,
                ChplProductNumberUtil.CERTIFIED_DATE_CODE_REGEX);
    }
}
