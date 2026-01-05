package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.statistics.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.StatisticsSnapshot;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Log4j2
@Repository("summaryStatisticsDAO")
public class SummaryStatisticsDAO extends BaseDAOImpl {

    public SummaryStatisticsEntity create(SummaryStatisticsEntity summaryStatistics)
            throws EntityCreationException, EntityRetrievalException {

        summaryStatistics.setDeleted(false);
        entityManager.persist(summaryStatistics);
        entityManager.flush();

        return summaryStatistics;
    }

    public StatisticsSnapshot getCurrentSummaryStatistics() {
        Query currStatQuery = entityManager.createQuery("SELECT stats "
                + "FROM SummaryStatisticsEntity stats "
                + "WHERE (stats.deleted <> true) "
                + "ORDER BY stats.id DESC", SummaryStatisticsEntity.class);
        currStatQuery.setMaxResults(1);
        List<SummaryStatisticsEntity> entities = currStatQuery.getResultList();

        if (entities.size() > 0) {
            return toSnapshot(entities.get(0));
        } else {
            return null;
        }
    }

    public StatisticsSnapshot getSummaryStatistics(LocalDate asOf) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM SummaryStatisticsEntity stats "
                + "WHERE MONTH(stats.endDate) = :month "
                + "AND DAY(stats.endDate) = :day "
                + "AND YEAR(stats.endDate) = :year "
                + "AND deleted = false "
                + "ORDER BY stats.endDate DESC", SummaryStatisticsEntity.class);

        query.setParameter("month", asOf.getMonthValue());
        query.setParameter("day", asOf.getDayOfMonth());
        query.setParameter("year", asOf.getYear());
        List<SummaryStatisticsEntity> entities = query.getResultList();

        if (entities.size() > 0) {
            return toSnapshot(entities.get(0));
        } else {
            return null;
        }
    }

    private StatisticsSnapshot toSnapshot(SummaryStatisticsEntity entity) {
        if (StringUtils.isEmpty(entity.getSummaryStatistics())) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            StatisticsSnapshot snapshot = mapper.readValue(entity.getSummaryStatistics(), StatisticsSnapshot.class);
            snapshot.setSnapshotDate(entity.getEndDate());
            return snapshot;
        } catch (JacksonException ex) {
            LOGGER.error("Unable to convert SummaryStatisticsEntity JSON into StatisticsSnapshot java object.", ex);
            return null;
        }
    }
}
