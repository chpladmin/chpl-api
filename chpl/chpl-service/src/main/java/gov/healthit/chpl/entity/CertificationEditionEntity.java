package gov.healthit.chpl.entity;

import java.util.HashSet;
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

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.domain.CertificationEdition;
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
@Table(name = "certification_edition")
public class CertificationEditionEntity extends EntityAudit {
    private static final long serialVersionUID = -365316096272783095L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_edition_id", nullable = false)
    private Long id;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "certificationEditionId")
    @Basic(optional = false)
    @Column(name = "certification_edition_id", nullable = false)
    private Set<CertificationCriterionEntity> certificationCriterions = new HashSet<CertificationCriterionEntity>();

    @Basic(optional = true)
    @Column(name = "year", length = 10)
    private String year;

    @Basic(optional = false)
    @Column(name = "retired", length = 10)
    private Boolean retired;

    public CertificationEdition toDomain() {
        return CertificationEdition.builder()
                .certificationEditionId(getId())
                .id(getId())
                .retired(getRetired())
                .year(getYear())
                .name(getYear())
                .build();
    }
}
