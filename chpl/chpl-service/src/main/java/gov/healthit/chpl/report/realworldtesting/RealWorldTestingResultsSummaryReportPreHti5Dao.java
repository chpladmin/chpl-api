package gov.healthit.chpl.report.realworldtesting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;

@Deprecated
@Component
public class RealWorldTestingResultsSummaryReportPreHti5Dao extends BaseDAOImpl {

    public void save(RealWorldTestingSummaryByAcbReportPreHti5 realWorldTestingSummaryReport) throws EntityRetrievalException {
        RealWorldTestingResultsSummaryByAcbReportPreHti5Entity entity = getEntityByCheckedDateAndAcb(realWorldTestingSummaryReport.getCheckedDate(),
                realWorldTestingSummaryReport.getCertificationBody().getId());
        if (entity == null) {
            entity = RealWorldTestingResultsSummaryByAcbReportPreHti5Entity.builder()
                    .realWorldTestingYear(realWorldTestingSummaryReport.getRealWorldTestingYear())
                    .certificationBody(CertificationBodyEntity.builder()
                            .id(realWorldTestingSummaryReport.getCertificationBody().getId())
                            .build())
                    .checkedDate(realWorldTestingSummaryReport.getCheckedDate())
                    .checkedCount(realWorldTestingSummaryReport.getCheckedCount())
                    .requiresCheckCount(realWorldTestingSummaryReport.getRequiresCheckCount())
                    .build();

            create(entity);
        } else {
            entity.setCheckedCount(realWorldTestingSummaryReport.getCheckedCount());
            entity.setRequiresCheckCount(realWorldTestingSummaryReport.getRequiresCheckCount());
            update(entity);
        }
    }

    public Optional<Long> getMaxRealWorldTestingYearForAcbSummary() {
        return Optional.ofNullable(entityManager.createQuery(
                "select MAX(rwtrsr.realWorldTestingYear) "
                + "from RealWorldTestingResultsSummaryByAcbReportPreHti5Entity rwtrsr "
                + "where (NOT deleted = true)", Long.class)
                .getSingleResult());

    }

    public List<RealWorldTestingSummaryByAcbReportPreHti5> getRealWorldTestingSummaryByAcbReportsByTestingYear(Long realWorldTestingYear) {
        return getAcbSummaryEntitiesByRealWorldTestingYear(realWorldTestingYear).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private RealWorldTestingResultsSummaryByAcbReportPreHti5Entity getEntityByCheckedDateAndAcb(LocalDate checkedDate, Long certificationBodyId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from RealWorldTestingResultsSummaryByAcbReportPreHti5Entity rwtrs "
                + "where (NOT deleted = true) "
                + "and checkedDate = :checkedDate "
                + "and rwtrs.certificationBody.id = :certificationBodyId", RealWorldTestingResultsSummaryByAcbReportPreHti5Entity.class);
        query.setParameter("checkedDate", checkedDate);
        query.setParameter("certificationBodyId", certificationBodyId);
        List<RealWorldTestingResultsSummaryByAcbReportPreHti5Entity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate checked_date in real_world_testing_results_summary_by_acb_report_pre_hti5 table.");
        }

        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private List<RealWorldTestingResultsSummaryByAcbReportPreHti5Entity> getAcbSummaryEntitiesByRealWorldTestingYear(Long testingYear) {
        return entityManager.createQuery(
                "from RealWorldTestingResultsSummaryByAcbReportPreHti5Entity rwtrs "
                + "where (NOT deleted = true) "
                + "and rwtrs.realWorldTestingYear = :realWorldTestingYear", RealWorldTestingResultsSummaryByAcbReportPreHti5Entity.class)
                .setParameter("realWorldTestingYear", testingYear)
                .getResultList();
    }

    public void save(RealWorldTestingSummaryByDeveloperReportPreHti5 realWorldTestingSummaryReport) throws EntityRetrievalException {
        RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity entity = getEntityByCheckedDateAndDeveloper(realWorldTestingSummaryReport.getCheckedDate(),
                realWorldTestingSummaryReport.getDeveloperId());
        if (entity == null) {
            entity = RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity.builder()
                    .realWorldTestingYear(realWorldTestingSummaryReport.getRealWorldTestingYear())
                    .developer(DeveloperEntitySimple.builder()
                            .id(realWorldTestingSummaryReport.getDeveloperId())
                            .build())
                    .checkedDate(realWorldTestingSummaryReport.getCheckedDate())
                    .checkedCount(realWorldTestingSummaryReport.getCheckedCount())
                    .requiresCheckCount(realWorldTestingSummaryReport.getRequiresCheckCount())
                    .build();

            create(entity);
        } else {
            entity.setCheckedCount(realWorldTestingSummaryReport.getCheckedCount());
            entity.setRequiresCheckCount(realWorldTestingSummaryReport.getRequiresCheckCount());
            update(entity);
        }
    }

    public Optional<Long> getMaxRealWorldTestingYearForDeveloperSummary() {
        return Optional.ofNullable(entityManager.createQuery(
                "select MAX(rwtrsr.realWorldTestingYear) "
                + "from RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity rwtrsr "
                + "where (NOT deleted = true)", Long.class)
                .getSingleResult());

    }

    public List<RealWorldTestingSummaryByDeveloperReportPreHti5> getRealWorldTestingSummaryByDeveloperReportsByTestingYear(Long realWorldTestingYear) {
        return getDeveloperSummaryEntitiesByRealWorldTestingYear(realWorldTestingYear).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity getEntityByCheckedDateAndDeveloper(LocalDate checkedDate, Long developerId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity rwtrs "
                + "JOIN FETCH rwtrs.developer dev "
                + "where (NOT rwtrs.deleted = true) "
                + "and checkedDate = :checkedDate "
                + "and dev.id = :developerId", RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity.class);
        query.setParameter("checkedDate", checkedDate);
        query.setParameter("developerId", developerId);
        List<RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate checked_date in real_world_testing_results_by_devleoper_summary_report_pre_hti5 table.");
        }

        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private List<RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity> getDeveloperSummaryEntitiesByRealWorldTestingYear(Long testingYear) {
        return entityManager.createQuery(
                "from RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity rwtrs "
                + "JOIN FETCH rwtrs.developer "
                + "where (NOT rwtrs.deleted = true) "
                + "and rwtrs.realWorldTestingYear = :realWorldTestingYear", RealWorldTestingResultsSummaryByDeveloperReportPreHti5Entity.class)
                .setParameter("realWorldTestingYear", testingYear)
                .getResultList();
    }

}
