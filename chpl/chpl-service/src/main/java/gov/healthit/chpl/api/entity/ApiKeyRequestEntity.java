package gov.healthit.chpl.api.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.api.domain.ApiKeyRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "api_key_request")
public class ApiKeyRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "email")
    private String email;

    @Basic(optional = false)
    @Column(name = "name_organization")
    private String nameOrganization;

    @Basic(optional = false)
    @Column(name = "api_request_token")
    private String apiRequestToken;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public ApiKeyRequestEntity(ApiKeyRequest request) {
        this.id = request.getId();
        this.email = request.getEmail();
        this.nameOrganization = request.getNameOrganization();
        this.apiRequestToken = request.getApiRequestToken();
        this.creationDate = request.getCreationDate();
        this.lastModifiedDate = request.getLastModifiedDate();
        this.lastModifiedUser = request.getLastModifiedUser();
        this.deleted = request.getDeleted();
    }

}
