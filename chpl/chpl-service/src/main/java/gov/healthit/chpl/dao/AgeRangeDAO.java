package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.TestParticipant.TestParticipantAge;
import gov.healthit.chpl.entity.AgeRangeEntity;

@Repository("ageRangeDao")
public class AgeRangeDAO extends BaseDAOImpl {

    public TestParticipantAge getById(Long id) {
        AgeRangeEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public TestParticipantAge getByName(String name) {
        AgeRangeEntity entity = getEntityByName(name);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public List<TestParticipantAge> getAll() {
        List<AgeRangeEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    private List<AgeRangeEntity> getAllEntities() {
        List<AgeRangeEntity> result = entityManager
                .createQuery("from AgeRangeEntity where (NOT deleted = true) ", AgeRangeEntity.class).getResultList();
        return result;

    }

    private AgeRangeEntity getEntityByName(String name) {
        AgeRangeEntity entity = null;

        Query query = entityManager.createQuery(
                "from AgeRangeEntity where (NOT deleted = true) and (UPPER(age) = :age)", AgeRangeEntity.class);
        query.setParameter("age", name.toUpperCase());
        List<AgeRangeEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private AgeRangeEntity getEntityById(Long id) {
        AgeRangeEntity entity = null;
        Query query = entityManager.createQuery("from AgeRangeEntity where (NOT deleted = true) AND (id = :entityid) ",
                AgeRangeEntity.class);
        query.setParameter("entityid", id);
        List<AgeRangeEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}
