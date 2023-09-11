package gov.healthit.chpl.util;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.ChplProductNumberDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@NoArgsConstructor
@Log4j2
public class ChplProductNumberUtil {
    public static final String LEGACY_ID_BEGIN = "CHP-";
    public static final String CHPL_PRODUCT_NUMBER_SEARCH_REGEX = "(\\d{2}\\.){3}\\d{4}\\.(\\w{4}\\.(\\w{2}\\.(\\d{2}\\.(\\d\\.(\\d{6})?)?)?)?)?";

    public static final int EDITION_CODE_INDEX = 0;
    public static final String EDITION_CODE_DEFAULT = "15";
    private static final String EDITION_CODE_REGEX = "^[0-9]{" + ChplProductNumberUtil.EDITION_CODE_LENGTH + "}$";
    public static final int EDITION_CODE_LENGTH = 2;
    public static final int ATL_CODE_INDEX = 1;
    public static final String ATL_CODE_REGEX = "^[0-9]{" + ChplProductNumberUtil.ATL_CODE_LENGTH + "}$";
    public static final int ATL_CODE_LENGTH = 2;
    public static final int ACB_CODE_INDEX = 2;
    public static final String ACB_CODE_REGEX = "^[0-9]{" + ChplProductNumberUtil.ACB_CODE_LENGTH + "}$";
    public static final int ACB_CODE_LENGTH = 2;
    public static final int DEVELOPER_CODE_INDEX = 3;
    public static final String DEVELOPER_CODE_REGEX = "^[0-9]{" + ChplProductNumberUtil.DEVELOPER_CODE_LENGTH + "}|XXXX$";
    public static final int DEVELOPER_CODE_LENGTH = 4;
    public static final int PRODUCT_CODE_INDEX = 4;
    public static final String PRODUCT_CODE_REGEX = "^[a-zA-Z0-9_]{" + ChplProductNumberUtil.PRODUCT_CODE_LENGTH + "}$";
    public static final int PRODUCT_CODE_LENGTH = 4;
    public static final int VERSION_CODE_INDEX = 5;
    public static final String VERSION_CODE_REGEX = "^[a-zA-Z0-9_]{" + ChplProductNumberUtil.VERSION_CODE_LENGTH + "}$";
    public static final int VERSION_CODE_LENGTH = 2;
    public static final int ICS_CODE_INDEX = 6;
    public static final int ICS_CODE_LENGTH = 2;
    public static final String ICS_CODE_REGEX = "^[0-9]{" + ChplProductNumberUtil.ICS_CODE_LENGTH + "}$";
    public static final int ADDITIONAL_SOFTWARE_CODE_INDEX = 7;
    public static final int ADDITIONAL_SOFTWARE_CODE_LENGTH = 1;
    public static final String ADDITIONAL_SOFTWARE_CODE_REGEX = "^0|1$";
    public static final int CERTIFIED_DATE_CODE_INDEX = 8;
    public static final int CERTIFIED_DATE_CODE_LENGTH = 6;
    public static final String CERTIFIED_DATE_CODE_REGEX = "^[0-9]{" + ChplProductNumberUtil.CERTIFIED_DATE_CODE_LENGTH + "}$";
    public static final int CHPL_PRODUCT_ID_PARTS = 9;

    private static final int LEGACY_ID_LENGTH = 10;

    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private ChplProductNumberDAO chplProductNumberDAO;

    @Autowired
    public ChplProductNumberUtil(CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            ChplProductNumberDAO chplProductNumberDAO) {
        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.chplProductNumberDAO = chplProductNumberDAO;
    }

    /**
     * Gets the CHPL Product Number as calculated by the DB.
     *
     * @param certifiedProductId
     *            - Long
     * @return - String
     */
    @Transactional
    public String generate(final Long certifiedProductId) {
        return chplProductNumberDAO.getChplProductNumber(certifiedProductId);
    }

