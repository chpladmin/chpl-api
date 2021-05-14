package gov.healthit.chpl.api.domain;

import java.util.Date;

import gov.healthit.chpl.api.entity.ApiKeyRequestEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiKeyRequest {
    private Long id;
    private String email;
    private String nameOrganization;
    private String apiRequestToken;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public ApiKeyRequest(ApiKeyRequestEntity entity) {
        this.id = entity.getId();
        this.email = entity.getEmail();
        this.nameOrganization = entity.getNameOrganization();
        this.apiRequestToken = entity.getApiRequestToken();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();
    }
}
