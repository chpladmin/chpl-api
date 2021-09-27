package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.statistics.CuresListingStatistic;
import gov.healthit.chpl.entity.statistics.CuresListingStatisticEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository
public class CuresListingStatisticsDAO extends BaseDAOImpl {
    public LocalDate getDateOfMostRecentStatistics() {
        LocalDate result = null;
        Query query = entityManager.createQuery("SELECT max(statisticDate) "
                + "FROM CuresListingStatisticEntity stats "
                + "WHERE (stats.deleted = false) ",
                LocalDate.class);
        Object queryResult = query.getSingleResult();
        if (queryResult instanceof LocalDate) {
            result = (LocalDate) queryResult;
        }
        return result;
    }

    public void delete(Long id) throws EntityRetrievalException {
        CuresListingStatisticEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            update(toDelete);
        }
    }

    public List<CuresListingStatistic> getStatisticsForDate(LocalDate statisticDate) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CuresListingStatisticEntity stats "
                + "WHERE (stats.deleted = false) "
                + "AND stats.statisticDate = :statisticDate ",
                CuresListingStatisticEntity.class);
        query.setParameter("statisticDate", statisticDate);
        List<CuresListingStatisticEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> new CuresListingStatistic(entity))
                .collect(Collectors.toList());
    }

    public void create(List<CuresListingStatistic> domains) {
        domains.stream()
                .forEach(domain -> create(domain));

    }

    public void create(CuresListingStatistic domain) {
        CuresListingStatisticEntity entity = new CuresListingStatisticEntity(domain);
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);

        create(entity);
    }

    private CuresListingStatisticEntity getEntityById(Long id) throws EntityRetrievalException {
        CuresListingStatisticEntity entity = null;
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CuresListingStatisticEntity stats "
                + "WHERE stats.deleted = false "
                + "AND stats.id = :id",
                CuresListingStatisticEntity.class);
        query.setParameter("id", id);
        List<CuresListingStatisticEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }
        return entity;
    }

}
