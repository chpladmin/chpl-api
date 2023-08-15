package gov.healthit.chpl.conformanceMethod.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodCriteriaMapEntity;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("conformanceMethodDAO")
public class ConformanceMethodDAO extends BaseDAOImpl {

    public List<ConformanceMethod> getAll() {
        Query query = entityManager.createQuery("SELECT cm "
                + "FROM ConformanceMethodEntity cm "
                + "WHERE cm.deleted = false", ConformanceMethodEntity.class);
        List<ConformanceMethodEntity> results = query.getResultList();
        return results.stream()
                .map(result -> result.toDomain())
                .collect(Collectors.toList());
    }

    public List<ConformanceMethod> getByCriterionId(Long criterionId) {
        Set<ConformanceMethodEntity> entities = getConformanceMethodByCertificationCriteria(criterionId);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    private Set<ConformanceMethodEntity> getConformanceMethodByCertificationCriteria(Long criterionId) {
        Query query = entityManager.createQuery("SELECT cmMap "
                + "FROM ConformanceMethodCriteriaMapEntity cmMap "
                + "JOIN FETCH cmMap.conformanceMethod cm "
                + "JOIN FETCH cmMap.certificationCriterion cce "
                + "LEFT JOIN FETCH cce.certificationEdition "
                + "LEFT JOIN FETCH cce.rule "
                + "WHERE cmMap.deleted <> true "
                + "AND cm.deleted <> true "
                + "AND cce.id = :criterionId",
                ConformanceMethodCriteriaMapEntity.class);
        query.setParameter("criterionId", criterionId);
        List<ConformanceMethodCriteriaMapEntity> results = query.getResultList();

        Set<ConformanceMethodEntity> cms = new HashSet<ConformanceMethodEntity>();
        for (ConformanceMethodCriteriaMapEntity result : results) {
            cms.add(result.getConformanceMethod());
        }
        return cms;
    }

    public List<ConformanceMethodCriteriaMap> getAllConformanceMethodCriteriaMap() throws EntityRetrievalException {
        return getAllConformanceMethodCriteriaMapEntities().stream()
                .map(e -> new ConformanceMethodCriteriaMap(e))
                .collect(Collectors.toList());
    }

    private List<ConformanceMethodCriteriaMapEntity> getAllConformanceMethodCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT cmcm "
                        + "FROM ConformanceMethodCriteriaMapEntity cmcm "
                        + "JOIN FETCH cmcm.certificationCriterion c "
                        + "LEFT JOIN FETCH c.certificationEdition "
                        + "LEFT JOIN FETCH c.rule "
                        + "JOIN FETCH cmcm.conformanceMethod cm "
                        + "WHERE cmcm.deleted <> true "
                        + "AND cm.deleted <> true ",
                        ConformanceMethodCriteriaMapEntity.class)
                .getResultList();
    }
}
