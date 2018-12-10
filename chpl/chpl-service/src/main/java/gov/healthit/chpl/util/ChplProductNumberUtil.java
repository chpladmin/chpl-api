package gov.healthit.chpl.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.ChplProductNumberDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * A component that provides some helpful methods for dealing with Chpl Product Numbers.
 * @author TYoung
 *
 */
@Component
public class ChplProductNumberUtil {
    @Autowired
    private TestingLabDAO testingLabDAO;

    @Autowired
    private CertificationBodyDAO certBodyDAO;

    @Autowired
    private DeveloperDAO developerDAO;

    @Autowired
    private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;

    @Autowired
    private ChplProductNumberDAO chplProductNumberDAO;

    private static final int CERTIFICATION_EDITION_BEGIN_INDEX = 2;

    private static final int CERTIFICATION_EDITION_END_INDEX = 4;

    private static final int LEGACY_ID_LENGTH = 10;
    private static final String LEGACY_ID_BEGIN = "CHP-";

    /**
     * Gets the CHPL Product Number as calculated by the DB
     * @param certifiedProductId - Long
     * @return - String
     */
    public String generate(Long certifiedProductId) {
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
        parts.productCode = uniqueIdParts[CertifiedProductDTO.PRODUCT_CODE_INDEX];
        parts.versionCode = uniqueIdParts[CertifiedProductDTO.VERSION_CODE_INDEX];
        parts.icsCode = uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX];
        parts.additionalSoftwareCode = uniqueIdParts[CertifiedProductDTO.ADDITIONAL_SOFTWARE_CODE_INDEX];
        parts.certifiedDateCode = uniqueIdParts[CertifiedProductDTO.CERTIFIED_DATE_CODE_INDEX];

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
        parts.setEditionCode(cpnParts[0]);
        parts.setAtlCode(cpnParts[1]);
        parts.setAcbCode(cpnParts[2]);
        parts.setDeveloperCode(cpnParts[3]);
        parts.setProductCode(cpnParts[4]);
        parts.setVersionCode(cpnParts[5]);
        parts.setIcsCode(cpnParts[6]);
        parts.setAdditionalSoftwareCode(cpnParts[7]);
        parts.setCertifiedDateCode(cpnParts[8]);

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
        if(!isLegacy(chplProductNumber)) {
            String[] uniqueIdParts = chplProductNumber.split("\\.");
            icsCode = Integer.valueOf(uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX]);
        }
        return icsCode;
    }

    public boolean hasIcsConflict(String uniqueId, Boolean hasIcs) {
        boolean hasIcsConflict = false;
        String[] uniqueIdParts = uniqueId.split("\\.");
        if(uniqueIdParts.length == CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
            Integer icsCodeInteger = Integer.valueOf(uniqueIdParts[CertifiedProductDTO.ICS_CODE_INDEX]);
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

    private String[] splitUniqueIdParts(final String uniqueId) {
        String[] uniqueIdParts = uniqueId.split("\\.");
        if (uniqueIdParts == null || uniqueIdParts.length != CertifiedProductDTO.CHPL_PRODUCT_ID_PARTS) {
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
