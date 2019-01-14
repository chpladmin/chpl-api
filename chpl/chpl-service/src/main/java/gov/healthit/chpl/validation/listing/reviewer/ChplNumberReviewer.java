package gov.healthit.chpl.validation.listing.reviewer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("chplNumberReviewer")
public class ChplNumberReviewer implements Reviewer {
    private CertifiedProductDAO cpDao;
    private CertificationResultManager certificationResultManager;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ChplNumberReviewer(CertifiedProductDAO cpDao, CertificationResultManager certificationResultManager,
            ErrorMessageUtil msgUtil) {
        this.cpDao = cpDao;
        this.certificationResultManager = certificationResultManager;
        this.msgUtil = msgUtil;
    }

    /**
     * Looks at the format of the CHPL Product Number
     * Makes sure each part of the identifier is correctly formatted and is the correct value.
     * May change the CHPL ID if necessary (if additional software was added or certification date was changed)
     * and if the CHPL ID is changed, confirms that the new ID is unique.
     */
    public void review(CertifiedProductSearchDetails listing) {
        boolean productIdChanged = false;
        String uniqueId = listing.getChplProductNumber();
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
            String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];

            if (!validateProductCodeCharacters(listing.getChplProductNumber())) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badProductCodeChars", CertifiedProductDTO.PRODUCT_CODE_LENGTH));
            }

            if (!validateVersionCodeCharacters(listing.getChplProductNumber())) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badVersionCodeChars", CertifiedProductDTO.VERSION_CODE_LENGTH));
            }

            if (!validateIcsCodeCharacters(listing.getChplProductNumber())) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badIcsCodeChars", CertifiedProductDTO.ICS_CODE_LENGTH));
            } else {
                Integer icsCodeInteger = Integer.valueOf(uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX]);
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

            if (!validateAdditionalSoftwareCodeCharacters(listing.getChplProductNumber())) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badAdditionalSoftwareCodeChars", CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_LENGTH));
            } else {
                boolean hasAS = certificationResultManager.getCertifiedProductHasAdditionalSoftware(listing.getId());
                String desiredAdditionalSoftwareCode = hasAS ? "1" : "0";
                if (!additionalSoftwareCode.equals(desiredAdditionalSoftwareCode)) {
                    updateChplProductNumber(listing, CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX,
                            desiredAdditionalSoftwareCode);
                    productIdChanged = true;
                }
            }

            if (!validateCertifiedDateCodeCharacters(listing.getChplProductNumber())) {
                listing.getErrorMessages()
                .add(msgUtil.getMessage("listing.badCertifiedDateCodeChars", CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH));
            }
            SimpleDateFormat idDateFormat = new SimpleDateFormat("yyMMdd");
            idDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String desiredCertificationDateCode = idDateFormat.format(listing.getCertificationDate());
            if (!certifiedDateCode.equals(desiredCertificationDateCode)) {
                // change the certified date code to match the new certification
                // date
                updateChplProductNumber(listing, CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX,
                        desiredCertificationDateCode);
                productIdChanged = true;
            }
        }

        if (productIdChanged) {
            // make sure the unique id is really unique -
            // only check this if we know it changed
            // because if it hasn't changed there will be 1 product with its id (itself)
            if (!validateUniqueId(listing.getChplProductNumber())) {
                listing.getErrorMessages().add("The id " + listing.getChplProductNumber()
                + " must be unique among all other certified products but one already exists with this ID.");
            }
        }
    }

    public boolean validateUniqueId(final String chplProductNumber) {
        try {
            CertifiedProductDetailsDTO dup = cpDao.getByChplUniqueId(chplProductNumber);
            if (dup != null) {
                return false;
            }
        } catch (final EntityRetrievalException ex) {
        }
        return true;
    }

    public void updateChplProductNumber(final CertifiedProductSearchDetails product, final int productNumberIndex,
            final String newValue) {
        String[] uniqueIdParts = product.getChplProductNumber().split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
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

    public boolean validateProductCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {

            // validate that these pieces match up with data
            String productCode = uniqueIdParts[CertifiedProductDTO.PRODUCT_CODE_INDEX];
            if (StringUtils.isEmpty(productCode)
                    || !productCode.matches("^[a-zA-Z0-9_]{" + CertifiedProductDTO.PRODUCT_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    public boolean validateVersionCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {

            // validate that these pieces match up with data
            String versionCode = uniqueIdParts[CertifiedProductDTO.VERSION_CODE_INDEX];
            if (StringUtils.isEmpty(versionCode)
                    || !versionCode.matches("^[a-zA-Z0-9_]{" + CertifiedProductDTO.VERSION_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    public boolean validateIcsCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String icsCode = uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX];
            if (StringUtils.isEmpty(icsCode)
                    || !icsCode.matches("^[0-9]{" + CertifiedProductDTO.ICS_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    public boolean validateAdditionalSoftwareCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
            if (StringUtils.isEmpty(additionalSoftwareCode) || !additionalSoftwareCode.matches("^0|1$")) {
                return false;
            }
        }
        return true;
    }

    public boolean validateCertifiedDateCodeCharacters(final String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];
            if (StringUtils.isEmpty(certifiedDateCode)
                    || !certifiedDateCode.matches("^[0-9]{" + CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

}
