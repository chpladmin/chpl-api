package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertifiedProductQmsStandardEntity;

public class CertifiedProductQmsStandardDTO implements Serializable {
    private static final long serialVersionUID = -2498158351086396042L;
    private Long id;
    private Long certifiedProductId;
    private Long qmsStandardId;
    private String qmsStandardName;
    private String qmsModification;
    private String applicableCriteria;

    public CertifiedProductQmsStandardDTO() {
    }

    public CertifiedProductQmsStandardDTO(CertifiedProductQmsStandardEntity entity) {
        this.id = entity.getId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.qmsStandardId = entity.getQmsStandardId();
        if (entity.getQmsStandard() != null) {
            this.qmsStandardName = entity.getQmsStandard().getName();
        }
        this.qmsModification = entity.getModification();
        this.applicableCriteria = entity.getApplicableCriteria();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public Long getQmsStandardId() {
        return qmsStandardId;
    }

    public void setQmsStandardId(final Long qmsStandardId) {
        this.qmsStandardId = qmsStandardId;
    }

    public String getQmsStandardName() {
        return qmsStandardName;
    }

    public void setQmsStandardName(final String qmsStandardName) {
        this.qmsStandardName = qmsStandardName;
    }

    public String getQmsModification() {
        return qmsModification;
    }

    public void setQmsModification(final String qmsModification) {
        this.qmsModification = qmsModification;
    }

    public String getApplicableCriteria() {
        return applicableCriteria;
    }

    public void setApplicableCriteria(final String applicableCriteria) {
        this.applicableCriteria = applicableCriteria;
    }
}
