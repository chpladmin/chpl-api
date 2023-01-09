package gov.healthit.chpl.functionalityTested;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("testFunctionalityDAO")
public class TestFunctionalityDAO extends BaseDAOImpl {

    public TestFunctionality getById(Long id) throws EntityRetrievalException {
        TestFunctionalityEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public TestFunctionality getByNumberAndEdition(String number, Long editionId) {
        List<TestFunctionalityEntity> entities = getEntitiesByNumberAndYear(number, editionId);
        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    public TestFunctionality getByIdAndEdition(Long testFunctionalityId, Long editionId) {
        List<TestFunctionalityEntity> entities = getEntitiesByIdAndYear(testFunctionalityId, editionId);
        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    public List<TestFunctionality> findAll() {
        List<TestFunctionalityEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<TestFunctionalityCriteriaMap> getFunctionalitiesTestedCritieriaMaps() {
        List<TestFunctionalityCriteriaMapEntity> entities = getAllMapEntities();
        return entities.stream()
                    .map(entity -> entity.toDomain())
                    .collect(Collectors.toList());
    }

    @Cacheable(CacheNames.FUNCTIONALITY_TESTED_MAPS)
    public Map<Long, List<TestFunctionality>> getFunctionalitiesTestedCriteriaMaps(String edition) {
        List<TestFunctionalityCriteriaMap> allMaps = getFunctionalitiesTestedCritieriaMaps();
        Map<Long, List<TestFunctionality>> mapping = new HashMap<Long, List<TestFunctionality>>();
        for (TestFunctionalityCriteriaMap map : allMaps) {
            if (map.getCriterion().getCertificationEdition().equals(edition)) {
                if (!mapping.containsKey(map.getCriterion().getId())) {
                    mapping.put(map.getCriterion().getId(), new ArrayList<TestFunctionality>());
                }
                mapping.get(map.getCriterion().getId()).add(map.getFunctionalityTested());
            }
        }
        return mapping;
    }

    private List<TestFunctionalityCriteriaMapEntity> getAllMapEntities() {
        return entityManager
                .createQuery("FROM TestFunctionalityCriteriaMapEntity ftcm "
                            + "LEFT OUTER JOIN FETCH ftcm.functionalityTested ft "
                            + "LEFT OUTER JOIN FETCH ft.practiceType pt "
                            + "LEFT OUTER JOIN FETCH ftcm.criterion c "
                            + "LEFT OUTER JOIN FETCH c.certificationEdition "
                            + "WHERE (NOT ftcm.deleted = true) ", TestFunctionalityCriteriaMapEntity.class)
                .getResultList();
    }

    private List<TestFunctionalityEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT ft "
                            + "FROM TestFunctionalityEntity ft "
                            + "LEFT OUTER JOIN FETCH ft.certificationEdition "
                            + "LEFT OUTER JOIN FETCH ft.practiceType "
                            + "WHERE (NOT ft.deleted = true) ", TestFunctionalityEntity.class)
                .getResultList();
    }

    private TestFunctionalityEntity getEntityById(Long id) throws EntityRetrievalException {

        TestFunctionalityEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT ft "
                        + "FROM TestFunctionalityEntity ft "
                        + "LEFT OUTER JOIN FETCH ft.certificationEdition "
                        + "LEFT OUTER JOIN FETCH ft.practiceType "
                        + "WHERE (NOT ft.deleted = true) "
                        + "AND (ft.id = :entityid) ",
                        TestFunctionalityEntity.class);
        query.setParameter("entityid", id);
        List<TestFunctionalityEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate functionality tested id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<TestFunctionalityEntity> getEntitiesByNumberAndYear(String number, Long editionId) {
        Query query = entityManager.createQuery("SELECT ft "
                        + "FROM TestFunctionalityEntity ft "
                        + "LEFT OUTER JOIN FETCH ft.certificationEdition "
                        + "LEFT OUTER JOIN FETCH ft.practiceType "
                        + "WHERE ft.deleted <> true "
                        + "AND UPPER(ft.number) = :number "
                        + "AND ft.certificationEdition.id = :editionId ", TestFunctionalityEntity.class);
        query.setParameter("number", number.toUpperCase());
        query.setParameter("editionId", editionId);

        return query.getResultList();
    }

    private List<TestFunctionalityEntity> getEntitiesByIdAndYear(Long id, Long editionId) {
        Query query = entityManager.createQuery("SELECT ft "
                        + "FROM TestFunctionalityEntity ft "
                        + "LEFT OUTER JOIN FETCH ft.certificationEdition "
                        + "LEFT OUTER JOIN FETCH ft.practiceType "
                        + "WHERE ft.deleted <> true "
                        + "AND ft.id = :id "
                        + "AND ft.certificationEdition.id = :editionId ", TestFunctionalityEntity.class);
        query.setParameter("id", id);
        query.setParameter("editionId", editionId);

        return query.getResultList();
    }
}
