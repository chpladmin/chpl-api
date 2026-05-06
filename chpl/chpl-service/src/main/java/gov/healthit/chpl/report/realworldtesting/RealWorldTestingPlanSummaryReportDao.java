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
public class RealWorldTestingPlanSummaryReportDao extends BaseDAOImpl {

    public void save(RealWorldTestingSummaryByAcbReport realWorldTestingSummaryReport) throws EntityRetrievalException {
        RealWorldTestingPlanSummaryByAcbReportEntity entity = getEntityByCheckedDateAndAcb(realWorldTestingSummaryReport.getCheckedDate(),
                realWorldTestingSummaryReport.getCertificationBody().getId());
        if (entity == null) {
            entity = RealWorldTestingPlanSummaryByAcbReportEntity.builder()
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
                "select MAX(rwtpsr.realWorldTestingYear) "
                + "from RealWorldTestingPlanSummaryByAcbReportEntity rwtpsr "
                + "where (NOT deleted = true)", Long.class)
                .getSingleResult());

    }

    public List<RealWorldTestingSummaryByAcbReport> getRealWorldTestingSummaryByAcbReportsByTestingYear(Long realWorldTestingYear) {
        return getAcbSummaryEntitiesByRealWorldTestingYear(realWorldTestingYear).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private RealWorldTestingPlanSummaryByAcbReportEntity getEntityByCheckedDateAndAcb(LocalDate checkedDate, Long certificationBodyId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from RealWorldTestingPlanSummaryByAcbReportEntity rwtps "
                + "where (NOT deleted = true) "
                + "and checkedDate = :checkedDate "
                + "and rwtps.certificationBody.id = :certificationBodyId", RealWorldTestingPlanSummaryByAcbReportEntity.class);
        query.setParameter("checkedDate", checkedDate);
        query.setParameter("certificationBodyId", certificationBodyId);
        List<RealWorldTestingPlanSummaryByAcbReportEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate checked_date in real_world_testing_plan_summary_by_acb_report table.");
        }

        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private List<RealWorldTestingPlanSummaryByAcbReportEntity> getAcbSummaryEntitiesByRealWorldTestingYear(Long testingYear) {
        return entityManager.createQuery(
                "from RealWorldTestingPlanSummaryByAcbReportEntity rwtps "
                + "where (NOT deleted = true) "
                + "and rwtps.realWorldTestingYear = :realWorldTestingYear", RealWorldTestingPlanSummaryByAcbReportEntity.class)
                .setParameter("realWorldTestingYear", testingYear)
                .getResultList();
    }

    public void save(RealWorldTestingSummaryByDeveloperReport realWorldTestingSummaryReport) throws EntityRetrievalException {
        RealWorldTestingPlanSummaryByDeveloperReportEntity entity = getEntityByCheckedDateAndDeveloper(realWorldTestingSummaryReport.getCheckedDate(),
                realWorldTestingSummaryReport.getDeveloperId());
        if (entity == null) {
            entity = RealWorldTestingPlanSummaryByDeveloperReportEntity.builder()
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
                "select MAX(rwtpsr.realWorldTestingYear) "
                + "from RealWorldTestingPlanSummaryByDeveloperReportEntity rwtpsr "
                + "where (NOT deleted = true)", Long.class)
                .getSingleResult());

    }

    public List<RealWorldTestingSummaryByDeveloperReport> getRealWorldTestingSummaryByDeveloperReportsByTestingYear(Long realWorldTestingYear) {
        return getDeveloperSummaryEntitiesByRealWorldTestingYear(realWorldTestingYear).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private RealWorldTestingPlanSummaryByDeveloperReportEntity getEntityByCheckedDateAndDeveloper(LocalDate checkedDate, Long developerId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from RealWorldTestingPlanSummaryByDeveloperReportEntity rwtps "
                + "JOIN FETCH rwtps.developer dev "
                + "where (NOT rwtps.deleted = true) "
                + "and rwtps.checkedDate = :checkedDate "
                + "and dev.id = :developerId", RealWorldTestingPlanSummaryByAcbReportEntity.class);
        query.setParameter("checkedDate", checkedDate);
        query.setParameter("developerId", developerId);
        List<RealWorldTestingPlanSummaryByDeveloperReportEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate checked_date in real_world_testing_plan_summary_by_developer_report table.");
        }

        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private List<RealWorldTestingPlanSummaryByDeveloperReportEntity> getDeveloperSummaryEntitiesByRealWorldTestingYear(Long testingYear) {
        return entityManager.createQuery(
                "from RealWorldTestingPlanSummaryByDeveloperReportEntity rwtps "
                + "JOIN FETCH rwtps.developer "
                + "where (NOT rwtps.deleted = true) "
                + "and rwtps.realWorldTestingYear = :realWorldTestingYear", RealWorldTestingPlanSummaryByDeveloperReportEntity.class)
                .setParameter("realWorldTestingYear", testingYear)
                .getResultList();
    }

}
