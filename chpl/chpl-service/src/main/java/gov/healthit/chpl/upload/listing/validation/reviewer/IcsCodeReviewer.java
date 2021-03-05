package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadIcsReviewer")
@Log4j2
public class IcsCodeReviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public IcsCodeReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        String chplProductNumber = listing.getChplProductNumber();
        if (StringUtils.isEmpty(chplProductNumber)) {
            return;
        }

        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            return;
        }

        Integer icsCodeInteger = null;
        try {
            icsCodeInteger = Integer.valueOf(uniqueIdParts[ChplProductNumberUtil.ICS_CODE_INDEX]);
        } catch (NumberFormatException ex) {
            LOGGER.catching(ex);
        }

        if (icsCodeInteger != null && icsCodeInteger.intValue() == 0) {
            if (listing.getIcs() != null && listing.getIcs().getParents() != null
                    && listing.getIcs().getParents().size() > 0) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.ics00"));
            }

            if (listing.getIcs() != null && listing.getIcs().getInherits() != null
                    && listing.getIcs().getInherits().equals(Boolean.TRUE)) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.icsCodeFalseValueTrue"));
            }
        } else if (listing.getIcs() == null || listing.getIcs().getInherits() == null
                || listing.getIcs().getInherits().equals(Boolean.FALSE) && icsCodeInteger != null
                && icsCodeInteger.intValue() > 0) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.icsCodeTrueValueFalse"));
        }
    }
}
