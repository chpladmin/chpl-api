package gov.healthit.chpl.api.deprecatedUsage;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.api.entity.ApiKeyEntity;
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
@Table(name = "deprecated_api_usage")
public class DeprecatedApiUsageEntity extends EntityAudit {
    private static final long serialVersionUID = -3667640013723601658L;

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
