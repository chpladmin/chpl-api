package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.TestParticipant.TestParticipantEducation;
import gov.healthit.chpl.entity.EducationTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("educationTypeDao")
public class EducationTypeDAO extends BaseDAOImpl {

    public TestParticipantEducation getById(Long id) throws EntityRetrievalException {
        EducationTypeEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public TestParticipantEducation getByName(String name) {
        EducationTypeEntity entity = getEntityByName(name);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public List<TestParticipantEducation> getAll() {
        List<EducationTypeEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    private List<EducationTypeEntity> getAllEntities() {
        List<EducationTypeEntity> result = entityManager
                .createQuery("from EducationTypeEntity where (NOT deleted = true) ", EducationTypeEntity.class)
                .getResultList();
        return result;

    }

    private EducationTypeEntity getEntityByName(String name) {
        EducationTypeEntity entity = null;
        Query query = entityManager.createQuery(
                "from EducationTypeEntity where (NOT deleted = true) and (UPPER(name) = :name)",
                EducationTypeEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<EducationTypeEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private EducationTypeEntity getEntityById(Long id) throws EntityRetrievalException {
        EducationTypeEntity entity = null;

        Query query = entityManager.createQuery(
                "from EducationTypeEntity where (NOT deleted = true) AND (id = :entityid) ", EducationTypeEntity.class);
        query.setParameter("entityid", id);
        List<EducationTypeEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate education type id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}
