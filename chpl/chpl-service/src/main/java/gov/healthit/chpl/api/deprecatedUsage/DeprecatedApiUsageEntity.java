package gov.healthit.chpl.api.deprecatedUsage;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.api.entity.ApiKeyEntity;
import lombok.Data;

@Entity
@Data
@Table(name = "deprecated_api_usage")
public class DeprecatedApiUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "api_key_id")
    private Long apiKeyId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id", insertable = false, updatable = false)
    @Where(clause = " deleted = false ")
    private ApiKeyEntity apiKey;

    @Column(name = "deprecated_api_id")
    private Long deprecatedApiId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "deprecated_api_id", insertable = false, updatable = false)
    @Where(clause = " deleted = false ")
    private DeprecatedApiEntity deprecatedApi;

    @Column(name = "api_call_count")
    private Long apiCallCount;

    @Column(name = "last_accessed_date")
    private Date lastAccessedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    public DeprecatedApiUsage toDomain() {
        return DeprecatedApiUsage.builder()
                .id(this.getId())
                .api(this.getDeprecatedApi() != null ? this.getDeprecatedApi().toDomain() : null)
                .apiKey(this.getApiKey() != null ? this.getApiKey().toDomain() : null)
                .build();
    }
}
