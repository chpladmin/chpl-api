package gov.healthit.chpl.api.deprecatedUsage;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.http.HttpMethod;

import lombok.Data;

@Entity
@Data
@Table(name = "deprecated_api")
public class DeprecatedApiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "api_operation")
    private String apiOperation;

    @Column(name = "request_parameter")
    private String requestParameter;

    @Column(name = "change_description")
    private String changeDescription;

    @Column(name = "removal_date")
    private LocalDate removalDate;

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

    public DeprecatedApi toDomain() {
        return DeprecatedApi.builder()
                .id(this.getId())
                .apiOperation(this.getApiOperation())
                .httpMethod(HttpMethod.valueOf(this.getHttpMethod().toUpperCase()))
                .requestParameter(this.getRequestParameter())
                .changeDescription(this.getChangeDescription())
                .removalDate(this.getRemovalDate())
                .build();
    }
}
