package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.entity.CertificationIdAndCertifiedProductEntity;
import gov.healthit.chpl.util.Util;

public class CertificationIdAndCertifiedProductDTO implements Serializable {
    private static final long serialVersionUID = 4517668914795030238L;
    private Long ehrCertificationId;
    private String certificationId;
    private Date creationDate;
    private String chplProductNumber;

    public CertificationIdAndCertifiedProductDTO(CertificationIdAndCertifiedProductEntity entity) {
        this.ehrCertificationId = entity.getEhrCertificationId();
        this.certificationId = entity.getCertificationId();
        this.creationDate = entity.getCreationDate();
        if (!StringUtils.isEmpty(entity.getLegacyChplNumber())) {
            this.chplProductNumber = entity.getLegacyChplNumber();
        } else {
            String yearCode = "";
            if (!StringUtils.isEmpty(entity.getCertificationYear())) {
                if (entity.getCertificationYear().length() == 4) {
                    yearCode = entity.getCertificationYear().substring(2, 4);
                } else {
                    yearCode = entity.getCertificationYear();
                }
            }
            this.chplProductNumber = yearCode + "." + entity.getAtlCode() + "." + entity.getAcbCode() + "."
                    + entity.getDeveloperCode() + "." + entity.getProductCode() + "." + entity.getVersionCode() + "."
                    + entity.getIcsCode() + "." + entity.getAdditionalSoftwareCode() + "."
                    + entity.getCertifiedDateCode();
        }
    }

    public Long getEhrCertificationId() {
        return ehrCertificationId;
    }

    public void setEhrCertificationId(final Long ehrCertificationId) {
        this.ehrCertificationId = ehrCertificationId;
    }

    public String getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(final String certificationId) {
        this.certificationId = certificationId;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }
}
