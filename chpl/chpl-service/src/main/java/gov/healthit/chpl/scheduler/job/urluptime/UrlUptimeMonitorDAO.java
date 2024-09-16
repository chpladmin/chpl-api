package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;
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
                .url(datadogMonitor.getDatadogPublicId())
                .developer(getSimpleDeveloperById(datadogMonitor.getDeveloper().getId(), false))
                .url(datadogMonitor.getUrl())
                .build();

        create(entity);
        return getEntityById(entity.getId()).toDomain();
    }

    public void delete(UrlUptimeMonitor datadogMonitor) {
        UrlUptimeMonitorEntity entity = getEntityById(datadogMonitor.getId());
        if (entity != null) {
            entity.setDeleted(true);
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
                + "JOIN FETCH uume.developer dev "
                + "WHERE (uume.deleted = false)", UrlUptimeMonitorEntity.class)
                .getResultList();
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
