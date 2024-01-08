package gov.healthit.chpl.api.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.api.domain.ApiKeyRequest;
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "api_key_request")
public class ApiKeyRequestEntity extends EntityAudit {
    private static final long serialVersionUID = -3116775426247103741L;

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

    public ApiKeyRequestEntity(ApiKeyRequest request) {
        this.id = request.getId();
        this.email = request.getEmail();
        this.nameOrganization = request.getNameOrganization();
        this.apiRequestToken = request.getApiRequestToken();
        this.setCreationDate(request.getCreationDate());
        this.setLastModifiedDate(request.getLastModifiedDate());
        this.setLastModifiedUser(request.getLastModifiedUser());
        this.setDeleted(request.getDeleted());
    }

}
