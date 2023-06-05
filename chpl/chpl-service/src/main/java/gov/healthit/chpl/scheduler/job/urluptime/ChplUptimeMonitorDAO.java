package gov.healthit.chpl.scheduler.job.urluptime;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository
public class ChplUptimeMonitorDAO extends BaseDAOImpl {
//    public List<ChplUptimeMonitor> getAll() {
//        return getEntitiesAll().stream()
//                .map(entity -> entity.toDomain())
//                .toList();
//    }
//
//    public ChplUptimeMonitor create(ChplUptimeMonitor chplUptimeMonitor) {
//        ChplUptimeMonitorEntity entity = ChplUptimeMonitorEntity.builder()
//                .description(chplUptimeMonitor.getDescription())
//                .url(chplUptimeMonitor.getUrl())
//                .datadogMonitorKey(chplUptimeMonitor.getDatadogMonitorKey())
//                .creationDate(new Date())
//                .lastModifiedUser(User.SYSTEM_USER_ID)
//                .lastModifiedDate(new Date())
//                .deleted(false)
//                .build();
//
//        entityManager.persist(entity);
//        entityManager.flush();
//        return entity.toDomain();
//    }
//
//    public void delete(ChplUptimeMonitor chplUptimeMonitor) {
//        ChplUptimeMonitorEntity entity = getEntityById(chplUptimeMonitor.getId());
//        if (entity != null) {
//            entity.setDeleted(true);
//            entity.setLastModifiedUser(User.SYSTEM_USER_ID);
//            entityManager.merge(entity);
//            entityManager.flush();
//        }
//    }
//
//    private ChplUptimeMonitorEntity getEntityById(Long id) {
//        ChplUptimeMonitorEntity entity = null;
//
//        Query query = entityManager.createQuery(
//                "from ChplUptimeMonitorEntity where (NOT deleted = true) and id = :id", ChplUptimeMonitorEntity.class);
//        query.setParameter("id", id);
//        List<ChplUptimeMonitorEntity> result = query.getResultList();
//        if (result.size() > 0) {
//            entity = result.get(0);
//        }
//
//        return entity;
//    }
//
//    private List<ChplUptimeMonitorEntity> getEntitiesAll() {
//        return entityManager.createQuery("FROM ChplUptimeMonitorEntity cume "
//                + "WHERE (cume.deleted = false)", ChplUptimeMonitorEntity.class)
//                .getResultList();
//    }
}
