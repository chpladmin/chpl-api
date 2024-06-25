package gov.healthit.chpl.functionalitytested;

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
@Table(name = "certification_result_functionality_tested")
public class CertificationResultFunctionalityTestedEntity extends EntityAudit {
    private static final long serialVersionUID = -6544851759049221578L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long certificationResultId;

    @Column(name = "functionality_tested_id")
    private Long functionalityTestedId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "functionality_tested_id", unique = true, nullable = true, insertable = false, updatable = false)
    private FunctionalityTestedEntity functionalityTested;

    public CertificationResultFunctionalityTested toDomain() {
        return CertificationResultFunctionalityTested.builder()
                .id(this.getId())
                .certificationResultId(certificationResultId)
                .functionalityTested(this.functionalityTested.toDomain())
                .creationDate(getCreationDate())
                .build();
    }
}
