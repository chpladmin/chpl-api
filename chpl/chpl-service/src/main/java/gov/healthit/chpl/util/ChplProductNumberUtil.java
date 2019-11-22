package gov.healthit.chpl.util;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.ChplProductNumberDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * A component that provides some helpful methods for dealing with Chpl Product Numbers.
 * @author TYoung
 *
 */
@Component
public class ChplProductNumberUtil {
    private static final Logger LOGGER = LogManager.getLogger(ChplProductNumberUtil.class);

    /**
     * Location of the EDITION in the CHPL_PRODUCT_ID.
     */
    public static final int EDITION_CODE_INDEX = 0;

    /**
     * Location of the ATL in the CHPL_PRODUCT_ID.
     */
    public static final int ATL_CODE_INDEX = 1;

    /**
     * Location of the ACB in the CHPL_PRODUCT_ID.
     */
    public static final int ACB_CODE_INDEX = 2;

    /**
     * Location of the Developer in the CHPL_PRODUCT_ID.
     */
    public static final int DEVELOPER_CODE_INDEX = 3;

    /**
     * Location of the Product in the CHPL_PRODUCT_ID.
     */
    public static final int PRODUCT_CODE_INDEX = 4;

    /**
     * Regex for valid product codes.
     */
    public static final String PRODUCT_CODE_REGEX =
            "^[a-zA-Z0-9_]{" + ChplProductNumberUtil.PRODUCT_CODE_LENGTH + "}$";

    /**
     * Required length of the PRODUCT code.
     */
    public static final int PRODUCT_CODE_LENGTH = 4;

    /**
     * Location of the Version in the CHPL_PRODUCT_ID.
     */
    public static final int VERSION_CODE_INDEX = 5;

    /**
     * Regex for valid version codes.
     */
    public static final String VERSION_CODE_REGEX =
            "^[a-zA-Z0-9_]{" + ChplProductNumberUtil.VERSION_CODE_LENGTH + "}$";
    /**
     * Required length of the VERSION code.
     */
    public static final int VERSION_CODE_LENGTH = 2;

    /**
     * Location of the ICS in the CHPL_PRODUCT_ID.
     */
    public static final int ICS_CODE_INDEX = 6;

    /**
     * Required length of the ICS code.
     */
    public static final int ICS_CODE_LENGTH = 2;

    /**
     * Regex for valid ICS codes.
     */
    public static final String ICS_CODE_REGEX =
            "^[0-9]{" + ChplProductNumberUtil.ICS_CODE_LENGTH + "}$";

    /**
     * Location of the Additional Software in the CHPL_PRODUCT_ID.
     */
    public static final int ADDITIONAL_SOFTWARE_CODE_INDEX = 7;

    /**
     * Required length of the ADDITIONAL SOFTWARE code.
     */
    public static final int ADDITIONAL_SOFTWARE_CODE_LENGTH = 1;

    /**
     * Regex for valid additional software codes.
     */
    public static final String ADDITIONAL_SOFTWARE_CODE_REGEX = "^0|1$";

    /**
     * Location of the Certification Date in the CHPL_PRODUCT_ID.
     */
    public static final int CERTIFIED_DATE_CODE_INDEX = 8;

    /**
     * Required length of the CERTIFICATION DATE code.
     */
    public static final int CERTIFIED_DATE_CODE_LENGTH = 6;

    /**
     * Regex for valid certified date codes.
     */
    public static final String CERTIFIED_DATE_CODE_REGEX =
            "^[0-9]{" + ChplProductNumberUtil.CERTIFIED_DATE_CODE_LENGTH + "}$";

    /**
     * How many parts there are in the CHPL Product ID.
     */
    public static final int CHPL_PRODUCT_ID_PARTS = 9;

    private static final int CERTIFICATION_EDITION_BEGIN_INDEX = 2;

    private static final int CERTIFICATION_EDITION_END_INDEX = 4;

    private static final int LEGACY_ID_LENGTH = 10;
    private static final String LEGACY_ID_BEGIN = "CHP-";

    /**
     * REGEX that matches a CHPL Product ID for searching.
     * Requires first four components (Edition, ATL, ACB, Developer Code).
     * Optional for remaining parts.
     */
    public static final String CHPL_PRODUCT_NUMBER_SEARCH_REGEX =
            "(\\d{2}\\.){3}\\d{4}\\.(\\w{4}\\.(\\w{2}\\.(\\d{2}\\.(\\d\\.(\\d{6})?)?)?)?)?";

    private TestingLabDAO testingLabDAO;
    private CertificationBodyDAO certBodyDAO;
    private DeveloperDAO developerDAO;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private ChplProductNumberDAO chplProductNumberDAO;
    private CertifiedProductDAO cpDao;

    @Autowired public ChplProductNumberUtil(final TestingLabDAO testingLabDAO,
            final CertificationBodyDAO certBodyDAO,
            final DeveloperDAO developerDAO,
            final CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            final ChplProductNumberDAO chplProductNumberDAO,
            final CertifiedProductDAO cpDao) {
        this.testingLabDAO = testingLabDAO;
        this.certBodyDAO = certBodyDAO;
        this.developerDAO = developerDAO;
        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.chplProductNumberDAO = chplProductNumberDAO;
        this.cpDao = cpDao;
    }

    /**
     * Gets the CHPL Product Number as calculated by the DB.
     * @param certifiedProductId - Long
     * @return - String
     */
    @Transactional
    public String generate(final Long certifiedProductId) {
        return chplProductNumberDAO.getChplProductNumber(certifiedProductId);
    }

