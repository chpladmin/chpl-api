package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ApiKeyActivityEntity;
import gov.healthit.chpl.util.Util;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiKeyActivityDTO implements Serializable {
    private static final long serialVersionUID = 8932636865845455436L;
    private Long id;
    private Long apiKeyId;
    private String apiCallPath;
    private String apiCallMethod;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public ApiKeyActivityDTO(ApiKeyActivityEntity entity) {

        this.id = entity.getId();
        this.apiKeyId = entity.getApiKeyId();
        this.apiCallPath = entity.getApiCallPath();
        this.apiCallMethod = entity.getApiCallMethod();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();
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
}
