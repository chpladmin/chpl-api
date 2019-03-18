package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ActivityConceptDAO;
import gov.healthit.chpl.dto.ActivityConceptDTO;
import gov.healthit.chpl.entity.ActivityConceptEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("activityClassDAO")
public class ActivityConceptDAOImpl extends BaseDAOImpl implements ActivityConceptDAO {

    @Override
    public ActivityConceptDTO create(ActivityConceptDTO dto) throws EntityCreationException, EntityRetrievalException {

        ActivityConceptEntity entity = null;
        try {
            if (dto.getId() != null) {
                entity = this.getEntityById(dto.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {

            entity = new ActivityConceptEntity();

            entity.setId(dto.getId());
            entity.setClassName(dto.getClassName());
            entity.setCreationDate(new Date());
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(Util.getAuditId());
            entity.setDeleted(dto.getDeleted());

            create(entity);

        }
        ActivityConceptDTO result = null;
        if (entity != null) {
            result = new ActivityConceptDTO(entity);
        }
        return result;
    }

    @Override
    public ActivityConceptDTO update(ActivityConceptDTO dto) throws EntityRetrievalException {

        ActivityConceptEntity entity = this.getEntityById(dto.getId());

        entity.setId(dto.getId());
        entity.setClassName(dto.getClassName());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());
        entity.setLastModifiedUser(Util.getAuditId());
        entity.setDeleted(dto.getDeleted());

        update(entity);

        ActivityConceptDTO result = null;
        
        //Findbugs says this cannot be null since it used above - an NPE would have been thrown
        //if (entity != null) {
        result = new ActivityConceptDTO(entity);
        //}
        return result;

    }

    @Override
    public void delete(Long id) throws EntityRetrievalException {

        Query query = entityManager
                .createQuery("UPDATE ActivityClassEntity SET deleted = true WHERE ActivityClass_id = :resultid");
        query.setParameter("resultid", id);
        query.executeUpdate();

    }

    @Override
    public ActivityConceptDTO getById(Long id) throws EntityRetrievalException {

        ActivityConceptEntity entity = getEntityById(id);
        ActivityConceptDTO dto = null;
        if (entity != null) {
            dto = new ActivityConceptDTO(entity);
        }
        return dto;
    }

    @Override
    public List<ActivityConceptDTO> findAll() {

        List<ActivityConceptEntity> entities = getAllEntities();
        List<ActivityConceptDTO> activities = new ArrayList<>();

        for (ActivityConceptEntity entity : entities) {
            ActivityConceptDTO result = new ActivityConceptDTO(entity);
            activities.add(result);
        }
        return activities;
    }

    private void create(ActivityConceptEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();

    }

    private void update(ActivityConceptEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();

    }

    private ActivityConceptEntity getEntityById(Long id) throws EntityRetrievalException {

        ActivityConceptEntity entity = null;

        Query query = entityManager.createQuery(
                "from ActivityClassEntity where (NOT deleted = true) AND (additional_software_id = :entityid) ",
                ActivityConceptEntity.class);
        query.setParameter("entityid", id);
        List<ActivityConceptEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private ActivityConceptEntity getEntityByName(String name) {
        ActivityConceptEntity entity = null;

        Query query = entityManager.createQuery(
                "from ActivityClassEntity where (NOT deleted = true) AND (name = :name) ", ActivityConceptEntity.class);
        query.setParameter("name", name);
        List<ActivityConceptEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ActivityConceptEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {

        Query query = entityManager.createQuery(
                "from ActivityClassEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ",
                ActivityConceptEntity.class);
        query.setParameter("entityid", certifiedProductId);
        List<ActivityConceptEntity> result = query.getResultList();
        return result;
    }

    private List<ActivityConceptEntity> getAllEntities() {

        List<ActivityConceptEntity> result = entityManager
                .createQuery("from ActivityClassEntity where (NOT deleted = true) ", ActivityConceptEntity.class)
                .getResultList();
        return result;
    }

}
