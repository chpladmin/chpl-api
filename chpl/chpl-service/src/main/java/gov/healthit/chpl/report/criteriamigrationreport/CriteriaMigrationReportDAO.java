package gov.healthit.chpl.report.criteriamigrationreport;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CriteriaMigrationReportDAO extends BaseDAOImpl {

    public CriteriaMigrationReport getCriteriaMigrationReport(Long criteriaMigrationReportId) {
        //return getCriteriaMigrationReportEntity(criteriaMigrationReportId).toDomain();
        CriteriaMigrationReportEntity report = getCriteriaMigrationReportEntity(criteriaMigrationReportId);

        report.setCriteriaMigrationDefinitions(getCriteriaMigrationDefinitionEntities(criteriaMigrationReportId));

        report.getCriteriaMigrationDefinitions()
                .forEach(def -> def.setCriteriaMigrationCounts(getCriteriaMigrationCountEntity(def.getId())));

        return report.toDomain();

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

}
