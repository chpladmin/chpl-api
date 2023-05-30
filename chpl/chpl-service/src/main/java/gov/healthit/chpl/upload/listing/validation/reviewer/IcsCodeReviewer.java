package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("icsCodeReviewer")
@Log4j2
public class IcsCodeReviewer implements Reviewer {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public IcsCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
            ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
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

        Integer icsCodeInteger = null;
        try {
            icsCodeInteger = chplProductNumberUtil.getIcsCode(chplProductNumber);
        } catch (Exception ex) {
            LOGGER.warn("Cannot find ICS code in " + chplProductNumber);
        }

        if (icsCodeInteger != null && icsCodeInteger.intValue() == 0) {
            if (listing.getIcs() != null && listing.getIcs().getInherits() != null
                    && listing.getIcs().getInherits().equals(Boolean.TRUE)) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.icsCodeFalseValueTrue"));
            }
        } else if ((listing.getIcs() == null || (listing.getIcs().getInherits() == null
                || listing.getIcs().getInherits().equals(Boolean.FALSE))) && icsCodeInteger != null
                && icsCodeInteger.intValue() > 0) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.icsCodeTrueValueFalse"));
        }
    }
}
