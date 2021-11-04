package gov.healthit.chpl.optionalStandard.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.optionalStandard.entity.OptionalStandardCriteriaMapEntity;
import gov.healthit.chpl.optionalStandard.entity.OptionalStandardEntity;

@Repository("optionalStandardDAO")
public class OptionalStandardDAO extends BaseDAOImpl {

    public OptionalStandardEntity getById(Long id) {
        OptionalStandardEntity entity = getEntityById(id);
        return entity;
    }

    public List<OptionalStandardEntity> findAll() {
        List<OptionalStandardEntity> entities = getAllEntities();
        return entities;
    }

    public List<OptionalStandardCriteriaMap> getAllOptionalStandardCriteriaMap() throws EntityRetrievalException {
        return getAllOptionalStandardCriteriaMapEntities().stream()
                .map(e -> new OptionalStandardCriteriaMap(e))
                .collect(Collectors.toList());
    }

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

        Query query = entityManager.createQuery("SELECT os "
                + "FROM OptionalStandardEntity os "
                + "WHERE (NOT deleted = true) "
                + "AND (os.id = :entityid) ", OptionalStandardEntity.class);
        query.setParameter("entityid", id);
        List<OptionalStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<OptionalStandardEntity> getEntitiesByCitation(String citation) {
        String osQuery = "SELECT os "
                + "FROM OptionalStandardEntity os "
                + "WHERE os.deleted <> true "
                + "AND UPPER(os.citation) = :citation ";
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

    private List<OptionalStandardCriteriaMapEntity> getAllOptionalStandardCriteriaMapEntities() throws EntityRetrievalException {
        return entityManager.createQuery("SELECT DISTINCT osm "
                        + "FROM OptionalStandardCriteriaMapEntity osm "
                        + "JOIN FETCH osm.criteria c "
                        + "JOIN FETCH c.certificationEdition "
                        + "JOIN FETCH osm.optionalStandard os "
                        + "WHERE osm.deleted <> true "
                        + "AND os.deleted <> true ",
                        OptionalStandardCriteriaMapEntity.class)
                .getResultList();
    }
}
