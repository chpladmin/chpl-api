package gov.healthit.chpl.dao.statistics;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("summaryStatisticsDAO")
public class SummaryStatisticsDAOImpl extends BaseDAOImpl implements SummaryStatisticsDAO {
    private static final Logger LOGGER = LogManager.getLogger(SummaryStatisticsDAOImpl.class);

    @Override
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

    @Override
    public SummaryStatisticsEntity getCurrentSummaryStatistics() throws EntityRetrievalException {
        List<SummaryStatisticsEntity> entities = entityManager
                .createQuery("SELECT stats " + "FROM SummaryStatisticsEntity stats " + "WHERE (stats.deleted <> true) "
                        + "ORDER BY stats.id DESC " + "LIMIT 1", SummaryStatisticsEntity.class)
                .getResultList();

        if (entities.size() > 0) {
            return entities.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void deleteAll() {
        try {
            List<SummaryStatisticsEntity> entities = getAllSummaryStatistics();
            if (entities != null) {
                for (SummaryStatisticsEntity entity : entities) {
                    entity.setDeleted(true);
                    entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
                    entityManager.merge(entity);
                    entityManager.flush();
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    private List<SummaryStatisticsEntity> getAllSummaryStatistics() throws EntityRetrievalException {
        List<SummaryStatisticsEntity> entities = entityManager
                .createQuery("SELECT stats " + "FROM SummaryStatisticsEntity stats " + "WHERE (stats.deleted <> true)",
                        SummaryStatisticsEntity.class)
                .getResultList();

        return entities;
    }

}
