package gov.healthit.chpl.standard;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "certification_result_standard")
public class CertificationResultStandardEntity extends EntityAudit {
    private static final long serialVersionUID = 6724520660214522655L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "standard_id")
    private Long standardId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id", unique = true, nullable = true, insertable = false, updatable = false)
    private StandardEntity standard;

    public CertificationResultStandard toDomain() {
        return CertificationResultStandard.builder()
                .id(this.getId())
                .certificationResultId(certificationResultId)
                .standard(this.standard.toDomain())
                .creationDate(getCreationDate())
                .build();
    }

}
