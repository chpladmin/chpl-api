package gov.healthit.chpl.api.deprecatedUsage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Where;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import lombok.Data;

@Entity
@Data
@Table(name = "deprecated_response_field_api")
public class DeprecatedResponseFieldApiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "api_operation")
    private String apiOperation;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "deprecatedApiId")
    @Basic(optional = false)
    @Column(name = "deprecated_api_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<DeprecatedResponseFieldEntity> responseFields = new HashSet<DeprecatedResponseFieldEntity>();

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

    public DeprecatedResponseFieldApi toDomain() {
        List<DeprecatedResponseField> deprecatedFields = new ArrayList<DeprecatedResponseField>();
        if (!CollectionUtils.isEmpty(this.getResponseFields())) {
            this.getResponseFields().stream()
                .forEach(rf -> deprecatedFields.add(rf.toDomain()));
        }

        return DeprecatedResponseFieldApi.builder()
                .id(this.getId())
                .api(ApiOperation.builder()
                        .httpMethod(HttpMethod.valueOf(this.getHttpMethod().toUpperCase()))
                        .apiOperation(this.getApiOperation())
                        .build())
                .responseFields(deprecatedFields)
                .build();
    }
}
