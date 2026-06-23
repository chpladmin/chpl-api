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

@Component
public class RealWorldTestingResultsSummaryReportDao extends BaseDAOImpl {

    public void save(RealWorldTestingSummaryByAcbReport realWorldTestingSummaryReport) throws EntityRetrievalException {
        RealWorldTestingResultsSummaryByAcbReportEntity entity = getEntityByCheckedDateAndAcb(realWorldTestingSummaryReport.getCheckedDate(),
                realWorldTestingSummaryReport.getCertificationBody().getId());
        if (entity == null) {
            entity = RealWorldTestingResultsSummaryByAcbReportEntity.builder()
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
                + "from RealWorldTestingResultsSummaryByAcbReportEntity rwtrsr "
                + "where (NOT deleted = true)", Long.class)
                .getSingleResult());

    }

    public List<RealWorldTestingSummaryByAcbReport> getRealWorldTestingSummaryByAcbReportsByTestingYear(Long realWorldTestingYear) {
        return getAcbSummaryEntitiesByRealWorldTestingYear(realWorldTestingYear).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private RealWorldTestingResultsSummaryByAcbReportEntity getEntityByCheckedDateAndAcb(LocalDate checkedDate, Long certificationBodyId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from RealWorldTestingResultsSummaryByAcbReportEntity rwtrs "
                + "where (NOT deleted = true) "
                + "and checkedDate = :checkedDate "
                + "and rwtrs.certificationBody.id = :certificationBodyId", RealWorldTestingResultsSummaryByAcbReportEntity.class);
        query.setParameter("checkedDate", checkedDate);
        query.setParameter("certificationBodyId", certificationBodyId);
        List<RealWorldTestingResultsSummaryByAcbReportEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate checked_date in real_world_testing_results_summary_by_acb_report table.");
        }

        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private List<RealWorldTestingResultsSummaryByAcbReportEntity> getAcbSummaryEntitiesByRealWorldTestingYear(Long testingYear) {
        return entityManager.createQuery(
                "from RealWorldTestingResultsSummaryByAcbReportEntity rwtrs "
                + "where (NOT deleted = true) "
                + "and rwtrs.realWorldTestingYear = :realWorldTestingYear", RealWorldTestingResultsSummaryByAcbReportEntity.class)
                .setParameter("realWorldTestingYear", testingYear)
                .getResultList();
    }

    public void save(RealWorldTestingSummaryByDeveloperReport realWorldTestingSummaryReport) throws EntityRetrievalException {
        RealWorldTestingResultsSummaryByDeveloperReportEntity entity = getEntityByCheckedDateAndDeveloper(realWorldTestingSummaryReport.getCheckedDate(),
                realWorldTestingSummaryReport.getDeveloperId());
        if (entity == null) {
            entity = RealWorldTestingResultsSummaryByDeveloperReportEntity.builder()
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
                + "from RealWorldTestingResultsSummaryByDeveloperReportEntity rwtrsr "
                + "where (NOT deleted = true)", Long.class)
                .getSingleResult());

    }

    public List<RealWorldTestingSummaryByDeveloperReport> getRealWorldTestingSummaryByDeveloperReportsByTestingYear(Long realWorldTestingYear) {
        return getDeveloperSummaryEntitiesByRealWorldTestingYear(realWorldTestingYear).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private RealWorldTestingResultsSummaryByDeveloperReportEntity getEntityByCheckedDateAndDeveloper(LocalDate checkedDate, Long developerId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from RealWorldTestingResultsSummaryByDeveloperReportEntity rwtrs "
                + "JOIN FETCH rwtrs.developer dev "
                + "where (NOT rwtrs.deleted = true) "
                + "and checkedDate = :checkedDate "
                + "and dev.id = :developerId", RealWorldTestingResultsSummaryByDeveloperReportEntity.class);
        query.setParameter("checkedDate", checkedDate);
        query.setParameter("developerId", developerId);
        List<RealWorldTestingResultsSummaryByDeveloperReportEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate checked_date in real_world_testing_results_by_devleoper_summary_report table.");
        }

        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private List<RealWorldTestingResultsSummaryByDeveloperReportEntity> getDeveloperSummaryEntitiesByRealWorldTestingYear(Long testingYear) {
        return entityManager.createQuery(
                "from RealWorldTestingResultsSummaryByDeveloperReportEntity rwtrs "
                + "JOIN FETCH rwtrs.developer "
                + "where (NOT rwtrs.deleted = true) "
                + "and rwtrs.realWorldTestingYear = :realWorldTestingYear", RealWorldTestingResultsSummaryByDeveloperReportEntity.class)
                .setParameter("realWorldTestingYear", testingYear)
                .getResultList();
    }

}
