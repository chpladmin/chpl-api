package gov.healthit.chpl.dao.statistics;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.SummaryStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("summaryStatisticsDAO")
public class SummaryStatisticsDAOImpl extends BaseDAOImpl implements SummaryStatisticsDAO {

    @Override
    @Transactional
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
    public SummaryStatisticsEntity getMostRecent() throws EntityRetrievalException {
        List<SummaryStatisticsEntity> entities = entityManager
                .createQuery("SELECT stats "
                            + "FROM SummaryStatisticsEntity stats "
                            + "WHERE (stats.deleted <> true) "
                            + "ORDER BY stats.id DESC "
                            + "LIMIT 1",
                            SummaryStatisticsEntity.class)
                .getResultList();

        if (entities.size() > 0) {
            return entities.get(0);
        } else {
            return null;
        }
    }
}
