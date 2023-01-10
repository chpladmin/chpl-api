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

    public FunctionalityTested getByNumberAndEdition(String number, Long editionId) {
        List<FunctionalityTestedEntity> entities = getEntitiesByNumberAndYear(number, editionId);
        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    public FunctionalityTested getByIdAndEdition(Long functionalityTestedId, Long editionId) {
        List<FunctionalityTestedEntity> entities = getEntitiesByIdAndYear(functionalityTestedId, editionId);
        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    public List<FunctionalityTested> findAll() {
        List<FunctionalityTestedEntity> entities = getAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<FunctionalityTestedCriteriaMap> getFunctionalitiesTestedCritieriaMaps() {
        List<FunctionalityTestedCriteriaMapEntity> entities = getAllMapEntities();
        return entities.stream()
                    .map(entity -> entity.toDomain())
                    .collect(Collectors.toList());
    }

    @Cacheable(CacheNames.FUNCTIONALITY_TESTED_MAPS)
    public Map<Long, List<FunctionalityTested>> getFunctionalitiesTestedCriteriaMaps(String edition) {
        List<FunctionalityTestedCriteriaMap> allMaps = getFunctionalitiesTestedCritieriaMaps();
        Map<Long, List<FunctionalityTested>> mapping = new HashMap<Long, List<FunctionalityTested>>();
        for (FunctionalityTestedCriteriaMap map : allMaps) {
            if (map.getCriterion().getCertificationEdition().equals(edition)) {
                if (!mapping.containsKey(map.getCriterion().getId())) {
                    mapping.put(map.getCriterion().getId(), new ArrayList<FunctionalityTested>());
                }
                mapping.get(map.getCriterion().getId()).add(map.getFunctionalityTested());
            }
        }
        return mapping;
    }

    private List<FunctionalityTestedCriteriaMapEntity> getAllMapEntities() {
        return entityManager
                .createQuery("FROM FunctionalityTestedCriteriaMapEntity ftcm "
                            + "LEFT OUTER JOIN FETCH ftcm.functionalityTested ft "
                            + "LEFT OUTER JOIN FETCH ft.practiceType pt "
                            + "LEFT OUTER JOIN FETCH ftcm.criterion c "
                            + "LEFT OUTER JOIN FETCH c.certificationEdition "
                            + "WHERE (NOT ftcm.deleted = true) ", FunctionalityTestedCriteriaMapEntity.class)
                .getResultList();
    }

    private List<FunctionalityTestedEntity> getAllEntities() {
        return entityManager
                .createQuery("SELECT ft "
                            + "FROM FunctionalityTestedEntity ft "
                            + "LEFT OUTER JOIN FETCH ft.certificationEdition "
                            + "LEFT OUTER JOIN FETCH ft.practiceType "
                            + "WHERE (NOT ft.deleted = true) ", FunctionalityTestedEntity.class)
                .getResultList();
    }

    private FunctionalityTestedEntity getEntityById(Long id) throws EntityRetrievalException {

        FunctionalityTestedEntity entity = null;

        Query query = entityManager
                .createQuery("SELECT ft "
                        + "FROM FunctionalityTestedEntity ft "
                        + "LEFT OUTER JOIN FETCH ft.certificationEdition "
                        + "LEFT OUTER JOIN FETCH ft.practiceType "
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

    private List<FunctionalityTestedEntity> getEntitiesByNumberAndYear(String number, Long editionId) {
        Query query = entityManager.createQuery("SELECT ft "
                        + "FROM FunctionalityTestedEntity ft "
                        + "LEFT OUTER JOIN FETCH ft.certificationEdition "
                        + "LEFT OUTER JOIN FETCH ft.practiceType "
                        + "WHERE ft.deleted <> true "
                        + "AND UPPER(ft.number) = :number "
                        + "AND ft.certificationEdition.id = :editionId ", FunctionalityTestedEntity.class);
        query.setParameter("number", number.toUpperCase());
        query.setParameter("editionId", editionId);

        return query.getResultList();
    }

    private List<FunctionalityTestedEntity> getEntitiesByIdAndYear(Long id, Long editionId) {
        Query query = entityManager.createQuery("SELECT ft "
                        + "FROM FunctionalityTestedEntity ft "
                        + "LEFT OUTER JOIN FETCH ft.certificationEdition "
                        + "LEFT OUTER JOIN FETCH ft.practiceType "
                        + "WHERE ft.deleted <> true "
                        + "AND ft.id = :id "
                        + "AND ft.certificationEdition.id = :editionId ", FunctionalityTestedEntity.class);
        query.setParameter("id", id);
        query.setParameter("editionId", editionId);

        return query.getResultList();
    }
}
