package gov.healthit.chpl.validation.listing.reviewer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

@Component("chplNumberReviewer")
public class ChplNumberReviewer implements Reviewer {
    private CertificationResultManager certificationResultManager;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplNumberReviewer(CertificationResultManager certificationResultManager,
            ChplProductNumberUtil chplProductNumberUtil, ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        this.certificationResultManager = certificationResultManager;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    /**
     * Looks at the format of the CHPL Product Number
     * Makes sure each part of the identifier is correctly formatted and is the correct value.
     * May change the CHPL ID if necessary (if additional software was added or certification date was changed)
     * and if the CHPL ID is changed, confirms that the new ID is unique.
     */
    public void review(final CertifiedProductSearchDetails listing) {
        String origUniqueId = listing.getChplProductNumber();
        String[] uniqueIdParts = origUniqueId.split("\\.");
        if (uniqueIdParts.length == ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String additionalSoftwareCode = uniqueIdParts[ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX];
            String certifiedDateCode = uniqueIdParts[ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX];

            if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                    ChplProductNumberUtil.PRODUCT_CODE_INDEX,
                    ChplProductNumberUtil.PRODUCT_CODE_REGEX)) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badProductCodeChars", ChplProductNumberUtil.PRODUCT_CODE_LENGTH));
            }

            if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                    ChplProductNumberUtil.VERSION_CODE_INDEX,
                    ChplProductNumberUtil.VERSION_CODE_REGEX)) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badVersionCodeChars", ChplProductNumberUtil.VERSION_CODE_LENGTH));
            }

            if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                    ChplProductNumberUtil.ICS_CODE_INDEX,
                    ChplProductNumberUtil.ICS_CODE_REGEX)) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badIcsCodeChars", ChplProductNumberUtil.ICS_CODE_LENGTH));
            } else {
                Integer icsCodeInteger = Integer.valueOf(uniqueIdParts[ChplProductNumberUtil.ICS_CODE_INDEX]);
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

            if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                    ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX,
                    ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_REGEX)) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badAdditionalSoftwareCodeChars",
                        ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_LENGTH));
            } else {
                boolean hasAS = certificationResultManager.getCertifiedProductHasAdditionalSoftware(listing.getId());
                String desiredAdditionalSoftwareCode = hasAS ? "1" : "0";
                if (!additionalSoftwareCode.equals(desiredAdditionalSoftwareCode)) {
                    updateChplProductNumber(listing, ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX,
                            desiredAdditionalSoftwareCode);
                }
            }

            if (!validationUtils.chplNumberPartIsValid(listing.getChplProductNumber(),
                    ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX,
                    ChplProductNumberUtil.CERTIFIED_DATE_CODE_REGEX)) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badCertifiedDateCodeChars", ChplProductNumberUtil.CERTIFIED_DATE_CODE_LENGTH));
            }
            SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
            idDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String desiredCertificationDateCode = idDateFormat.format(listing.getCertificationDate());
            if (!certifiedDateCode.equals(desiredCertificationDateCode)) {
                // change the certified date code to match the new certification
                // date
                updateChplProductNumber(listing, ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX,
                        desiredCertificationDateCode);
            }
        }

        String updatedUniqueId = listing.getChplProductNumber();
        if (!origUniqueId.equals(updatedUniqueId)) {
            // make sure the unique id is really unique -
            // only check this if we know it changed
            // because if it hasn't changed there will be 1 product with its id (itself)
            if (!chplProductNumberUtil.isUnique(listing.getChplProductNumber())) {
                listing.getErrorMessages().add(
                        msgUtil.getMessage("listing.chplProductNumber.systemChangedNotUnique",
                        origUniqueId,
                        updatedUniqueId));
            }
        }
    }

    private void updateChplProductNumber(final CertifiedProductSearchDetails product, final int productNumberIndex,
            final String newValue) {
        String[] uniqueIdParts = product.getChplProductNumber().split("\\.");
        if (uniqueIdParts.length == ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            StringBuffer newCodeBuffer = new StringBuffer();
            for (int idx = 0; idx < uniqueIdParts.length; idx++) {
                if (idx == productNumberIndex) {
                    newCodeBuffer.append(newValue);
                } else {
                    newCodeBuffer.append(uniqueIdParts[idx]);
                }

                if (idx < uniqueIdParts.length - 1) {
                    newCodeBuffer.append(".");
                }
            }
            product.setChplProductNumber(newCodeBuffer.toString());
        }
    }
}
