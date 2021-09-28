package gov.healthit.chpl.domain.statistics;

import java.time.LocalDate;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.entity.statistics.CuresListingStatisticByAcbEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CuresListingStatisticByAcb {
    private Long id;
    private CertificationBody certificationBody;
    private Long curesListingWithoutCuresCriteriaCount;
    private Long curesListingWithCuresCriteriaCount;
    private Long nonCuresListingCount;
    private LocalDate statisticDate;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public CuresListingStatisticByAcb(CuresListingStatisticByAcbEntity entity) {
        this.id = entity.getId();
        this.certificationBody = new CertificationBody(entity.getCertificationBody());
        this.curesListingWithoutCuresCriteriaCount = entity.getCuresListingWithoutCuresCriteriaCount();
        this.curesListingWithCuresCriteriaCount = entity.getCuresListingWithCuresCriteriaCount();
        this.nonCuresListingCount = entity.getNonCuresListingCount();
        this.statisticDate = entity.getStatisticDate();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }
}
