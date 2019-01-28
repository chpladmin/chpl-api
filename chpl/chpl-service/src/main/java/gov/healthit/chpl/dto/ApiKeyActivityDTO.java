package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ApiKeyActivityEntity;
import gov.healthit.chpl.util.Util;

public class ApiKeyActivityDTO implements Serializable {
    private static final long serialVersionUID = 8932636865845455436L;
    private Long id;
    private Long apiKeyId;
    private String apiCallPath;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public ApiKeyActivityDTO() {
    }

    public ApiKeyActivityDTO(ApiKeyActivityEntity entity) {

        this.id = entity.getId();
        this.apiKeyId = entity.getApiKeyId();
        this.apiCallPath = entity.getApiCallPath();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();

    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getApiKeyId() {
        return apiKeyId;
    }

    public void setApiKeyId(final Long apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    public String getApiCallPath() {
        return apiCallPath;
    }

    public void setApiCallPath(final String apiCallPath) {
        this.apiCallPath = apiCallPath;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

}
