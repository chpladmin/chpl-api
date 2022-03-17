package gov.healthit.chpl.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DeveloperStatus;
import gov.healthit.chpl.entity.developer.DeveloperStatusEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;

@Repository("developerStatusDAO")
public class DeveloperStatusDAO extends BaseDAOImpl {

    public DeveloperStatus getById(Long id) {
        DeveloperStatusEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public DeveloperStatus getByName(String name) {
        List<DeveloperStatusEntity> entities = getEntitiesByName(name);
        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    public List<DeveloperStatus> findAll() {
        List<DeveloperStatusEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    List<DeveloperStatusEntity> getAllEntities() {
        return entityManager
                .createQuery("from DeveloperStatusEntity where (NOT deleted = true) ", DeveloperStatusEntity.class)
                .getResultList();
    }

    DeveloperStatusEntity getEntityById(final Long id) {

        DeveloperStatusEntity entity = null;

        Query query = entityManager.createQuery(
                "from DeveloperStatusEntity ds where (NOT deleted = true) AND (ds.id = :entityid) ",
                DeveloperStatusEntity.class);
        query.setParameter("entityid", id);
        List<DeveloperStatusEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    List<DeveloperStatusEntity> getEntitiesByName(final String name) {

        Query query = entityManager.createQuery(
                "from DeveloperStatusEntity where " + "(NOT deleted = true) AND (name LIKE :name) ",
                DeveloperStatusEntity.class);
        query.setParameter("name", DeveloperStatusType.getValue(name));
        List<DeveloperStatusEntity> result = query.getResultList();

        return result;
    }
}
