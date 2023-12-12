package gov.healthit.chpl.entity.listing;

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
@Table(name = "certification_result_conformance_method")
public class CertificationResultConformanceMethodEntity extends EntityAudit {
    private static final long serialVersionUID = -3129172074380514255L;

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

    public CertificationResultConformanceMethod toDomain() {
        return CertificationResultConformanceMethod.builder()
                .id(this.getId())
                .conformanceMethod(this.getConformanceMethod().toDomain())
                .conformanceMethodVersion(this.getVersion())
                .build();
    }
}
