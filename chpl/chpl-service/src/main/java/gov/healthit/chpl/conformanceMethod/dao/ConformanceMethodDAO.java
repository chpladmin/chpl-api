package gov.healthit.chpl.conformanceMethod.dao;

import java.util.ArrayList;
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

    /*
    public OptionalStandardEntity getById(Long id) {
        OptionalStandardEntity entity = getEntityById(id);
        return entity;
    }

    public List<OptionalStandardEntity> findAll() {
        List<OptionalStandardEntity> entities = getAllEntities();
        return entities;
    }
*/
    public List<ConformanceMethod> getByCriterionId(Long criterionId) {
        Set<ConformanceMethodEntity> entities = getConformanceMethodByCertificationCriteria(criterionId);
        List<ConformanceMethod> domains = new ArrayList<ConformanceMethod>();

        for (ConformanceMethodEntity entity : entities) {
            ConformanceMethod domain = new ConformanceMethod(entity);
            domains.add(domain);
        }
        return domains;
    }

    private Set<ConformanceMethodEntity> getConformanceMethodByCertificationCriteria(Long criterionId) {
        Query query = entityManager.createQuery("SELECT cmMap "
                + "FROM ConformanceMethodCriteriaMapEntity cmMap "
                + "JOIN FETCH cmMap.conformanceMethod cm "
                + "JOIN FETCH cmMap.certificationCriterion cce "
                + "JOIN FETCH cce.certificationEdition "
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
/*
    public OptionalStandard getByCitation(String citation) {
        List<OptionalStandardEntity> entities = getEntitiesByCitation(citation);
        OptionalStandard obj = null;
        if (entities != null && entities.size() > 0) {
            obj = new OptionalStandard(entities.get(0));
        }
        return obj;
    }

    private List<OptionalStandardEntity> getAllEntities() {
        return entityManager
                .createQuery("from OptionalStandardEntity where (NOT deleted = true) ", OptionalStandardEntity.class)
                .getResultList();
    }

    private OptionalStandardEntity getEntityById(Long id) {
        OptionalStandardEntity entity = null;

        Query query = entityManager.createQuery("SELECT os " + "FROM OptionalStandardEntity os "
                + "WHERE (NOT deleted = true) " + "AND (os.id = :entityid) ", OptionalStandardEntity.class);
        query.setParameter("entityid", id);
        List<OptionalStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<OptionalStandardEntity> getEntitiesByCitation(String citation) {
        String osQuery = "SELECT os " + "FROM OptionalStandardEntity os "
                + "WHERE os.deleted <> true " + "AND UPPER(os.citation) = :citation ";
        Query query = entityManager.createQuery(osQuery, OptionalStandardEntity.class);
        query.setParameter("citation", citation.toUpperCase());

        List<OptionalStandardEntity> matches = query.getResultList();
        if (matches == null || matches.size() == 0) {
            // if this didn't find anything try again with spaces removed from
            // the optional standard
            query = entityManager.createQuery(osQuery, OptionalStandardEntity.class);
            String citationWithoutSpaces = citation.replaceAll("\\s", "");
            query.setParameter("citation", citationWithoutSpaces.toUpperCase());
            matches = query.getResultList();
        }
        return matches;
    }
*/
    private List<ConformanceMethodCriteriaMapEntity> getAllConformanceMethodCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT cmcm "
                        + "FROM ConformanceMethodCriteriaMapEntity cmcm "
                        + "JOIN FETCH cmcm.certificationCriterion c "
                        + "JOIN FETCH c.certificationEdition "
                        + "JOIN FETCH cmcm.conformanceMethod cm "
                        + "WHERE cmcm.deleted <> true "
                        + "AND cm.deleted <> true ",
                        ConformanceMethodCriteriaMapEntity.class)
                .getResultList();
    }
}
