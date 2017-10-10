package gov.healthit.chpl.domain;

import java.io.Serializable;

import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;

public class MeaningfulUseAccurateAsOf implements Serializable {
    private static final long serialVersionUID = -4803763243075068608L;

    private Long id;
    private Long accurateAsOfDate;
    private Boolean deleted;
    private Long lastModifiedUser;
    private Long creationDate;
    private Long lastModifiedDate;

    public MeaningfulUseAccurateAsOf() {
    };

    public MeaningfulUseAccurateAsOf(MeaningfulUseAccurateAsOfDTO muuDTO) {
        this.id = muuDTO.getId();
        this.accurateAsOfDate = muuDTO.getAccurateAsOfDate().getTime();
        this.deleted = muuDTO.getDeleted();
        this.lastModifiedUser = muuDTO.getLastModifiedUser();
        this.creationDate = muuDTO.getCreationDate().getTime();
        this.lastModifiedDate = muuDTO.getLastModifiedDate().getTime();
    };

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getAccurateAsOfDate() {
        return accurateAsOfDate;
    }

    public void setAccurateAsOfDate(final Long accurateAsOfDate) {
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

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Long creationDate) {
        this.creationDate = creationDate;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

}
