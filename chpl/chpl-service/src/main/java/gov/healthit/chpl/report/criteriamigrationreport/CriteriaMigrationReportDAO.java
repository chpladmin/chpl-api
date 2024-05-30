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
        return getCriteriaMigrationReportEntity(criteriaMigrationReportId).toDomain();
    }

    private CriteriaMigrationReportEntity getCriteriaMigrationReportEntity(Long criteriaMigrationReportId) {
        Query query = entityManager.createQuery(
                "select cmr "
                + "from CriteriaMigrationReportEntity cmr "
                + "left join fetch crm.criteriaMigrationDefinitions cmd "
                + "left join fetch cmd.originalCriterion oc "
                + "left join fetch cmd.updatedCriterion uc "
                + "left join fetch cmd.criteriaMigrationCounts cmc "
                + "where cmr.deleted = false "
                + "and cmd.deleted = false "
                + "and cmc.deleted = false "
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
}
