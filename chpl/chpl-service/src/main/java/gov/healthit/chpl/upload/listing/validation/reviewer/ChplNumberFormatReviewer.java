package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("chplNumberFormatReviewer")
public class ChplNumberFormatReviewer implements Reviewer {
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplNumberFormatReviewer(ChplProductNumberUtil chplProductNumberUtil,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        String chplProductNumber = listing.getChplProductNumber();
        if (chplProductNumberUtil.isLegacyChplProductNumberStyle(chplProductNumber)) {
            return;
        }

        if (StringUtils.isEmpty(chplProductNumber)) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.chplProductNumberMissing"));
            return;
        }

        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.chplProductNumberInvalidFormat", ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS));
            return;
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.ATL_CODE_INDEX,
                ChplProductNumberUtil.ATL_CODE_REGEX)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.badAtlCodeChars", ChplProductNumberUtil.ATL_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.ACB_CODE_INDEX,
                ChplProductNumberUtil.ACB_CODE_REGEX)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.badAcbCodeChars", ChplProductNumberUtil.ACB_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.DEVELOPER_CODE_INDEX,
                ChplProductNumberUtil.DEVELOPER_CODE_REGEX)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.badDeveloperCodeChars", ChplProductNumberUtil.DEVELOPER_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.PRODUCT_CODE_INDEX,
                ChplProductNumberUtil.PRODUCT_CODE_REGEX)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.badProductCodeChars", ChplProductNumberUtil.PRODUCT_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.VERSION_CODE_INDEX,
                ChplProductNumberUtil.VERSION_CODE_REGEX)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.badVersionCodeChars", ChplProductNumberUtil.VERSION_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.ICS_CODE_INDEX,
                ChplProductNumberUtil.ICS_CODE_REGEX)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.badIcsCodeChars", ChplProductNumberUtil.ICS_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX,
                ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_REGEX)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.badAdditionalSoftwareCodeChars",
                            ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_LENGTH));
        }

        if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX,
                ChplProductNumberUtil.CERTIFIED_DATE_CODE_REGEX)) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.badCertifiedDateCodeChars", ChplProductNumberUtil.CERTIFIED_DATE_CODE_LENGTH));
        }
    }
}
