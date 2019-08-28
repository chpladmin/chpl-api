package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.AnnouncementDAO;
import gov.healthit.chpl.dto.AnnouncementDTO;
import gov.healthit.chpl.entity.AnnouncementEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository(value = "announcementDAO")
public class AnnouncementDAOImpl extends BaseDAOImpl implements AnnouncementDAO {

    @Transactional
    public AnnouncementDTO create(AnnouncementDTO dto) throws EntityRetrievalException, EntityCreationException {
        AnnouncementEntity entity = null;
        try {
            if (dto.getId() != null) {
                entity = this.getEntityById(dto.getId(), false);
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new AnnouncementEntity();

            if (dto.getTitle() != null) {
                entity.setTitle(dto.getTitle());
            }

            if (dto.getText() != null) {
                entity.setText(dto.getText());
            }

            if (dto.getStartDate() != null) {
                entity.setStartDate(dto.getStartDate());
            }

            if (dto.getEndDate() != null) {
                entity.setEndDate(dto.getEndDate());
            }

            if (dto.getIsPublic() != null) {
                entity.setIsPublic(dto.getIsPublic());
            } else {
                entity.setIsPublic(false);
            }

            if (dto.getDeleted() != null) {
                entity.setDeleted(dto.getDeleted());
            } else {
                entity.setDeleted(false);
            }

            if (dto.getLastModifiedUser() != null) {
                entity.setLastModifiedUser(dto.getLastModifiedUser());
            } else {
                entity.setLastModifiedUser(AuthUtil.getAuditId());
            }

            if (dto.getLastModifiedDate() != null) {
                entity.setLastModifiedDate(dto.getLastModifiedDate());
            } else {
                entity.setLastModifiedDate(new Date());
            }

            if (dto.getCreationDate() != null) {
                entity.setCreationDate(dto.getCreationDate());
            } else {
                entity.setCreationDate(new Date());
            }

            create(entity);
            return new AnnouncementDTO(entity);
        }
    }

    @Transactional
    public AnnouncementDTO update(AnnouncementDTO dto, boolean includeDeleted) throws EntityRetrievalException {

        AnnouncementEntity entity = getEntityById(dto.getId(), includeDeleted);
        if (entity == null) {
            throw new EntityRetrievalException(
                    "Cannot update entity with id " + dto.getId() + ". Entity does not exist.");
        }

        if (dto.getText() != null) {
            entity.setText(dto.getText());
        }

        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }

        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }

        if (dto.getIsPublic() != null) {
            entity.setIsPublic(dto.getIsPublic());
        } else {
            entity.setIsPublic(false);
        }

        if (dto.getDeleted() != null) {
            entity.setDeleted(dto.getDeleted());
        }
        if (dto.getLastModifiedUser() != null) {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        } else {
            entity.setLastModifiedUser(AuthUtil.getAuditId());
        }

        if (dto.getLastModifiedDate() != null) {
            entity.setLastModifiedDate(dto.getLastModifiedDate());
        } else {
            entity.setLastModifiedDate(new Date());
        }

        update(entity);
        return new AnnouncementDTO(entity);
    }

    @Transactional
    public void delete(Long aId) {

        // TODO: How to delete this without leaving orphans

        Query query = entityManager
                .createQuery("UPDATE AnnouncementEntity SET deleted = true WHERE announcement_id = :aid");
        query.setParameter("aid", aId);
        query.executeUpdate();

    }

    public List<AnnouncementDTO> findAll() {

        List<AnnouncementEntity> entities = getAllEntities();
        List<AnnouncementDTO> announcements = new ArrayList<>();

        for (AnnouncementEntity entity : entities) {
            AnnouncementDTO announcement = new AnnouncementDTO(entity);
            announcements.add(announcement);
        }
        return announcements;

    }

    public List<AnnouncementDTO> findAllCurrentAndFuture() {

        List<AnnouncementEntity> entities = getAllEntitiesCurrentAndFuture();
        List<AnnouncementDTO> announcements = new ArrayList<>();

        for (AnnouncementEntity entity : entities) {
            AnnouncementDTO announcement = new AnnouncementDTO(entity);
            announcements.add(announcement);
        }
        return announcements;

    }

    public List<AnnouncementDTO> findAllFuture() {

        List<AnnouncementEntity> entities = getAllEntitiesFuture();
        List<AnnouncementDTO> announcements = new ArrayList<>();

        for (AnnouncementEntity entity : entities) {
            AnnouncementDTO announcement = new AnnouncementDTO(entity);
            announcements.add(announcement);
        }
        return announcements;

    }

    public AnnouncementDTO getById(Long announcementId, boolean includeDeleted) throws EntityRetrievalException {
        AnnouncementEntity entity = getEntityById(announcementId, includeDeleted);

        AnnouncementDTO dto = null;
        if (entity != null) {
            dto = new AnnouncementDTO(entity);
        }
        return dto;

    }

    public AnnouncementDTO getByIdToUpdate(Long announcementId, boolean includeDeleted)
            throws EntityRetrievalException {
        AnnouncementEntity entity = getEntityById(announcementId, includeDeleted);

        AnnouncementDTO dto = null;
        if (entity != null) {
            dto = new AnnouncementDTO(entity);
        }
        return dto;

    }

    private void create(AnnouncementEntity announcement) {

        entityManager.persist(announcement);
        entityManager.flush();
    }

    private void update(AnnouncementEntity announcement) {

        entityManager.merge(announcement);
        entityManager.flush();

    }

    private List<AnnouncementEntity> getAllEntities() {

        List<AnnouncementEntity> result = entityManager
                    .createQuery("from AnnouncementEntity" + " where deleted = false"
                            + " AND start_date <= now() AND end_date > now()", AnnouncementEntity.class)
                    .getResultList();
        return result;
    }

    private AnnouncementEntity getEntityById(final Long entityId, final boolean includeDeleted) throws EntityRetrievalException {

        List<AnnouncementEntity> results = null;
        AnnouncementEntity entity = null;
        String hql = "SELECT a "
                + "FROM AnnouncementEntity a "
                + "WHERE (a.id = :entityid) ";
        if (!includeDeleted) {
            hql += " AND a.deleted = false ";
        }
        Query query = entityManager
                .createQuery(hql, AnnouncementEntity.class);
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

    @Override
    public List<AnnouncementEntity> getAllEntitiesFuture() {
        List<AnnouncementEntity> result = null;
        result = entityManager
                .createQuery("from AnnouncementEntity" + " where deleted = false" + " AND (start_date > now())",
                        AnnouncementEntity.class)
                .getResultList();

        return result;
    }

    private List<AnnouncementEntity> getAllEntitiesCurrentAndFuture() {
        List<AnnouncementEntity> result = null;
        result = entityManager
                .createQuery("from AnnouncementEntity" + " where deleted = false" + " AND (end_date > now())",
                        AnnouncementEntity.class)
                .getResultList();

        return result;
    }
}
