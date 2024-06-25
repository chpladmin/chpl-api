package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class UpdatedCriteriaStatusReportDAO extends BaseDAOImpl {
    public UpdatedCriteriaStatusReport create(UpdatedCriteriaStatusReport ucsr) {
        UpdatedCriteriaStatusReportEntity entity = UpdatedCriteriaStatusReportEntity.builder()
                .certificationCriterionId(ucsr.getCertificationCriterionId())
                .reportDay(LocalDate.now())
                .listingsWithCriterionCount(ucsr.getListingsWithCriterionCount())
                .fullyUpToDateCount(ucsr.getFullyUpToDateCount())
                .functionalitiesTestedUpToDateCount(ucsr.getFunctionalitiesTestedUpToDateCount())
                .standardsUpToDateCount(ucsr.getStandardsUpToDateCount())
                .codeSetsUpToDateCount(ucsr.getCodeSetsUpToDateCount())
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }

    public List<UpdatedCriteriaStatusReport> getUpdatedCriteriaStatusReportsByDate(LocalDate reportDate) {
        return getUpdatedCriteriaStatusReportEntitiessByDate(reportDate).stream()
                .map(ent -> ent.toDomain())
                .toList();
    }

    public void deleteUpdatedCriteriaStatusReportsByDate(LocalDate reportDate) {
        getUpdatedCriteriaStatusReportEntitiessByDate(reportDate).stream()
                .forEach(ent -> {
                    ent.setDeleted(true);
                    update(ent);
                });
    }

    public LocalDate getMaxReportDate() {
        return entityManager
                .createQuery("SELECT MAX(reportDay) "
                            + "FROM UpdatedCriteriaStatusReportEntity ucsr "
                            + "WHERE (NOT ucsr.deleted = true) ", LocalDate.class)
                .getSingleResult();
    }


    public List<Long> getCriteriaIdsFromUpdatedCritieriaStatusReport() {
        return entityManager
                .createQuery("SELECT DISTINCT ucsr.certificationCriterionId "
                            + "FROM UpdatedCriteriaStatusReportEntity ucsr "
                            + "WHERE (NOT ucsr.deleted = true) ", Long.class)
                .getResultList();
    }


    private List<UpdatedCriteriaStatusReportEntity> getUpdatedCriteriaStatusReportEntitiessByDate(LocalDate reportDate) {
        return entityManager
                .createQuery("SELECT ucsr "
                            + "FROM UpdatedCriteriaStatusReportEntity ucsr "
                            + "WHERE (NOT ucsr.deleted = true) "
                            + "AND ucsr.reportDay = :reportDate", UpdatedCriteriaStatusReportEntity.class)
                .setParameter("reportDate", reportDate)
                .getResultList();
    }

}
