package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ActivityConceptEntity;

public class ActivityConceptDTO implements Serializable {
    private static final long serialVersionUID = 2774054409084343411L;
    private Long id;
    private String className;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public ActivityConceptDTO() {
    }

    public ActivityConceptDTO(ActivityConceptEntity entity) {
        this.id = entity.getId();
        this.className = entity.getClassName();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

}
