package gov.healthit.chpl.api.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.util.Util;
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
@Table(name = "api_key")
public class ApiKeyEntity extends EntityAudit {
    private static final long serialVersionUID = -2162141254926731903L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "api_key_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "api_key")
    private String apiKey;

    @Basic(optional = false)
    @Column(name = "email")
    private String email;

    @Basic(optional = false)
    @Column(name = "name_organization")
    private String nameOrganization;

    @Basic(optional = false)
    @Column(name = "unrestricted", nullable = false, insertable = false)
    private Boolean unrestricted;

    @Column(name = "last_used_date", nullable = false)
    private Date lastUsedDate;

    @Column(name = "delete_warning_sent_date", nullable = true)
    private Date deleteWarningSentDate;

    public Date getLastUsedDate() {
        return Util.getNewDate(lastUsedDate);
    }

    public void setLastUsedDate(final Date lastUsedDate) {
        this.lastUsedDate = Util.getNewDate(lastUsedDate);
    }

    public Date getDeleteWarningSentDate() {
        return Util.getNewDate(deleteWarningSentDate);
    }

    public void setDeleteWarningSentDate(final Date deleteWarningSentDate) {
        this.deleteWarningSentDate = Util.getNewDate(deleteWarningSentDate);
    }

    public ApiKey toDomain() {
        return ApiKey.builder()
                .id(this.getId())
                .email(this.getEmail())
                .key(this.getApiKey())
                .name(this.getNameOrganization())
                .unrestricted(this.getUnrestricted())
                .lastUsedDate(this.getLastUsedDate())
                .deleteWarningSentDate(this.getDeleteWarningSentDate())
                .build();
    }
}
