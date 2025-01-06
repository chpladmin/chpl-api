package gov.healthit.chpl.report.realworldtesting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;

@Component
public class RealWorldTestingResultsSummaryReportDao extends BaseDAOImpl {

    public void save(RealWorldTestingSummaryReport realWorldTestingSummaryReport) throws EntityRetrievalException {
        RealWorldTestingResultsSummaryReportEntity entity = getEntityByCheckedDateAndAcb(realWorldTestingSummaryReport.getCheckedDate(),
                realWorldTestingSummaryReport.getCertificationBody().getId());
        if (entity == null) {
            entity = RealWorldTestingResultsSummaryReportEntity.builder()
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

    public Optional<Long> getMaxRealWorldTestingYear() {
        return Optional.ofNullable(entityManager.createQuery(
                "select MAX(rwtrsr.realWorldTestingYear) "
                + "from RealWorldTestingResultsSummaryReportEntity rwtrsr "
                + "where (NOT deleted = true)", Long.class)
                .getSingleResult());

    }

    public List<RealWorldTestingSummaryReport> getRealWorldTestingReportsByTestingYear(Long realWorldTestingYear) {
        return getEntitiesByRealWorldTestingYear(realWorldTestingYear).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private RealWorldTestingResultsSummaryReportEntity getEntity(Long id) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from RealWorldTestingResultsSummaryReportEntity where (NOT deleted = true) and id = :id", RealWorldTestingResultsSummaryReportEntity.class);
        query.setParameter("id", id);
        List<RealWorldTestingResultsSummaryReportEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate id in real_world_testing_results_summary_report table.");
        }

        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private RealWorldTestingResultsSummaryReportEntity getEntityByCheckedDateAndAcb(LocalDate checkedDate, Long certificationBodyId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "from RealWorldTestingResultsSummaryReportEntity rwtrs "
                + "where (NOT deleted = true) "
                + "and checkedDate = :checkedDate "
                + "and rwtrs.certificationBody.id = :certificationBodyId", RealWorldTestingResultsSummaryReportEntity.class);
        query.setParameter("checkedDate", checkedDate);
        query.setParameter("certificationBodyId", certificationBodyId);
        List<RealWorldTestingResultsSummaryReportEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate checked_date in real_world_testing_results_summary_report table.");
        }

        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    private List<RealWorldTestingResultsSummaryReportEntity> getEntitiesByRealWorldTestingYear(Long testingYear){
        return entityManager.createQuery(
                "from RealWorldTestingResultsSummaryReportEntity rwtrs "
                + "where (NOT deleted = true) "
                + "and rwtrs.realWorldTestingYear = :realWorldTestingYear", RealWorldTestingResultsSummaryReportEntity.class)
                .setParameter("realWorldTestingYear", testingYear)
                .getResultList();
    }

}
