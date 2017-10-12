package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.MeaningfulUseAccurateAsOf;
import gov.healthit.chpl.entity.MeaningfulUseAccurateAsOfEntity;

public class MeaningfulUseAccurateAsOfDTO implements Serializable {
    private static final long serialVersionUID = 2215818722889786140L;

    private Long id;
    private Date accurateAsOfDate;
    private Boolean deleted;
    private Long lastModifiedUser;
    private Date creationDate;
    private Date lastModifiedDate;

    public MeaningfulUseAccurateAsOfDTO() {
    };

    public MeaningfulUseAccurateAsOfDTO(MeaningfulUseAccurateAsOfEntity muuEntity) {
        this.id = muuEntity.getId();
        this.accurateAsOfDate = muuEntity.getAccurateAsOfDate();
        this.deleted = muuEntity.getDeleted();
        this.lastModifiedUser = muuEntity.getLastModifiedUser();
        this.creationDate = muuEntity.getCreationDate();
        this.lastModifiedDate = muuEntity.getLastModifiedDate();
    };

    public MeaningfulUseAccurateAsOfDTO(MeaningfulUseAccurateAsOf meaningfulUseAccurateAsOf) {
        this.id = meaningfulUseAccurateAsOf.getId();
        this.accurateAsOfDate = new Date(meaningfulUseAccurateAsOf.getAccurateAsOfDate());
        this.deleted = meaningfulUseAccurateAsOf.getDeleted();
        this.lastModifiedUser = meaningfulUseAccurateAsOf.getLastModifiedUser();
        this.creationDate = new Date(meaningfulUseAccurateAsOf.getCreationDate());
        this.lastModifiedDate = new Date(meaningfulUseAccurateAsOf.getLastModifiedDate());
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getAccurateAsOfDate() {
        return accurateAsOfDate;
    }

    public void setAccurateAsOfDate(final Date accurateAsOfDate) {
        this.accurateAsOfDate = accurateAsOfDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
