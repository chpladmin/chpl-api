package gov.healthit.chpl.validation.certifiedProduct;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("allowedValidator")
public class CertifiedProductAllowedValidator implements CertifiedProductValidator {

    @Override
    public void validate(PendingCertifiedProductDTO product) {
        // does nothing, everything is valid
    }

    @Override
    public void validate(CertifiedProductSearchDetails product) {
        // does nothing, everything is valid
    }

    @Override
    public boolean validateUniqueId(String uniqueId) {
        return true;
    }

    @Override
    public boolean validateProductCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {

            // validate that these pieces match up with data
            String productCode = uniqueIdParts[CertifiedProductDTO.PRODUCT_CODE_INDEX];
            if (StringUtils.isEmpty(productCode)
                    || !productCode.matches("^[a-zA-Z0-9_]{" + CertifiedProductDTO.PRODUCT_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateVersionCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {

            // validate that these pieces match up with data
            String versionCode = uniqueIdParts[CertifiedProductDTO.VERSION_CODE_INDEX];
            if (StringUtils.isEmpty(versionCode)
                    || !versionCode.matches("^[a-zA-Z0-9_]{" + CertifiedProductDTO.VERSION_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateIcsCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String icsCode = uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX];
            if (StringUtils.isEmpty(icsCode)
                    || !icsCode.matches("^[0-9]{" + CertifiedProductDTO.ICS_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateAdditionalSoftwareCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String icsCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
            if (StringUtils.isEmpty(icsCode) || !icsCode.matches("^0|1$")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateCertifiedDateCodeCharacters(String chplProductNumber) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts != null && uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            // validate that these pieces match up with data
            String icsCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];
            if (StringUtils.isEmpty(icsCode)
                    || !icsCode.matches("^[0-9]{" + CertifiedProductDTO.CERTIFIED_DATE_CODE_LENGTH + "}$")) {
                return false;
            }
        }
        return true;
    }
}
