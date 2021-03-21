package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("certificationBodyCodeReviewer")
@Log4j2
public class CertificationBodyCodeReviewer implements Reviewer {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CertificationBodyCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
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

        String acbCode = null;
        try {
            acbCode = chplProductNumberUtil.getAcbCode(chplProductNumber);
        } catch (Exception ex) {
            LOGGER.warn("Cannot find ACB code in " + chplProductNumber);
        }

        Map<String, Object> listingAcbMap = listing.getCertifyingBody();
        if (listingAcbMap != null) {
            Object listingAcbCodeValue = listingAcbMap.get(CertifiedProductSearchDetails.ACB_CODE_KEY);
            if (listingAcbCodeValue != null) {
                String listingAcbCode = listingAcbCodeValue.toString();
                if (StringUtils.isNoneEmpty(acbCode, listingAcbCode)
                        && !acbCode.equals(listingAcbCode)) {
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.certificationBodyMismatch", acbCode, listingAcbCode));
                }
            }
        }
    }
}
