package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class DatadogMonitorDAO extends BaseDAOImpl {

    public List<DatadogMonitor> getAll() {
        return getEntitiesAll().stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public DatadogMonitor create(DatadogMonitor datadogMonitor) throws EntityRetrievalException {
        DatadogMonitorEntity entity = DatadogMonitorEntity.builder()
                .developer(DeveloperEntity.builder()
                        .id(datadogMonitor.getDeveloper().getId())
                        .build())
                .url(datadogMonitor.getUrl())
                .datadogPublicId(datadogMonitor.getDatadogPublicId())
                .creationDate(new Date())
                .lastModifiedUser(User.SYSTEM_USER_ID)
                .lastModifiedDate(new Date())
                .deleted(false)
                .build();

        entityManager.persist(entity);
        entityManager.flush();
        return entity.toDomain();
    }

    public void delete(DatadogMonitor datadogMonitor) {
        DatadogMonitorEntity entity = getEntityById(datadogMonitor.getId());
        if (entity != null) {
            entity.setDeleted(true);
            entity.setLastModifiedUser(User.SYSTEM_USER_ID);
            entityManager.merge(entity);
            entityManager.flush();
        }
    }

    private DatadogMonitorEntity getEntityById(Long id) {
        DatadogMonitorEntity entity = null;

        Query query = entityManager.createQuery(
                "from DatadogMonitorEntity "
                + "where (NOT deleted = true) "
                + "and id = :id", DatadogMonitorEntity.class);
        query.setParameter("id", id);
        List<DatadogMonitorEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<DatadogMonitorEntity> getEntitiesAll() {
        return entityManager.createQuery("FROM DatadogMonitorEntity cume "
                + "WHERE (cume.deleted = false)", DatadogMonitorEntity.class)
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
}
