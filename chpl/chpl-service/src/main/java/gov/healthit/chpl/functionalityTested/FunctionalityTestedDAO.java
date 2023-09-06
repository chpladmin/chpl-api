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

@Repository("functionalityTestedDAO")
public class FunctionalityTestedDAO extends BaseDAOImpl {

    public FunctionalityTested getById(Long id) throws EntityRetrievalException {
        FunctionalityTestedEntity entity = getEntityById(id);
        if (entity != null) {
            return entity.toDomain();
        }
        return null;
    }

    public List<FunctionalityTested> findAll() {
        List<FunctionalityTestedEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    @Cacheable(CacheNames.FUNCTIONALITY_TESTED_MAPS)
    public Map<Long, List<FunctionalityTested>> getFunctionalitiesTestedCriteriaMaps() {
        List<FunctionalityTested> allFunctionalityTested = findAll();
        Map<Long, List<FunctionalityTested>> mapping = new HashMap<Long, List<FunctionalityTested>>();
        allFunctionalityTested.stream()
            .forEach(funcTest -> updateMapping(mapping, funcTest));
        return mapping;
    }

    private void updateMapping(Map<Long, List<FunctionalityTested>> mapping, FunctionalityTested functionalityTested) {
        functionalityTested.getCriteria().stream()
            .forEach(funcTestCriterion -> {
                if (!mapping.containsKey(funcTestCriterion.getId())) {
                    mapping.put(funcTestCriterion.getId(), new ArrayList<FunctionalityTested>());
                }
                mapping.get(funcTestCriterion.getId()).add(functionalityTested);
            });
    }

    private List<FunctionalityTestedEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT ft "
                            + "FROM FunctionalityTestedEntity ft "
                            + "LEFT OUTER JOIN FETCH ft.practiceType "
                            + "LEFT OUTER JOIN FETCH ft.mappedCriteria criteriaMapping "
                            + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                            + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                            + "LEFT JOIN FETCH criterion.rule "
                            + "WHERE (NOT ft.deleted = true) ", FunctionalityTestedEntity.class)
                .getResultList();
    }

    private FunctionalityTestedEntity getEntityById(Long id) throws EntityRetrievalException {
        FunctionalityTestedEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT ft "
                        + "FROM FunctionalityTestedEntity ft "
                        + "LEFT OUTER JOIN FETCH ft.practiceType "
                        + "LEFT OUTER JOIN FETCH ft.mappedCriteria criteriaMapping "
                        + "LEFT OUTER JOIN FETCH criteriaMapping.criterion criterion "
                        + "LEFT OUTER JOIN FETCH criterion.certificationEdition "
                        + "LEFT JOIN FETCH criterion.rule "
                        + "WHERE (NOT ft.deleted = true) "
                        + "AND (ft.id = :entityid) ",
                        FunctionalityTestedEntity.class);
        query.setParameter("entityid", id);
        List<FunctionalityTestedEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate functionality tested id in database.");
        }

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }
}
