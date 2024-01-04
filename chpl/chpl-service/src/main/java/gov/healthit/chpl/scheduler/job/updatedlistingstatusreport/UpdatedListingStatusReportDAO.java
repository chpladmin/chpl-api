package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class UpdatedListingStatusReportDAO extends BaseDAOImpl {
    public UpdatedListingStatusReport create(UpdatedListingStatusReport ulsr) {
        UpdatedListingStatusReportEntity entity = UpdatedListingStatusReportEntity.builder()
                .certifiedProductId(ulsr.getCertifiedProductId())
                .criteriaRequireUpdateCount(ulsr.getCriteriaRequireUpdateCount())
                .daysUpdatedEarly(ulsr.getDaysUpdatedEarly())
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }
}
