package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ApiKeyEntity;

public class ApiKeyDTO implements Serializable {
    private static final long serialVersionUID = 7091753452944248313L;
    private Long id;
    private String apiKey;
    private String email;
    private String nameOrganization;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public ApiKeyDTO() {
    }

    public ApiKeyDTO(ApiKeyEntity entity) {

        this.id = entity.getId();
        this.apiKey = entity.getApiKey();
        this.email = entity.getEmail();
        this.nameOrganization = entity.getNameOrganization();
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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getNameOrganization() {
        return nameOrganization;
    }

    public void setNameOrganization(final String nameOrganization) {
        this.nameOrganization = nameOrganization;
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
