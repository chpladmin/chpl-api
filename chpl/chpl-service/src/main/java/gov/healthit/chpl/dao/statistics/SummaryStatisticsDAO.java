package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("summaryStatisticsDAO")
public class SummaryStatisticsDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(SummaryStatisticsDAO.class);

    public SummaryStatisticsEntity create(SummaryStatisticsEntity summaryStatistics)
            throws EntityCreationException, EntityRetrievalException {

        summaryStatistics.setCreationDate(new Date());
        summaryStatistics.setLastModifiedDate(new Date());
        summaryStatistics.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        summaryStatistics.setDeleted(false);
        entityManager.persist(summaryStatistics);
        entityManager.flush();

        return summaryStatistics;
    }

    public SummaryStatisticsEntity getCurrentSummaryStatistics() throws EntityRetrievalException {
        Query currStatQuery = entityManager.createQuery("SELECT stats "
                + "FROM SummaryStatisticsEntity stats "
                + "WHERE (stats.deleted <> true) "
                + "ORDER BY stats.id DESC", SummaryStatisticsEntity.class);
        currStatQuery.setMaxResults(1);
        List<SummaryStatisticsEntity> entities = currStatQuery.getResultList();

        if (entities.size() > 0) {
            return entities.get(0);
        } else {
            return null;
        }
    }

    public SummaryStatisticsEntity getSummaryStatistics(LocalDate asOf) throws EntityRetrievalException {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM SummaryStatisticsEntity stats "
                + "WHERE MONTH(stats.endDate) = :month "
                + "AND DAY(stats.endDate) = :day "
                + "AND YEAR(stats.endDate) = :year "
                + "AND deleted = false", SummaryStatisticsEntity.class);

        query.setParameter("month", asOf.getMonthValue());
        query.setParameter("day", asOf.getDayOfMonth());
        query.setParameter("year", asOf.getYear());
        List<SummaryStatisticsEntity> entities = query.getResultList();

        if (entities.size() > 0) {
            return entities.get(0);
        } else {
            return null;
        }
    }
}
