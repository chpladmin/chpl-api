package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CriteriaMigrationReportDAO extends BaseDAOImpl {

    public void create(CriteriaMigrationCount criteriaMigrationCount) {
        Optional<CriteriaMigrationCountEntity> entity = getCriteriaMigrationCountEntityByDefinitionAndReportDate(
                criteriaMigrationCount.getCriteriaMigrationDefinition().getId(), criteriaMigrationCount.getReportDate());

        if (entity.isPresent()) {
            softDeleteCriteriaMigrationCountEntity(entity.get());
        }

        create(CriteriaMigrationCountEntity.builder()
                .criteriaMigrationDefinitionId(criteriaMigrationCount.getCriteriaMigrationDefinition().getId())
                .reportDate(criteriaMigrationCount.getReportDate())
                .originalCriterionCount(criteriaMigrationCount.getOriginalCriterionCount())
                .updatedCriterionCount(criteriaMigrationCount.getUpdatedCriterionCount())
                .originalToUpdatedCriterionCount(criteriaMigrationCount.getOriginalToUpdatedCriterionCount())
                .build());
    }

    public CriteriaMigrationReport getCriteriaMigrationReport(Long criteriaMigrationReportId) {
        CriteriaMigrationReportEntity report = getCriteriaMigrationReportEntity(criteriaMigrationReportId);

        report.setCriteriaMigrationDefinitions(getCriteriaMigrationDefinitionEntities(criteriaMigrationReportId));

        report.getCriteriaMigrationDefinitions()
                .forEach(def -> def.setCriteriaMigrationCounts(getCriteriaMigrationCountEntity(def.getId())));

        return report.toDomain();
    }

    public Optional<CriteriaMigrationCount> getCriteriaMigrationCount(Long criteriaMigrationDefinitionId, LocalDate reportDate) {
        Optional<CriteriaMigrationCountEntity> cmc =  getCriteriaMigrationCountEntityByDefinitionAndReportDate(criteriaMigrationDefinitionId, reportDate);
        if (cmc.isPresent()) {
            return Optional.of(cmc.get().toDomain());
        } else {
            return Optional.empty();
        }
    }

    public CriteriaMigrationReport getCriteriaMigrationReportWithoutCounts(Long criteriaMigrationReportId) {
        CriteriaMigrationReportEntity report = getCriteriaMigrationReportEntity(criteriaMigrationReportId);

        report.setCriteriaMigrationDefinitions(getCriteriaMigrationDefinitionEntities(criteriaMigrationReportId));

        return report.toDomain();
    }

    public List<CriteriaMigrationReport> getAllCriteriaMigrationReportsWithoutCounts() {
        List<CriteriaMigrationReportEntity> reports = getAllCriteriaMigrationReportEntities();

        reports.forEach(report -> {
            report.setCriteriaMigrationDefinitions(getCriteriaMigrationDefinitionEntities(report.getId()));
        });

        return reports.stream()
                .map(rpt -> rpt.toDomain())
                .toList();
    }

    private CriteriaMigrationReportEntity getCriteriaMigrationReportEntity(Long criteriaMigrationReportId) {
        Query query = entityManager.createQuery(
                "select distinct cmr "
                + "from CriteriaMigrationReportEntity cmr "
                + "where cmr.deleted = false "
                + "and cmr.id = :criteriaMigrationReportId ", CriteriaMigrationReportEntity.class);
        query.setParameter("criteriaMigrationReportId", criteriaMigrationReportId);

        @SuppressWarnings("unchecked")
        List<CriteriaMigrationReportEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            LOGGER.error("Could not retrieve Criteria Migration Report with id: {}", criteriaMigrationReportId);
            return null;
        } else {
            return result.get(0);
        }
    }

    private List<CriteriaMigrationReportEntity> getAllCriteriaMigrationReportEntities() {
        Query query = entityManager.createQuery(
                "select distinct cmr "
                + "from CriteriaMigrationReportEntity cmr "
                + "where cmr.deleted = false ", CriteriaMigrationReportEntity.class);

        return query.getResultList();
    }

    private List<CriteriaMigrationDefinitionEntity> getCriteriaMigrationDefinitionEntities(Long criteriaMigrationReportId) {
        Query query = entityManager.createQuery(
                "select distinct cmd "
                + "from CriteriaMigrationDefinitionEntity cmd "
                + "left join fetch cmd.originalCriterion oc "
                + "left join fetch oc.certificationEdition "
                + "left join fetch oc.rule "
                + "left join fetch cmd.updatedCriterion uc "
                + "left join fetch uc.certificationEdition "
                + "left join fetch uc.rule "
                + "where cmd.deleted = false "
                + "and cmd.criteriaMigrationReportId = :criteriaMigrationReportId ", CriteriaMigrationDefinitionEntity.class);
        query.setParameter("criteriaMigrationReportId", criteriaMigrationReportId);

        @SuppressWarnings("unchecked")
        List<CriteriaMigrationDefinitionEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            LOGGER.error("Could not retrieve Criteria Migration Definition with id: {}", criteriaMigrationReportId);
            return null;
        } else {
            return result;
        }
    }

    private List<CriteriaMigrationCountEntity> getCriteriaMigrationCountEntity(Long criteriaMigrationDefinitionId) {
        Query query = entityManager.createQuery(
                "select distinct cmc "
                + "from CriteriaMigrationCountEntity cmc "
                + "where cmc.deleted = false "
                + "and cmc.criteriaMigrationDefinitionId = :criteriaMigrationDefinitionId ", CriteriaMigrationCountEntity.class);
        query.setParameter("criteriaMigrationDefinitionId", criteriaMigrationDefinitionId);

        @SuppressWarnings("unchecked")
        List<CriteriaMigrationCountEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            LOGGER.error("Could not retrieve Criteria Migration Counts with id: {}", criteriaMigrationDefinitionId);
            return null;
        } else {
            return result;
        }
    }

    private Optional<CriteriaMigrationCountEntity> getCriteriaMigrationCountEntityByDefinitionAndReportDate(Long criteriaMigrationDefinitionId, LocalDate reportDate) {
        Query query = entityManager.createQuery(
                "select distinct cmc "
                + "from CriteriaMigrationCountEntity cmc "
                + "where cmc.deleted = false "
                + "and cmc.criteriaMigrationDefinitionId = :criteriaMigrationDefinitionId "
                + "and cmc.reportDate = :reportDate ", CriteriaMigrationCountEntity.class);
        query.setParameter("criteriaMigrationDefinitionId", criteriaMigrationDefinitionId);
        query.setParameter("reportDate", reportDate);

        @SuppressWarnings("unchecked")
        List<CriteriaMigrationCountEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(result.get(0));
        }
    }

    private void softDeleteCriteriaMigrationCountEntity(CriteriaMigrationCountEntity entity) {
        entity.setDeleted(true);
        update(entity);
    }
}
