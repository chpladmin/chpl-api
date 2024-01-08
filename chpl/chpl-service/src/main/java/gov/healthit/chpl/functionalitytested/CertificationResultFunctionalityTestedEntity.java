package gov.healthit.chpl.functionalitytested;

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
                .functionalityTestedId(this.getFunctionalityTested() != null ? this.getFunctionalityTested().getId() : null)
                .name(this.getFunctionalityTested() != null ? this.getFunctionalityTested().getRegulatoryTextCitation() : null)
                .description(this.getFunctionalityTested() != null ? this.getFunctionalityTested().getValue() : null)
                .build();
    }
}
