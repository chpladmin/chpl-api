package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("developerCodeReviewer")
@Log4j2
public class DeveloperCodeReviewer implements Reviewer {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeveloperCodeReviewer(ChplProductNumberUtil chplProductNumberUtil,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        String chplProductNumber = listing.getChplProductNumber();
        if (StringUtils.isEmpty(chplProductNumber)
                || chplProductNumberUtil.isLegacyChplProductNumberStyle(chplProductNumber)
                || !chplProductNumberUtil.isCurrentChplProductNumberStyle(chplProductNumber)) {
            return;
        }

        String developerCode = null;
        try {
            developerCode = chplProductNumberUtil.getDeveloperCode(chplProductNumber);
        } catch (Exception ex) {
            LOGGER.warn("Cannot find Developer code in " + chplProductNumber);
        }

        Developer developer = listing.getDeveloper();
        if (developer != null && isValidDeveloperCode(listing.getChplProductNumber())) {
            if (DeveloperManager.NEW_DEVELOPER_CODE.equals(developerCode)
                    && developer.getId() != null) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.shouldNotHaveXXXXCode"));
            } else if (!DeveloperManager.NEW_DEVELOPER_CODE.equals(developerCode)
                    && developer.getId() == null) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.shouldHaveXXXXCode", developerCode));
            } else if (!StringUtils.isEmpty(developer.getDeveloperCode())
                    && !developerCode.equals(developer.getDeveloperCode())) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.developerCodeMismatch", developerCode, developer.getDeveloperCode()));
            } else if (!DeveloperManager.NEW_DEVELOPER_CODE.equals(developerCode)
                    && StringUtils.isEmpty(developer.getDeveloperCode())) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.missingDeveloperCode"));
            }
        }
    }

    private boolean isValidDeveloperCode(String chplProductNumber) {
        return validationUtils.chplNumberPartIsValid(chplProductNumber,
                ChplProductNumberUtil.DEVELOPER_CODE_INDEX,
                ChplProductNumberUtil.DEVELOPER_CODE_REGEX);
    }
}
