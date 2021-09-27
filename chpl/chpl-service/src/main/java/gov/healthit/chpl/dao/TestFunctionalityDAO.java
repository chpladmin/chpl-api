package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.TestFunctionalityCriteriaMapDTO;
import gov.healthit.chpl.dto.TestFunctionalityDTO;
import gov.healthit.chpl.entity.TestFunctionalityCriteriaMapEntity;
import gov.healthit.chpl.entity.TestFunctionalityEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("testFunctionalityDAO")
public class TestFunctionalityDAO extends BaseDAOImpl {

    public TestFunctionalityDTO getById(Long id) throws EntityRetrievalException {

        TestFunctionalityDTO dto = null;
        TestFunctionalityEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new TestFunctionalityDTO(entity);
        }
        return dto;
    }

    public TestFunctionalityDTO getByNumberAndEdition(String number, Long editionId) {
        TestFunctionalityDTO dto = null;
        List<TestFunctionalityEntity> entities = getEntitiesByNumberAndYear(number, editionId);
        if (entities != null && entities.size() > 0) {
            dto = new TestFunctionalityDTO(entities.get(0));
        }
        return dto;
    }

    public TestFunctionalityDTO getByIdAndEdition(Long testFunctionalityId, Long editionId) {
        TestFunctionalityDTO dto = null;
        List<TestFunctionalityEntity> entities = getEntitiesByIdAndYear(testFunctionalityId, editionId);
        if (entities != null && entities.size() > 0) {
            dto = new TestFunctionalityDTO(entities.get(0));
        }
        return dto;
    }

    public List<TestFunctionalityDTO> findAll() {

        List<TestFunctionalityEntity> entities = getAllEntities();
        List<TestFunctionalityDTO> dtos = new ArrayList<TestFunctionalityDTO>();

        for (TestFunctionalityEntity entity : entities) {
            TestFunctionalityDTO dto = new TestFunctionalityDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    public List<TestFunctionalityCriteriaMapDTO> getTestFunctionalityCritieriaMaps() {
        List<TestFunctionalityCriteriaMapEntity> maps = getAllMapEntities();
        List<TestFunctionalityCriteriaMapDTO> dtos = new ArrayList<TestFunctionalityCriteriaMapDTO>();
        for (TestFunctionalityCriteriaMapEntity entity : maps) {
            dtos.add(new TestFunctionalityCriteriaMapDTO(entity));
        }
        return dtos;
    }

    @Cacheable(CacheNames.TEST_FUNCTIONALITY_MAPS)
    public Map<Long, List<TestFunctionalityDTO>> getTestFunctionalityCriteriaMaps(String edition) {
        List<TestFunctionalityCriteriaMapDTO> allMaps = getTestFunctionalityCritieriaMaps();
        Map<Long, List<TestFunctionalityDTO>> mapping = new HashMap<Long, List<TestFunctionalityDTO>>();
        for (TestFunctionalityCriteriaMapDTO map : allMaps) {
            if (map.getCriteria().getCertificationEdition().equals(edition)) {
                if (!mapping.containsKey(map.getCriteria().getId())) {
                    mapping.put(map.getCriteria().getId(), new ArrayList<TestFunctionalityDTO>());
                }
                mapping.get(map.getCriteria().getId()).add(map.getTestFunctionality());
            }
        }
        return mapping;
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

    private List<TestFunctionalityEntity> getEntitiesByIdAndYear(Long id, Long editionId) {
        Query query = entityManager.createQuery("SELECT tf "
                        + "FROM TestFunctionalityEntity tf "
                        + "LEFT OUTER JOIN FETCH tf.certificationEdition "
                        + "LEFT OUTER JOIN FETCH tf.practiceType "
                        + "WHERE tf.deleted <> true "
                        + "AND tf.id = :id "
                        + "AND tf.certificationEdition.id = :editionId ", TestFunctionalityEntity.class);
        query.setParameter("id", id);
        query.setParameter("editionId", editionId);

        return query.getResultList();
    }
}