    /**
     * Determines what the derived CHPL Product Number will be based on the values passed.
     * @param uniqueId - Unique ID from the product
     * @param certificationEdition 4 character year, i.e. "2015"
     * @param testingLabs the testing Labs used for the Listing
     * @param certificationBodyId Id (not code) for the certification body
     * @param developerId Id (not code) for the developer
     * @return String representing the derived CHPL Product Number
     */
    public String generate(final String uniqueId, final String certificationEdition,
            final List<PendingCertifiedProductTestingLabDTO> testingLabs, final Long certificationBodyId,
            final Long developerId) {

        String[] uniqueIdParts = splitUniqueIdParts(uniqueId);
        ChplProductNumberParts parts = new ChplProductNumberParts();
        parts.editionCode = certificationEdition.
                substring(CERTIFICATION_EDITION_BEGIN_INDEX, CERTIFICATION_EDITION_END_INDEX);
        parts.atlCode = getTestingLabCode(testingLabs);
        parts.acbCode = getCertificationBodyCode(certificationBodyId);
        parts.developerCode = getDeveloperCode(developerId);
        parts.productCode = uniqueIdParts[ChplProductNumberUtil.PRODUCT_CODE_INDEX];
        parts.versionCode = uniqueIdParts[ChplProductNumberUtil.VERSION_CODE_INDEX];
        parts.icsCode = uniqueIdParts[ChplProductNumberUtil.ICS_CODE_INDEX];
        parts.additionalSoftwareCode = uniqueIdParts[ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX];
        parts.certifiedDateCode = uniqueIdParts[ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX];

        return concatParts(parts);
    }

    /**
     * Determines if a CHPL Product Number already exists in the database.
     * @param chplProductNumber - String representing the CHPL Product Number to check
     * @return Boolean - true if the value does not exist, false if the value exists
     */
    public Boolean isUnique(final String chplProductNumber) {
        List<CertifiedProductDetailsDTO> details = null;
        try {
            details = certifiedProductSearchResultDAO.getByChplProductNumber(chplProductNumber);
        } catch (EntityRetrievalException e) {
            return true;  //Need to determine the correct action here
        }
        return !(details != null && details.size() > 0);
    }


    /**
     * Determines if the given CHPL ID is a listing in the system.
     * @param id
     * @return true if there is a listing with the chpl product number, false otherwise
     * @throws EntityRetrievalException
     */
    public boolean chplIdExists(final String chplProductNumber) throws EntityRetrievalException {
        if (StringUtils.isEmpty(chplProductNumber)) {
            return false;
        }

        boolean exists = false;
        if (chplProductNumber.startsWith("CHP")) {
            CertifiedProductDTO existing = cpDao.getByChplNumber(chplProductNumber);
            if (existing != null) {
                exists = true;
            }
        } else {
            try {
                CertifiedProductDetailsDTO existing = cpDao.getByChplUniqueId(chplProductNumber);
                if (existing != null) {
                    exists = true;
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not look up " + chplProductNumber, ex);
            }
        }
        return exists;
    }

    /**
     * Properly concats all of the parts of a CHPL Product Number.
     * @param year
     * @param testingLab
     * @param certBody
     * @param vendorCode
     * @param productCode
     * @param versionCode
     * @param icsCode
     * @param addlSoftwareCode
     * @param certDateCode
     * @return String
     */
    public String getChplProductNumber(final String year, final String testingLab, final String certBody,
            final String vendorCode, final String productCode, final String versionCode, final String icsCode,
            final String addlSoftwareCode, final String certDateCode) {

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

    public boolean isLegacy(String chplProductNumber) {
        if(!StringUtils.isEmpty(chplProductNumber) && chplProductNumber.length() == LEGACY_ID_LENGTH
                && chplProductNumber.startsWith(LEGACY_ID_BEGIN)) {
            return true;
        }
        return false;
    }

    public Integer getIcsCode(String chplProductNumber) {
        Integer icsCode = null;
        if (!isLegacy(chplProductNumber)) {
            String[] uniqueIdParts = chplProductNumber.split("\\.");
            icsCode = Integer.valueOf(uniqueIdParts[ChplProductNumberUtil.ICS_CODE_INDEX]);
        }
        return icsCode;
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

    public String getDeveloperCode(final String chplProductNumber) {
        ChplProductNumberParts parts = parseChplProductNumber(chplProductNumber);
        return parts.getDeveloperCode();
    }

    private String[] splitUniqueIdParts(final String uniqueId) {
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts == null || uniqueIdParts.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            return new String[0];  //Maybe an exception??
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

    private String getTestingLabCode(final List<PendingCertifiedProductTestingLabDTO> testingLabs) {
        if (testingLabs.size() > 1) {
            return "99";
        } else {
            TestingLabDTO dto = testingLabDAO.getByName(testingLabs.get(0).getTestingLabName());
            if (dto != null) {
                return dto.getTestingLabCode();
            } else {
                return null; //Throw excepotion?
            }
        }
    }

    private String getDeveloperCode(final Long developerId) {
        DeveloperDTO dto = null;
        try {
            dto = developerDAO.getById(developerId);
        } catch (EntityRetrievalException e) {
            return null;  //Throw Exception??
        }
        if (dto != null) {
            return dto.getDeveloperCode();
        } else {
            return null;  //Throw exception?
        }
    }

    private String getCertificationBodyCode(final Long certificationBodyId) {
        CertificationBodyDTO dto = null;
        try {
            dto = certBodyDAO.getById(certificationBodyId);
        } catch (EntityRetrievalException e) {
            return null;  //Throw exception?
        }
        if (dto != null) {
            return dto.getAcbCode();
        } else {
            return null;  //Throw exception?
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
