package gov.healthit.chpl.entity.listing;

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
                .testingLab(testingLab.toDomain())
                .build();
    }
}
