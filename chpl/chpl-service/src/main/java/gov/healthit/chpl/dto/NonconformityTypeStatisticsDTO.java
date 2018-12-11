package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.surveillance.NonconformityTypeStatisticsEntity;
import gov.healthit.chpl.util.Util;

public class NonconformityTypeStatisticsDTO {

    private Long id;
    private Long nonconformityCount;
    private String nonconformityType;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public NonconformityTypeStatisticsDTO(NonconformityTypeStatisticsEntity entity) {
        this.nonconformityCount = entity.getNonconformityCount();
        this.nonconformityType = entity.getNonconformityType();
        this.id = entity.getId();
        this.setCreationDate(entity.getCreationDate());
        this.setDeleted(entity.getDeleted());
        this.setLastModifiedUser(entity.getLastModifiedUser());
        this.setLastModifiedDate(entity.getLastModifiedDate());
    }

    public NonconformityTypeStatisticsDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNonconformityCount() {
        return nonconformityCount;
    }

    public void setNonconformityCount(Long nonconformityCount) {
        this.nonconformityCount = nonconformityCount;
    }

    public String getNonconformityType() {
        return nonconformityType;
    }

    public void setNonconformityType(String nonconformityType) {
        this.nonconformityType = nonconformityType;
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

    public void setDeleted(Boolean deleted) {
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

    public void setLastModifiedUser(Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }
}
