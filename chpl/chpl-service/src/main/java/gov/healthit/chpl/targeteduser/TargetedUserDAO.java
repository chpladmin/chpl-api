package gov.healthit.chpl.targeteduser;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Repository("targetedUserDao")
@Log4j2
public class TargetedUserDAO extends BaseDAOImpl {

    public Long create(String name) throws EntityCreationException {
        try {
            TargetedUserEntity entity = new TargetedUserEntity();
            entity.setName(name);
            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public void update(TargetedUser targetedUser) throws EntityRetrievalException {
        TargetedUserEntity entity = this.getEntityById(targetedUser.getId());
        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + targetedUser.getId() + " does not exist");
        }
        entity.setName(targetedUser.getName());
        update(entity);
    }

    public void delete(Long id) throws EntityRetrievalException {
        TargetedUserEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            update(toDelete);
        }
    }

    public TargetedUser getById(Long id) {
        TargetedUserEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public TargetedUser getByName(String name) {
        List<TargetedUserEntity> entities = getEntitiesByName(name);

        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    public List<TargetedUser> findAll() {
        List<TargetedUserEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public TargetedUser findOrCreate(Long id, String name) throws EntityCreationException {
        TargetedUser result = null;
        if (id != null) {
            result = getById(id);
        } else if (!StringUtils.isEmpty(name)) {
            result = getByName(name);
        }

        if (result == null) {
            create(name.trim());
            result = getByName(name.trim());
        }
        return result;
    }

    private List<TargetedUserEntity> getAllEntities() {
        return entityManager
                .createQuery("from TargetedUserEntity where (NOT deleted = true) ", TargetedUserEntity.class)
                .getResultList();
    }

    private TargetedUserEntity getEntityById(Long id) {
        TargetedUserEntity entity = null;
        Query query = entityManager.createQuery(
                "from TargetedUserEntity where (NOT deleted = true) AND (id = :entityid) ", TargetedUserEntity.class);
        query.setParameter("entityid", id);
        List<TargetedUserEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<TargetedUserEntity> getEntitiesByName(String name) {
        Query query = entityManager.createQuery(
                "from TargetedUserEntity where " + "(NOT deleted = true) AND (UPPER(name) = :name) ",
                TargetedUserEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<TargetedUserEntity> result = query.getResultList();
        return result;
    }
}
