package gov.healthit.chpl.entity.statistics;

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
import jakarta.persistence.Transient;

import gov.healthit.chpl.entity.CertificationEditionEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.CurrentUserThenSystemUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
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
@Table(name = "incumbent_developers_statistics")
public class IncumbentDevelopersStatisticsEntity extends EntityAudit {
    private static final long serialVersionUID = 1313677047965534572L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new CurrentUserThenSystemUserStrategy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "new_count", nullable = false)
    private Long newCount;

    @Basic(optional = false)
    @Column(name = "incumbent_count", nullable = false)
    private Long incumbentCount;

    @Basic(optional = false)
    @Column(name = "old_certification_edition_id", nullable = false)
    private Long oldCertificationEditionId;

    @Basic(optional = false)
    @Column(name = "new_certification_edition_id", nullable = false)
    private Long newCertificationEditionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "old_certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity oldCertificationEdition;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "new_certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity newCertificationEdition;

    public IncumbentDevelopersStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return IncumbentDevelopersStatisticsEntity.class;
    }
}
