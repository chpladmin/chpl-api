package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ApiKeyEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ApiKeyDTO implements Serializable {
    private static final long serialVersionUID = 7091753452944248313L;
    private Long id;
    private String apiKey;
    private String email;
    private String nameOrganization;
    private Boolean unrestricted;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;
    private Date lastUsedDate;
    private Date deleteWarningSentDate;

    public ApiKeyDTO(ApiKeyEntity entity) {
        this.id = entity.getId();
        this.apiKey = entity.getApiKey();
        this.email = entity.getEmail();
        this.nameOrganization = entity.getNameOrganization();
        this.unrestricted = entity.getUnrestricted();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();
        this.setLastUsedDate(entity.getLastUsedDate());
        this.setDeleteWarningSentDate(entity.getDeleteWarningSentDate());
    }
}
