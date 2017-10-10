package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.entity.TestFunctionalityEntity;

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

    private List<TestFunctionalityEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT tf " + "FROM TestFunctionalityEntity tf " + "JOIN FETCH tf.certificationEdition "
                        + "WHERE (NOT tf.deleted = true) ", TestFunctionalityEntity.class)
                .getResultList();
    }

    private TestFunctionalityEntity getEntityById(Long id) throws EntityRetrievalException {

        TestFunctionalityEntity entity = null;

        Query query = entityManager
                .createQuery(
                        "SELECT tf " + "FROM TestFunctionalityEntity tf " + "JOIN FETCH tf.certificationEdition "
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

        Query query = entityManager.createQuery("SELECT tf " + "FROM TestFunctionalityEntity tf "
                + "JOIN FETCH tf.certificationEdition edition " + "WHERE tf.deleted <> true "
                + "AND UPPER(tf.number) = :number " + "AND edition.id = :editionId ", TestFunctionalityEntity.class);
        query.setParameter("number", number.toUpperCase());
        query.setParameter("editionId", editionId);

        return query.getResultList();
    }
}
