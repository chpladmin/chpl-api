package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;
import java.util.List;

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

    public List<UpdatedListingStatusReport> getUpdatedListingStatusReportsByDate(LocalDate reportDate) {
        return getUpdatedListingStatusReportEntitiessByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
    }

    public void deleteUpdatedListingStatusReportsByDate(LocalDate reportDate) {
        getUpdatedListingStatusReportEntitiessByDate(reportDate).stream()
                .forEach(ent -> {
                    ent.setDeleted(true);
                    update(ent);
                });
    }

    private List<UpdatedListingStatusReportEntity> getUpdatedListingStatusReportEntitiessByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ulsr "
                            + "FROM UpdatedListingStatusReportEntity ulsr "
                            + "WHERE (NOT ulsr.deleted = true) "
                            + "AND ulsr.reportDay = :reportDate", UpdatedListingStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

}
