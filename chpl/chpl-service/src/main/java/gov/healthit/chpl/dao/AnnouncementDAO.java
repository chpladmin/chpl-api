package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Announcement;
import gov.healthit.chpl.entity.AnnouncementEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.DateUtil;

@Repository(value = "announcementDAO")
public class AnnouncementDAO extends BaseDAOImpl {

    @Transactional
    public Announcement create(Announcement announcement) throws EntityRetrievalException, EntityCreationException {
        AnnouncementEntity entity = null;
        try {
            if (announcement.getId() != null) {
                entity = this.getEntityById(announcement.getId(), false);
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new AnnouncementEntity();

            if (announcement.getTitle() != null) {
                entity.setTitle(announcement.getTitle());
            }

            if (announcement.getText() != null) {
                entity.setText(announcement.getText());
            }

            if (announcement.getStartDateTime() != null) {
                entity.setStartDate(new Date(DateUtil.toEpochMillis(announcement.getStartDateTime())));
            }

            if (announcement.getEndDateTime() != null) {
                entity.setEndDate(new Date(DateUtil.toEpochMillis(announcement.getEndDateTime())));
            }

            if (announcement.getIsPublic() != null) {
                entity.setIsPublic(announcement.getIsPublic());
            } else {
                entity.setIsPublic(false);
            }

            if (announcement.getDeleted() != null) {
                entity.setDeleted(announcement.getDeleted());
            } else {
                entity.setDeleted(false);
            }

            if (announcement.getLastModifiedUser() != null) {
                entity.setLastModifiedUser(announcement.getLastModifiedUser());
            } else {
                entity.setLastModifiedUser(AuthUtil.getAuditId());
            }

            if (announcement.getLastModifiedDate() != null) {
                entity.setLastModifiedDate(announcement.getLastModifiedDate());
            } else {
                entity.setLastModifiedDate(new Date());
            }

            if (announcement.getCreationDate() != null) {
                entity.setCreationDate(announcement.getCreationDate());
            } else {
                entity.setCreationDate(new Date());
            }

            create(entity);
            return entity.toDomain();
        }
    }

    @Transactional
    public Announcement update(Announcement announcement, boolean includeDeleted) throws EntityRetrievalException {

        AnnouncementEntity entity = getEntityById(announcement.getId(), includeDeleted);
        if (entity == null) {
            throw new EntityRetrievalException(
                    "Cannot update entity with id " + announcement.getId() + ". Entity does not exist.");
        }

        if (announcement.getText() != null) {
            entity.setText(announcement.getText());
        }

        if (announcement.getTitle() != null) {
            entity.setTitle(announcement.getTitle());
        }

        if (announcement.getStartDateTime() != null) {
            entity.setStartDate(new Date(DateUtil.toEpochMillis(announcement.getStartDateTime())));
        }

        if (announcement.getEndDateTime() != null) {
            entity.setEndDate(new Date(DateUtil.toEpochMillis(announcement.getEndDateTime())));
        }

        if (announcement.getIsPublic() != null) {
            entity.setIsPublic(announcement.getIsPublic());
        } else {
            entity.setIsPublic(false);
        }

        if (announcement.getDeleted() != null) {
            entity.setDeleted(announcement.getDeleted());
        }
        if (announcement.getLastModifiedUser() != null) {
            entity.setLastModifiedUser(announcement.getLastModifiedUser());
        } else {
            entity.setLastModifiedUser(AuthUtil.getAuditId());
        }

        if (announcement.getLastModifiedDate() != null) {
            entity.setLastModifiedDate(announcement.getLastModifiedDate());
        } else {
            entity.setLastModifiedDate(new Date());
        }

        update(entity);
        return entity.toDomain();
    }

    @Transactional
    public void delete(Long aId) {

        // TODO: How to delete this without leaving orphans

        Query query = entityManager
                .createQuery("UPDATE AnnouncementEntity SET deleted = true WHERE announcement_id = :aid");
        query.setParameter("aid", aId);
        query.executeUpdate();

    }

    public List<Announcement> findCurrent() {

        List<AnnouncementEntity> entities = getAllCurrentEntities();
        List<Announcement> announcements = new ArrayList<>();

        for (AnnouncementEntity entity : entities) {
            Announcement announcement = entity.toDomain();
            announcements.add(announcement);
        }
        return announcements;

    }

    public List<Announcement> findAllCurrentAndFuture() {

        List<AnnouncementEntity> entities = getAllEntitiesCurrentAndFuture();
        List<Announcement> announcements = new ArrayList<>();

        for (AnnouncementEntity entity : entities) {
            Announcement announcement = entity.toDomain();
            announcements.add(announcement);
        }
        return announcements;

    }

    public List<Announcement> findAllFuture() {

        List<AnnouncementEntity> entities = getAllEntitiesFuture();
        List<Announcement> announcements = new ArrayList<>();

        for (AnnouncementEntity entity : entities) {
            Announcement announcement = entity.toDomain();
            announcements.add(announcement);
        }
        return announcements;

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
        List<Announcement> announcements = new ArrayList<>();
        entities.stream().forEach(entity -> announcements.add(entity.toDomain()));
        return announcements;
    }

    public Announcement getById(Long announcementId, boolean includeDeleted) throws EntityRetrievalException {
        AnnouncementEntity entity = getEntityById(announcementId, includeDeleted);

        Announcement announcement = null;
        if (entity != null) {
            announcement = entity.toDomain();
        }
        return announcement;

    }

    public Announcement getByIdToUpdate(Long announcementId, boolean includeDeleted)
            throws EntityRetrievalException {
        AnnouncementEntity entity = getEntityById(announcementId, includeDeleted);

        Announcement announcement = null;
        if (entity != null) {
            announcement = entity.toDomain();
        }
        return announcement;

    }

    private void create(AnnouncementEntity announcement) {
        entityManager.persist(announcement);
        entityManager.flush();
    }

    private void update(AnnouncementEntity announcement) {
        entityManager.merge(announcement);
        entityManager.flush();
    }

    private List<AnnouncementEntity> getAllCurrentEntities() {

        List<AnnouncementEntity> result = entityManager.createQuery(
                "from AnnouncementEntity"
                        + " where deleted = false"
                        + " AND start_date <= now() AND end_date > now()",
                AnnouncementEntity.class).getResultList();
        return result;
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

    public List<AnnouncementEntity> getAllEntitiesFuture() {
        List<AnnouncementEntity> result = null;
        result = entityManager
                .createQuery("from AnnouncementEntity"
                        + " where deleted = false"
                        + " AND (start_date > now())",
                        AnnouncementEntity.class)
                .getResultList();

        return result;
    }

    private List<AnnouncementEntity> getAllEntitiesCurrentAndFuture() {
        List<AnnouncementEntity> result = null;
        result = entityManager
                .createQuery("from AnnouncementEntity"
                        + " where deleted = false"
                        + " AND (end_date > now())",
                        AnnouncementEntity.class)
                .getResultList();

        return result;
    }
}
