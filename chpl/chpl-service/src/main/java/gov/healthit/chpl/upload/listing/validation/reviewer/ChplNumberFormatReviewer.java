package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component("listingUploadChplNumberFormatReviewer")
public class ChplNumberFormatReviewer {
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplNumberFormatReviewer(ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        String chplProductNumber = listing.getChplProductNumber();
        if (StringUtils.isEmpty(chplProductNumber)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.chplProductNumberMissing"));
            return;
        }

        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.chplProductNumberInvalidFormat", ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS));
            return;
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.PRODUCT_CODE_INDEX,
                ChplProductNumberUtil.PRODUCT_CODE_REGEX)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.badProductCodeChars", ChplProductNumberUtil.PRODUCT_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.VERSION_CODE_INDEX,
                ChplProductNumberUtil.VERSION_CODE_REGEX)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.badVersionCodeChars", ChplProductNumberUtil.VERSION_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.ICS_CODE_INDEX,
                ChplProductNumberUtil.ICS_CODE_REGEX)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.badIcsCodeChars", ChplProductNumberUtil.ICS_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX,
                ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_REGEX)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.badAdditionalSoftwareCodeChars",
                    ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX,
                ChplProductNumberUtil.CERTIFIED_DATE_CODE_REGEX)) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.badCertifiedDateCodeChars", ChplProductNumberUtil.CERTIFIED_DATE_CODE_LENGTH));
        }
    }
}
