package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.entity.TestFunctionalityCriteriaMapEntity;
import gov.healthit.chpl.entity.TestFunctionalityEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("testFunctionalityDAO")
public class TestFunctionalityDAOImpl extends BaseDAOImpl implements TestFunctionalityDAO {

    @Override
    public TestFunctionalityDTO getById(Long id) throws EntityRetrievalException {

        TestFunctionalityDTO dto = null;
        TestFunctionalityEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestFunctionalityDTO(entity);
        }
        return dto;
    }

    @Override
    public TestFunctionalityDTO getByNumberAndEdition(String number, Long editionId) {

        TestFunctionalityDTO dto = null;
        List<TestFunctionalityEntity> entities = getEntitiesByNumberAndYear(number, editionId);

        if (entities != null && entities.size() > 0) {
            dto = new TestFunctionalityDTO(entities.get(0));
        }
        return dto;
    }

    @Override
    public List<TestFunctionalityDTO> findAll() {

        List<TestFunctionalityEntity> entities = getAllEntities();
        List<TestFunctionalityDTO> dtos = new ArrayList<TestFunctionalityDTO>();

        for (TestFunctionalityEntity entity : entities) {
            TestFunctionalityDTO dto = new TestFunctionalityDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    @Override
    public List<TestFunctionalityCriteriaMapDTO> getTestFunctionalityCritieriaMaps() {
        List<TestFunctionalityCriteriaMapEntity> maps = getAllMapEntities();
        List<TestFunctionalityCriteriaMapDTO> dtos = new ArrayList<TestFunctionalityCriteriaMapDTO>();
        for (TestFunctionalityCriteriaMapEntity entity : maps) {
            dtos.add(new TestFunctionalityCriteriaMapDTO(entity));
        }
        return dtos;
    }

    private List<TestFunctionalityCriteriaMapEntity> getAllMapEntities() {
        return entityManager
                .createQuery("FROM TestFunctionalityCriteriaMapEntity tfcm "
                            + "LEFT OUTER JOIN FETCH tfcm.testFunctionality tf "
                            + "LEFT OUTER JOIN FETCH tf.practiceType pt "
                            + "LEFT OUTER JOIN FETCH tfcm.criteria c "
                            + "LEFT OUTER JOIN FETCH c.certificationEdition "
                            + "WHERE (NOT tfcm.deleted = true) ", TestFunctionalityCriteriaMapEntity.class)
                .getResultList();
    }

    private List<TestFunctionalityEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT tf "
                            + "FROM TestFunctionalityEntity tf "
                            + "LEFT OUTER JOIN FETCH tf.certificationEdition "
                            + "LEFT OUTER JOIN FETCH tf.practiceType "
                            + "WHERE (NOT tf.deleted = true) ", TestFunctionalityEntity.class)
                .getResultList();
    }

    private TestFunctionalityEntity getEntityById(Long id) throws EntityRetrievalException {

        TestFunctionalityEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT tf "
                        + "FROM TestFunctionalityEntity tf "
                        + "LEFT OUTER JOIN FETCH tf.certificationEdition "
                        + "LEFT OUTER JOIN FETCH tf.practiceType "
                        + "WHERE (NOT tf.deleted = true) " + "AND (tf.id = :entityid) ",
                        TestFunctionalityEntity.class);
        query.setParameter("entityid", id);
        List<TestFunctionalityEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate test functionality id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<TestFunctionalityEntity> getEntitiesByNumberAndYear(String number, Long editionId) {

        TestFunctionalityEntity entity = null;

        Query query = entityManager.createQuery("SELECT tf "
                        + "FROM TestFunctionalityEntity tf "
                        + "LEFT OUTER JOIN FETCH tf.certificationEdition "
                        + "LEFT OUTER JOIN FETCH tf.practiceType "
                        + "WHERE tf.deleted <> true "
                        + "AND UPPER(tf.number) = :number "
                        + "AND tf.certificationEdition.id = :editionId ", TestFunctionalityEntity.class);
        query.setParameter("number", number.toUpperCase());
        query.setParameter("editionId", editionId);

        return query.getResultList();
    }
}
