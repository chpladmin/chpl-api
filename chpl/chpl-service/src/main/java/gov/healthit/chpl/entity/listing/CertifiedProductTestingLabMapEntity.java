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

import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.TestingLabEntity;
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
@Table(name = "certified_product_testing_lab_map")
public class CertifiedProductTestingLabMapEntity extends EntityAudit {
    private static final long serialVersionUID = 5430701152773885608L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;

    @Basic(optional = false)
    @Column(name = "testing_lab_id", nullable = false)
    private Long testingLabId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "testing_lab_id", unique = true, nullable = true, insertable = false, updatable = false)
    private TestingLabEntity testingLab;

    public CertifiedProductTestingLab toDomain() {
        return CertifiedProductTestingLab.builder()
                .id(id)
                .testingLabId(testingLabId)
                .testingLabName(testingLab.getName())
                .testingLabCode(testingLab.getTestingLabCode())
                .testingLab(testingLab.toDomain())
                .build();
    }
}
