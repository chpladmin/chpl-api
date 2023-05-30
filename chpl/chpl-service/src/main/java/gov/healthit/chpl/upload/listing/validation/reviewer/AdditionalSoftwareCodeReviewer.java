package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("additionalSoftwareCodeReviewer")
@Log4j2
public class AdditionalSoftwareCodeReviewer implements Reviewer {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AdditionalSoftwareCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
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

        String additionalSoftwareCode = null;
        try {
            additionalSoftwareCode = chplProductNumberUtil.getAdditionalSoftwareCode(chplProductNumber);
        } catch (Exception ex) {
            LOGGER.warn("Cannot find additional software code in " + chplProductNumber);
        }

        boolean certsHaveAdditionalSoftware = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getAdditionalSoftware() != null && certResult.getAdditionalSoftware().size() > 0)
                .findAny().isPresent();
        if (additionalSoftwareCode != null && additionalSoftwareCode.equals("0")) {
            if (certsHaveAdditionalSoftware) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.additionalSoftwareCode0Mismatch"));
            }
        } else if (additionalSoftwareCode != null && additionalSoftwareCode.equals("1")) {
            if (!certsHaveAdditionalSoftware) {
                listing.addDataErrorMessage(msgUtil.getMessage("listing.additionalSoftwareCode1Mismatch"));
            }
        }
    }
}
