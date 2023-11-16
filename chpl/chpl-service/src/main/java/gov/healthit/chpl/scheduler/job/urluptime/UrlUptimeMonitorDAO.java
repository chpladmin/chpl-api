package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class UrlUptimeMonitorDAO extends BaseDAOImpl {

    public List<UrlUptimeMonitor> getAll() {
        return getEntitiesAll().stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public UrlUptimeMonitor create(UrlUptimeMonitor datadogMonitor) throws EntityRetrievalException {
        UrlUptimeMonitorEntity entity = UrlUptimeMonitorEntity.builder()
                .developer(getSimpleDeveloperById(datadogMonitor.getDeveloper().getId(), false))
                .url(datadogMonitor.getUrl())
                .lastModifiedUser(User.SYSTEM_USER_ID)
                .deleted(false)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }

    public void delete(UrlUptimeMonitor datadogMonitor) {
        UrlUptimeMonitorEntity entity = getEntityById(datadogMonitor.getId());
        if (entity != null) {
            entity.setDeleted(true);
            entity.setLastModifiedUser(User.SYSTEM_USER_ID);
            entityManager.merge(entity);
            entityManager.flush();
        }
    }

    private UrlUptimeMonitorEntity getEntityById(Long id) {
        UrlUptimeMonitorEntity entity = null;

        Query query = entityManager.createQuery(
                "FROM UrlUptimeMonitorEntity "
                + "WHERE (NOT deleted = true) "
                + "AND id = :id", UrlUptimeMonitorEntity.class);
        query.setParameter("id", id);
        List<UrlUptimeMonitorEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<UrlUptimeMonitorEntity> getEntitiesAll() {
        return entityManager.createQuery(
                "FROM UrlUptimeMonitorEntity uume "
                + "WHERE (uume.deleted = false)", UrlUptimeMonitorEntity.class)
                .getResultList();
    }

    private DeveloperEntity getDeveloperEntityById(Long id) throws EntityRetrievalException {
        String queryStr = "SELECT v "
                + "FROM DeveloperEntity v "
                + "WHERE v.id = :entityid "
                + "AND v.deleted = false";
        Query query = entityManager.createQuery(queryStr, DeveloperEntity.class);
        query.setParameter("entityid", id);

        LOGGER.info("id = {}", id);
        LOGGER.info(getEntityManager().createQuery(queryStr).unwrap(org.hibernate.query.Query.class).getQueryString());

        @SuppressWarnings("unchecked")
        List<DeveloperEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("developer.notFound");
            throw new EntityRetrievalException(msg);
        }
        return result.get(0);
    }

    private DeveloperEntitySimple getSimpleDeveloperById(Long id, boolean includeDeleted) throws EntityRetrievalException {
        String queryStr = "SELECT DISTINCT de "
                + "FROM DeveloperEntitySimple de "
                + "WHERE de.id = :entityid ";
        if (!includeDeleted) {
            queryStr += " AND de.deleted = false";
        }

        Query query = entityManager.createQuery(queryStr, DeveloperEntitySimple.class);
        query.setParameter("entityid", id);
        List<DeveloperEntitySimple> entities = query.getResultList();

        if (entities == null || entities.size() == 0) {
            String msg = msgUtil.getMessage("developer.notFound");
            throw new EntityRetrievalException(msg);
        } else if (entities.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate developer id in database.");
        }
        return entities.get(0);

    }

}
