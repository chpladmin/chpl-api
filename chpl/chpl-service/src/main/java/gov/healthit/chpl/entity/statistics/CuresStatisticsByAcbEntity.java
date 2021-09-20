package gov.healthit.chpl.entity.statistics;

import java.time.LocalDate;
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

import gov.healthit.chpl.domain.statistics.CuresStatisticsByAcb;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Data;

@Entity
@Data
@Table(name = "cures_statistics_by_acb")
public class CuresStatisticsByAcbEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_body_id", insertable = true, updatable = true)
    private CertificationBodyEntity certificationBody;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "original_criterion_id", insertable = true, updatable = true)
    private CertificationCriterionEntity originalCriterion;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cures_criterion_id", insertable = true, updatable = true)
    private CertificationCriterionEntity curesCriterion;

    @Basic(optional = false)
    @Column(name = "original_criterion_upgraded_count", nullable = false)
    private Long originalCriterionUpgradedCount;

    @Basic(optional = false)
    @Column(name = "cures_criterion_created_count", nullable = false)
    private Long curesCriterionCreatedCount;

    @Basic(optional = false)
    @Column(name = "statistic_date", nullable = false)
    private LocalDate statisticDate;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public CuresStatisticsByAcbEntity(CuresStatisticsByAcb domain) {
        this.id = domain.getId();
        this.certificationBody = CertificationBodyEntity.getNewAcbEntity(domain.getCertificationBody());
        this.originalCriterion = CertificationCriterionEntity.getNewCertificationCriterionEntity(domain.getOriginalCriterion());
        this.curesCriterion = CertificationCriterionEntity.getNewCertificationCriterionEntity(domain.getCuresCriterion());
        this.originalCriterionUpgradedCount = domain.getOriginalCriterionUpgradedCount();
        this.curesCriterionCreatedCount = domain.getCuresCriterionCreatedCount();
        this.statisticDate = domain.getStatisticDate();
        this.creationDate = domain.getCreationDate();
        this.deleted = domain.getDeleted();
        this.lastModifiedDate = domain.getLastModifiedDate();
        this.lastModifiedUser = domain.getLastModifiedUser();
    }
}
