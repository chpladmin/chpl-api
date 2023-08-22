package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.PracticeType;
import gov.healthit.chpl.entity.PracticeTypeEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("practiceTypeDAO")
public class PracticeTypeDAO extends BaseDAOImpl {

    public void create(PracticeType practiceType) throws EntityCreationException, EntityRetrievalException {
        PracticeTypeEntity entity = null;
        try {
            if (practiceType.getId() != null) {
                entity = this.getEntityById(practiceType.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {

            entity = new PracticeTypeEntity();
            entity.setCreationDate(new Date());
            entity.setDeleted(false);
            entity.setId(practiceType.getId());
            entity.setName(practiceType.getName());
            entity.setDescription(practiceType.getDescription());
            entity.setLastModifiedUser(AuthUtil.getAuditId());

            create(entity);
        }

    }

    public void update(PracticeType practceType) throws EntityRetrievalException {
        PracticeTypeEntity entity = this.getEntityById(practceType.getId());
        entity.setId(practceType.getId());
        entity.setName(practceType.getName());
        entity.setDescription(practceType.getDescription());
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        update(entity);
    }

    public void delete(Long id) {
        Query query = entityManager
                .createQuery("UPDATE PracticeTypeEntity SET deleted = true WHERE practice_type_id = :entityid");
        query.setParameter("entityid", id);
        query.executeUpdate();
    }

    public List<PracticeType> findAll() {
        return getAllEntities().stream()
                .map(e -> e.toDomain())
                .toList();
    }

    public PracticeType getById(Long id) throws EntityRetrievalException {

        PracticeTypeEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        } else {
            return null;
        }
    }

    public PracticeType getByName(String name) {
        return getEntityByName(name).toDomain();
    }

    private void create(PracticeTypeEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();
    }

    private void update(PracticeTypeEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();
    }

    private List<PracticeTypeEntity> getAllEntities() {

        List<PracticeTypeEntity> result = entityManager
                .createQuery("from PracticeTypeEntity where (NOT deleted = true) ", PracticeTypeEntity.class)
                .getResultList();
        return result;

    }

    public PracticeTypeEntity getEntityById(Long id) throws EntityRetrievalException {
        PracticeTypeEntity entity = null;
        Query query = entityManager.createQuery(
                "FROM PracticeTypeEntity "
                + "WHERE (NOT deleted = true) "
                + "AND (id = :entityid) ",
                PracticeTypeEntity.class);
        query.setParameter("entityid", id);
        List<PracticeTypeEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate developer id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private PracticeTypeEntity getEntityByName(String name) {
        Query query = entityManager.createQuery(
                "from PracticeTypeEntity where (NOT deleted = true) AND (name = :name) ", PracticeTypeEntity.class);
        query.setParameter("name", name);
        List<PracticeTypeEntity> result = query.getResultList();

        if (result.size() == 0) {
            return null;
        }

        return result.get(0);
    }
}
