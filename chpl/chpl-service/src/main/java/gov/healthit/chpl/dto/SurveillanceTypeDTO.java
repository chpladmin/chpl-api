package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceTypeEntity;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.util.Util;

public class SurveillanceTypeDTO implements Serializable {
    private static final long serialVersionUID = -7207194303881851463L;

    private Long id;
    private String name;
    private Boolean deleted;
    private Long lastModifiedUser;
    private Date creationDate;
    private Date lastModifiedDate;

    public SurveillanceTypeDTO() {

    }

    public SurveillanceTypeDTO(final SurveillanceTypeEntity entity) {
        BeanUtils.copyProperties(entity, this);
    }

    public SurveillanceTypeDTO(final SurveillanceType domain) {
        BeanUtils.copyProperties(domain, this);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "SurveillanceTypeDTO [id=" + id + ", name=" + name + ", deleted=" + deleted + ", lastModifiedUser="
                + lastModifiedUser + ", creationDate=" + creationDate + ", lastModifiedDate=" + lastModifiedDate + "]";
    }
}
