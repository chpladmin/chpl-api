package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.CertificationEditionEntity;
import gov.healthit.chpl.util.Util;

public class CertificationEditionDTO implements Serializable {
    private static final long serialVersionUID = -2554595626818018414L;
    private Long id;
    private Set<CertificationCriterionDTO> certificationCriterions = new HashSet<CertificationCriterionDTO>();
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String year;
    private Boolean retired;

    public CertificationEditionDTO() {
    }

    public CertificationEditionDTO(CertificationEditionEntity entity) {

        this.id = entity.getId();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.isDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.year = entity.getYear();
        this.retired = entity.getRetired();
        Set<CertificationCriterionEntity> certCriterionEntities = entity.getCertificationCriterions();
        if (certCriterionEntities != null && certCriterionEntities.size() > 0) {
            for (CertificationCriterionEntity certCriterion : certCriterionEntities) {
                CertificationCriterionDTO ccDto = new CertificationCriterionDTO(certCriterion);
                this.certificationCriterions.add(ccDto);
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Set<CertificationCriterionDTO> getCertificationCriterions() {
        return certificationCriterions;
    }

    public void setCertificationCriterions(final Set<CertificationCriterionDTO> certificationCriterions) {
        this.certificationCriterions = certificationCriterions;
    }

    public void addCertificationCriterion(CertificationCriterionDTO certificationCriterion) {
        this.certificationCriterions.add(certificationCriterion);
    }

    public void removeCertificationCriterion(CertificationCriterionDTO certificationCriterion) {
        this.certificationCriterions.remove(certificationCriterion);
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public Boolean getRetired() {
        return retired;
    }

    public void setRetired(final Boolean retired) {
        this.retired = retired;
    }

}
