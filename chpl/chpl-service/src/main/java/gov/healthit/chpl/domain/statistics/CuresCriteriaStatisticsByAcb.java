package gov.healthit.chpl.domain.statistics;

import java.time.LocalDate;
import java.util.Date;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.entity.statistics.CuresCriteriaStatisticsByAcbEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CuresCriteriaStatisticsByAcb {
    private Long id;
    private CertificationBody certificationBody;
    private CertificationCriterion originalCriterion;
    private CertificationCriterion curesCriterion;
    private Long originalCriterionUpgradedCount;
    private Long curesCriterionCreatedCount;
    private Long criteriaNeedingUpgradeCount;
    private LocalDate statisticDate;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public CuresCriteriaStatisticsByAcb(CuresCriteriaStatisticsByAcbEntity entity) {
        this.id = entity.getId();
        this.certificationBody = entity.getCertificationBody().toDomain();
        this.originalCriterion = entity.getOriginalCriterion().toDomain();
        this.curesCriterion = entity.getCuresCriterion().toDomain();
        this.originalCriterionUpgradedCount = entity.getOriginalCriterionUpgradedCount();
        this.curesCriterionCreatedCount = entity.getCuresCriterionCreatedCount();
        this.criteriaNeedingUpgradeCount = entity.getCriteriaNeedingUpgradeCount();
        this.statisticDate = entity.getStatisticDate();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }
}