    /**
     * Determines if a CHPL Product Number already exists in the database.
     *
     * @param chplProductNumber
     *            - String representing the CHPL Product Number to check
     * @return Boolean - true if the value does not exist, false if the value exists
     */
    public Boolean isUnique(final String chplProductNumber) {
        List<CertifiedProductDetailsDTO> details = null;
        try {
            details = certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        } catch (EntityRetrievalException e) {
            return true; // Need to determine the correct action here
        }
        return !(details != null && details.size() > 0);
    }

    @SuppressWarnings({"checkstyle:parameternumber"})
    public String getChplProductNumber(String year, String testingLab, String certBody,
            String vendorCode, String productCode, String versionCode, String icsCode,
            String addlSoftwareCode, String certDateCode) {

        ChplProductNumberParts parts = new ChplProductNumberParts();
        parts.setEditionCode(year);
        parts.setAtlCode(testingLab);
        parts.setAcbCode(certBody);
        parts.setDeveloperCode(vendorCode);
        parts.setProductCode(productCode);
        parts.setVersionCode(versionCode);
        parts.setIcsCode(icsCode);
        parts.setAdditionalSoftwareCode(addlSoftwareCode);
        parts.setCertifiedDateCode(certDateCode);

        return concatParts(parts);
    }

    /**
     * Properly concats the parts of a legacy CHPL Product number.
     *
     * @param chplPrefix
     * @param identifier
     * @return
     */
    public String getChplProductNumber(final String chplPrefix, final String identifier) {
        StringBuffer chplProductNumber = new StringBuffer();
        chplProductNumber.append(chplPrefix)
                .append("-").append(identifier);

        return chplProductNumber.toString();
    }

    public ChplProductNumberParts parseChplProductNumber(String chplProductNumber) {
        String[] cpnParts = splitUniqueIdParts(chplProductNumber);

        ChplProductNumberParts parts = new ChplProductNumberParts();
        parts.setEditionCode(cpnParts[ChplProductNumberUtil.EDITION_CODE_INDEX]);
        parts.setAtlCode(cpnParts[ChplProductNumberUtil.ATL_CODE_INDEX]);
        parts.setAcbCode(cpnParts[ChplProductNumberUtil.ACB_CODE_INDEX]);
        parts.setDeveloperCode(cpnParts[ChplProductNumberUtil.DEVELOPER_CODE_INDEX]);
        parts.setProductCode(cpnParts[ChplProductNumberUtil.PRODUCT_CODE_INDEX]);
        parts.setVersionCode(cpnParts[ChplProductNumberUtil.VERSION_CODE_INDEX]);
        parts.setIcsCode(cpnParts[ChplProductNumberUtil.ICS_CODE_INDEX]);
        parts.setAdditionalSoftwareCode(cpnParts[ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX]);
        parts.setCertifiedDateCode(cpnParts[ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX]);

        return parts;
    }

    public boolean isLegacyChplProductNumberStyle(String chplProductNumber) {
        if (!StringUtils.isEmpty(chplProductNumber) && chplProductNumber.length() == LEGACY_ID_LENGTH
                && chplProductNumber.startsWith(LEGACY_ID_BEGIN)) {
            return true;
        }
        return false;
    }

    public boolean isCurrentChplProductNumberStyle(String chplProductNumber) {
        if (!StringUtils.isEmpty(chplProductNumber)) {
            String[] uniqueIdParts = chplProductNumber.split("\\.");
            if (uniqueIdParts.length == ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
                return true;
            }
        }
        return false;
    }

    public Integer getIcsCode(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        String icsCodeStr = parts.getIcsCode();
        Integer icsCode = null;
        try {
            icsCode = Integer.valueOf(icsCodeStr);
        } catch (NumberFormatException ex) {
            LOGGER.error("Cannot convert " + icsCodeStr + " to an integer.");
        }
        return icsCode;
    }

    public String getIcsCodeAsString(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getIcsCode();
    }

