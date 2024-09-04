package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.entity.AnnouncementEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Repository(value = "announcementDAO")
@Log4j2
public class AnnouncementDAO extends BaseDAOImpl {

    public Announcement create(Announcement announcement) throws EntityRetrievalException, EntityCreationException {
        AnnouncementEntity entity = new AnnouncementEntity();
        entity.setTitle(announcement.getTitle());
        entity.setText(announcement.getText());
        entity.setStartDate(announcement.getStartDateTime());
        entity.setEndDate(announcement.getEndDateTime());
        entity.setIsPublic(announcement.getIsPublic());

        create(entity);
        return entity.toDomain();
    }

    public Announcement update(Announcement announcement, boolean includeDeleted) throws EntityRetrievalException {
        AnnouncementEntity entity = getEntityById(announcement.getId(), includeDeleted);
        if (entity == null) {
            throw new EntityRetrievalException(
                    "Cannot update entity with id " + announcement.getId() + ". Entity does not exist.");
        }

        entity.setText(announcement.getText());
        entity.setTitle(announcement.getTitle());
        entity.setStartDate(announcement.getStartDateTime());
        entity.setEndDate(announcement.getEndDateTime());
        entity.setIsPublic(announcement.getIsPublic());
        update(entity);
        return entity.toDomain();
    }

    public void delete(Long id) {
        AnnouncementEntity announcementToDelete = null;
        try {
            announcementToDelete = getEntityById(id, false);
        } catch (EntityRetrievalException ex) {
            LOGGER.warn("Attempted to delete announcement with ID " + id + " that does not exist.");
        }
        if (announcementToDelete != null) {
            announcementToDelete.setDeleted(true);
            update(announcementToDelete);
        }
    }

    public List<Announcement> findCurrent() {
        List<AnnouncementEntity> entities = getAllCurrentEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<Announcement> findAllCurrentAndFuture() {
        List<AnnouncementEntity> entities = getAllEntitiesCurrentAndFuture();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<Announcement> findAllFuture() {
        List<AnnouncementEntity> entities = getAllEntitiesFuture();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<Announcement> findAll(boolean includeDeleted, boolean includePrivate) {
        String hql = "SELECT a "
                + "FROM AnnouncementEntity a ";
        if (!includePrivate) {
            hql += " WHERE a.isPublic = true ";
        }
        if (!includeDeleted) {
            if (hql.contains("WHERE")) {
                hql += " AND ";
            } else {
                hql += " WHERE ";
            }
            hql += " a.deleted = false";
        }

        Query query = entityManager.createQuery(hql);
        List<AnnouncementEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public Announcement getById(Long announcementId, boolean includeDeleted) throws EntityRetrievalException {
        AnnouncementEntity entity = getEntityById(announcementId, includeDeleted);
        Announcement announcement = null;
        if (entity != null) {
            announcement = entity.toDomain();
        }
        return announcement;
    }

    private List<AnnouncementEntity> getAllCurrentEntities() {
        Query query = entityManager.createQuery(
                "FROM AnnouncementEntity "
                        + "WHERE deleted = false "
                        + "AND startDate <= local_datetime "
                        + "AND endDate > local_datetime ",
                AnnouncementEntity.class);
        return query.getResultList();
    }

    private List<AnnouncementEntity> getAllEntitiesFuture() {
        Query query = entityManager.createQuery("FROM AnnouncementEntity "
                        + "WHERE deleted = false "
                        + "AND startDate > local_datetime");
        return query.getResultList();
    }

    private List<AnnouncementEntity> getAllEntitiesCurrentAndFuture() {
        Query query = entityManager.createQuery("FROM AnnouncementEntity "
                        + "WHERE deleted = false "
                        + "AND endDate > local_datetime");
        return query.getResultList();
    }

    private AnnouncementEntity getEntityById(Long entityId, boolean includeDeleted)
            throws EntityRetrievalException {
        List<AnnouncementEntity> results = null;
        AnnouncementEntity entity = null;
        String hql = "SELECT a "
                + "FROM AnnouncementEntity a "
                + "WHERE (a.id = :entityid) ";
        if (!includeDeleted) {
            hql += " AND a.deleted = false ";
        }
        Query query = entityManager.createQuery(hql, AnnouncementEntity.class);
        query.setParameter("entityid", entityId);
        results = query.getResultList();
        if (results == null || results.size() == 0) {
            String msg = msgUtil.getMessage("announcement.notFound");
            throw new EntityRetrievalException(msg);
        } else {
            entity = results.get(0);
        }
        return entity;
    }
}
