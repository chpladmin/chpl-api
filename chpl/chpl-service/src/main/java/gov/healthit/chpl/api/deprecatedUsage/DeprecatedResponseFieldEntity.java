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

import lombok.Data;

@Entity
@Data
@Table(name = "deprecated_response_field")
public class DeprecatedResponseFieldEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "deprecated_api_id")
    private Long deprecatedApiId;

    @Column(name = "response_field")
    private String responseField;

    @Column(name = "removal_date")
    private LocalDate removalDate;

    @Column(name = "change_description")
    private String changeDescription;

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

    public DeprecatedResponseField toDomain() {
        return DeprecatedResponseField.builder()
                .id(this.getId())
                .responseField(this.getResponseField())
                .changeDescription(this.getChangeDescription())
                .removalDate(this.getRemovalDate())
                .build();
    }
}
