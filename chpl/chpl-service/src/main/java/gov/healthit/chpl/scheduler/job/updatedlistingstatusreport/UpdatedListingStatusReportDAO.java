package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class UpdatedListingStatusReportDAO extends BaseDAOImpl {
    public UpdatedListingStatusReport create(UpdatedListingStatusReport ulsr) {
        UpdatedListingStatusReportEntity entity = UpdatedListingStatusReportEntity.builder()
                .certifiedProductId(ulsr.getCertifiedProductId())
                .reportDay(LocalDate.now())
                .criteriaRequireUpdateCount(ulsr.getCriteriaRequireUpdateCount())
                .daysUpdatedEarly(ulsr.getDaysUpdatedEarly())
                .chplProductNumber(ulsr.getChplProductNumber())
                .product(ulsr.getProduct())
                .version(ulsr.getVersion())
                .developer(ulsr.getDeveloper())
                .certificationBody(ulsr.getCertificationBody())
                .certificationStatus(ulsr.getCertificationStatus())
                .developerId(ulsr.getDeveloperId())
                .certificationBodyId(ulsr.getCertificationBodyId())
                .certificationStatusId(ulsr.getCertificationStatusId())
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }
}
