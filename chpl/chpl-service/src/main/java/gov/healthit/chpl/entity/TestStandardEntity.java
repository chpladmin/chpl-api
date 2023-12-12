package gov.healthit.chpl.entity;

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

import gov.healthit.chpl.domain.TestStandard;
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
@Table(name = "test_standard")
public class TestStandardEntity extends EntityAudit {
    private static final long serialVersionUID = -2614142241543331045L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "test_standard_id")
    private Long id;

    @Column(name = "number", nullable = false)
    private String name;

    @Column(name = "name", nullable = false)
    private String description;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity certificationEdition;

    public TestStandard toDomain() {
        return TestStandard.builder()
                .id(getId())
                .description(getDescription())
                .name(getName())
                .year(getCertificationEdition().getYear())
                .build();
    }
}
