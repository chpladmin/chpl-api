package gov.healthit.chpl.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.dto.TestingLabDTO;

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

    private static final int CERTIFICATION_EDITION_BEGIN_INDEX = 2;

    private static final int CERTIFICATION_EDITION_END_INDEX = 4;

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
    
    public String getChplProductNumber( final String year, final String testingLab, final String certBody,
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
    
    public String getChplProductNumber(final String chplPrefix, final String identifier) {
        StringBuffer chplProductNumber = new StringBuffer();
        chplProductNumber.append(chplPrefix)
            .append("-").append(identifier);
        
        return chplProductNumber.toString();
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
        chplProductNumber.append(chplProductNumberParts.editionCode).append(".")
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

    class ChplProductNumberParts {
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