    public boolean hasIcsConflict(String uniqueId, Boolean hasIcs) {
        boolean hasIcsConflict = false;
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts.length == ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            Integer icsCodeInteger = Integer.valueOf(uniqueIdParts[ChplProductNumberUtil.ICS_CODE_INDEX]);
            if (icsCodeInteger != null && icsCodeInteger.intValue() == 0) {
                if (hasIcs != null && hasIcs.equals(Boolean.TRUE)) {
                    hasIcsConflict = true;
                }
            } else if (hasIcs == null || hasIcs.equals(Boolean.FALSE)
                    && icsCodeInteger != null
                    && icsCodeInteger.intValue() > 0) {
                hasIcsConflict = true;
            }
        }
        return hasIcsConflict;
    }

    public String getDeveloperCode(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getDeveloperCode();
    }

    public String getProductCode(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getProductCode();
    }

    public String getVersionCode(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getVersionCode();
    }

    public String getAcbCode(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getAcbCode();
    }

    public String getAtlCode(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getAtlCode();
    }

    public String getCertificationDateCode(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getCertifiedDateCode();
    }

    public String getAdditionalSoftwareCode(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getAdditionalSoftwareCode();
    }

    public String getCertificationEditionCode(String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getEditionCode();
    }

    public String getCertificationEditionCodeRegex() {
        return EDITION_CODE_REGEX;
    }

    public List<String> getAllowedEditionCodes() {
        return Stream.of("15").toList();
    }

    private String[] splitUniqueIdParts(final String uniqueId) {
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts == null || uniqueIdParts.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            return new String[0]; // Maybe an exception??
        }
        return uniqueIdParts;
    }

    private String concatParts(final ChplProductNumberParts chplProductNumberParts) {
        StringBuilder chplProductNumber = new StringBuilder();
        chplProductNumber.append(formatEdition(chplProductNumberParts.editionCode)).append(".")
                .append(chplProductNumberParts.atlCode).append(".")
                .append(chplProductNumberParts.acbCode).append(".")
                .append(chplProductNumberParts.developerCode).append(".")
                .append(chplProductNumberParts.productCode).append(".")
                .append(chplProductNumberParts.versionCode).append(".")
                .append(chplProductNumberParts.icsCode).append(".")
                .append(chplProductNumberParts.additionalSoftwareCode).append(".")
                .append(chplProductNumberParts.certifiedDateCode);
        return chplProductNumber.toString();
    }

    private String formatEdition(final String year) {
        if (year.length() == 2) {
            return year;
        } else {
            return year.substring(2);
        }
    }

    public static class ChplProductNumberParts {
        private String editionCode = null;
        private String atlCode = null;
        private String acbCode = null;
        private String developerCode = null;
        private String productCode = null;
        private String versionCode = null;
        private String icsCode = null;
        private String additionalSoftwareCode = null;
        private String certifiedDateCode = null;

        public String getEditionCode() {
            return editionCode;
        }

        public void setEditionCode(final String editionCode) {
            this.editionCode = editionCode;
        }

        public String getAtlCode() {
            return atlCode;
        }

        public void setAtlCode(final String atlCode) {
            this.atlCode = atlCode;
        }

        public String getAcbCode() {
            return acbCode;
        }

        public void setAcbCode(final String acbCode) {
            this.acbCode = acbCode;
        }

        public String getDeveloperCode() {
            return developerCode;
        }

        public void setDeveloperCode(final String developerCode) {
            this.developerCode = developerCode;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(final String productCode) {
            this.productCode = productCode;
        }

        public String getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(final String versionCode) {
            this.versionCode = versionCode;
        }

        public String getIcsCode() {
            return icsCode;
        }

        public void setIcsCode(final String icsCode) {
            this.icsCode = icsCode;
        }

        public String getAdditionalSoftwareCode() {
            return additionalSoftwareCode;
        }

        public void setAdditionalSoftwareCode(final String additionalSoftwareCode) {
            this.additionalSoftwareCode = additionalSoftwareCode;
        }

        public String getCertifiedDateCode() {
            return certifiedDateCode;
        }

        public void setCertifiedDateCode(final String certifiedDateCode) {
            this.certifiedDateCode = certifiedDateCode;
        }
    }
}
