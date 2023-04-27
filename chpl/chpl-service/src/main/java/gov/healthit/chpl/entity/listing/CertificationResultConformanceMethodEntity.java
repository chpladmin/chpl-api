package gov.healthit.chpl.entity.listing;

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

import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodEntity;
import lombok.Data;

@Entity
@Data
@Table(name = "certification_result_conformance_method")
public class CertificationResultConformanceMethodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "conformance_method_id")
    private Long conformanceMethodId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "conformance_method_id", unique = true, nullable = true, insertable = false, updatable = false)
    private ConformanceMethodEntity conformanceMethod;

    @Basic(optional = false)
    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(nullable = false, insertable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public CertificationResultConformanceMethod toDomain() {
        return CertificationResultConformanceMethod.builder()
                .id(this.getId())
                .conformanceMethod(this.getConformanceMethod().toDomain())
                .conformanceMethodVersion(this.getVersion())
                .build();
    }
}
