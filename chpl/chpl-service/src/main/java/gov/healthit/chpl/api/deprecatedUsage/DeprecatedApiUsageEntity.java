package gov.healthit.chpl.api.deprecatedUsage;

import java.time.LocalDate;
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

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "api_operation")
    private String apiOperation;

    @Column(name = "response_field")
    private String responseField;

    @Column(name = "removal_date")
    private LocalDate removalDate;

    @Column(name = "message")
    private String message;

    @Column(name = "api_call_count")
    private Long apiCallCount;

    @Column(name = "last_accessed_date")
    private Date lastAccessedDate;

    @Column(name = "notification_sent")
    private Date notificationSent;

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
                .apiKey(this.getApiKey() != null ? this.getApiKey().toDomain() : null)
                .httpMethod(this.getHttpMethod())
                .apiOperation(this.getApiOperation())
                .responseField(this.getResponseField())
                .removalDate(this.getRemovalDate())
                .message(this.getMessage())
                .callCount(this.getApiCallCount())
                .lastAccessedDate(this.getLastAccessedDate())
                .notificationSent(this.getNotificationSent())
                .build();
    }
}
