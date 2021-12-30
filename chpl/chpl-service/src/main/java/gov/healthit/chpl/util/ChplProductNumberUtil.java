package gov.healthit.chpl.util;

import java.util.List;

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
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@NoArgsConstructor
@Log4j2
public class ChplProductNumberUtil {
    public static final int EDITION_CODE_INDEX = 0;
    public static final String EDITION_CODE_REGEX = "^[0-9]{" + ChplProductNumberUtil.EDITION_CODE_LENGTH + "}$";
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

    private static final int CERTIFICATION_EDITION_BEGIN_INDEX = 2;
    private static final int CERTIFICATION_EDITION_END_INDEX = 4;

    private static final int LEGACY_ID_LENGTH = 10;
    private static final String LEGACY_ID_BEGIN = "CHP-";

    public static final String CHPL_PRODUCT_NUMBER_SEARCH_REGEX = "(\\d{2}\\.){3}\\d{4}\\.(\\w{4}\\.(\\w{2}\\.(\\d{2}\\.(\\d\\.(\\d{6})?)?)?)?)?";

    private TestingLabDAO testingLabDAO;
    private CertificationBodyDAO certBodyDAO;
    private DeveloperDAO developerDAO;
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
    private ChplProductNumberDAO chplProductNumberDAO;
    private CertifiedProductDAO cpDao;

    @Autowired
    public ChplProductNumberUtil(TestingLabDAO testingLabDAO,
            CertificationBodyDAO certBodyDAO,
            DeveloperDAO developerDAO,
            CertifiedProductSearchResultDAO certifiedProductSearchResultDAO,
            ChplProductNumberDAO chplProductNumberDAO,
            CertifiedProductDAO cpDao) {
        this.testingLabDAO = testingLabDAO;
        this.certBodyDAO = certBodyDAO;
        this.developerDAO = developerDAO;
        this.certifiedProductSearchResultDAO = certifiedProductSearchResultDAO;
        this.chplProductNumberDAO = chplProductNumberDAO;
        this.cpDao = cpDao;
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
     * Determines what the derived CHPL Product Number will be based on the values passed.
     *
     * @param uniqueId
     *            - Unique ID from the product
     * @param certificationEdition
     *            4 character year, i.e. "2015"
     * @param testingLabs
     *            the testing Labs used for the Listing
     * @param certificationBodyId
     *            Id (not code) for the certification body
     * @param developerId
     *            Id (not code) for the developer
     * @return String representing the derived CHPL Product Number
     */
    public String generate(final String uniqueId, final String certificationEdition,
            final List<PendingCertifiedProductTestingLabDTO> testingLabs, final Long certificationBodyId,
            final Long developerId) {

        String[] uniqueIdParts = splitUniqueIdParts(uniqueId);
        ChplProductNumberParts parts = new ChplProductNumberParts();
        parts.editionCode = certificationEdition.substring(CERTIFICATION_EDITION_BEGIN_INDEX, CERTIFICATION_EDITION_END_INDEX);
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

    /**
     * Determines if the given CHPL ID is a listing in the system.
     *
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

    public CertifiedProduct getListing(String chplProductNumber) {
        CertifiedProduct listing = null;
        if (chplProductNumber.startsWith("CHP-")) {
            try {
                CertifiedProductDTO chplProduct = cpDao.getByChplNumber(chplProductNumber);
                if (chplProduct != null) {
                    CertifiedProductDetailsDTO cpDetails = cpDao.getDetailsById(chplProduct.getId());
                    if (cpDetails != null) {
                        listing = new CertifiedProduct(cpDetails);
                    }
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not look up " + chplProductNumber, ex);
            }
        } else {
            try {
                CertifiedProductDetailsDTO cpDetails = cpDao.getByChplUniqueId(chplProductNumber);
                if (cpDetails != null) {
                    listing = new CertifiedProduct(cpDetails);
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not look up " + chplProductNumber, ex);
            }
        }
        return listing;
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
        return parts.getProductCode();
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

    private String getTestingLabCode(final List<PendingCertifiedProductTestingLabDTO> testingLabs) {
        if (testingLabs.size() > 1) {
            return TestingLab.MULTIPLE_TESTING_LABS_CODE;
        } else {
            TestingLabDTO dto = testingLabDAO.getByName(testingLabs.get(0).getTestingLabName());
            if (dto != null) {
                return dto.getTestingLabCode();
            } else {
                return null; // Throw excepotion?
            }
        }
    }

    private String getDeveloperCode(final Long developerId) {
        DeveloperDTO dto = null;
        try {
            dto = developerDAO.getById(developerId);
        } catch (EntityRetrievalException e) {
            return null; // Throw Exception??
        }
        if (dto != null) {
            return dto.getDeveloperCode();
        } else {
            return null; // Throw exception?
        }
    }

    private String getCertificationBodyCode(final Long certificationBodyId) {
        CertificationBodyDTO dto = null;
        try {
            dto = certBodyDAO.getById(certificationBodyId);
        } catch (EntityRetrievalException e) {
            return null; // Throw exception?
        }
        if (dto != null) {
            return dto.getAcbCode();
        } else {
            return null; // Throw exception?
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
