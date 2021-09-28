package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.statistics.CuresListingStatisticByAcb;
import gov.healthit.chpl.entity.statistics.CuresListingStatisticByAcbEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository
public class CuresListingStatisticsByAcbDAO extends BaseDAOImpl {
    public LocalDate getDateOfMostRecentStatistics() {
        LocalDate result = null;
        Query query = entityManager.createQuery("SELECT max(statisticDate) "
                + "FROM CuresListingStatisticByAcbEntity stats "
                + "WHERE (stats.deleted = false) ",
                LocalDate.class);
        Object queryResult = query.getSingleResult();
        if (queryResult instanceof LocalDate) {
            result = (LocalDate) queryResult;
        }
        return result;
    }

    public void delete(Long id) throws EntityRetrievalException {
        CuresListingStatisticByAcbEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            update(toDelete);
        }
    }

    public List<CuresListingStatisticByAcb> getStatisticsForDate(LocalDate statisticDate) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CuresListingStatisticByAcbEntity stats "
                + "JOIN FETCH stats.certificationBody cb "
                + "LEFT OUTER JOIN FETCH cb.address "
                + "WHERE (stats.deleted = false) "
                + "AND stats.statisticDate = :statisticDate ",
                CuresListingStatisticByAcbEntity.class);
        query.setParameter("statisticDate", statisticDate);
        List<CuresListingStatisticByAcbEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> new CuresListingStatisticByAcb(entity))
                .collect(Collectors.toList());
    }

    public void create(List<CuresListingStatisticByAcb> domains) {
        domains.stream()
                .forEach(domain -> create(domain));

    }

    public void create(CuresListingStatisticByAcb domain) {
        CuresListingStatisticByAcbEntity entity = new CuresListingStatisticByAcbEntity(domain);
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);

        create(entity);
    }

    private CuresListingStatisticByAcbEntity getEntityById(Long id) throws EntityRetrievalException {
        CuresListingStatisticByAcbEntity entity = null;
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CuresListingStatisticByAcbEntity stats "
                + "JOIN FETCH stats.certificationBody cb "
                + "LEFT OUTER JOIN FETCH cb.address "
                + "WHERE stats.deleted = false "
                + "AND stats.id = :id",
                CuresListingStatisticByAcbEntity.class);
        query.setParameter("id", id);
        List<CuresListingStatisticByAcbEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }
        return entity;
    }

}
