package gov.healthit.chpl.optionalStandard;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.optionalStandard.entity.OptionalStandardCriteriaMapEntity;
import gov.healthit.chpl.optionalStandard.entity.OptionalStandardEntity;

@Repository("optionalStandardDAO")
public class OptionalStandardDAO extends BaseDAOImpl {

    private CertificationCriterionComparator criteriaComparator;

    @Autowired
    public OptionalStandardDAO(CertificationCriterionComparator criteriaComparator) {
        this.criteriaComparator = criteriaComparator;
    }

    public List<OptionalStandard> getAll() {
        List<OptionalStandardEntity> entities = entityManager
                .createQuery("SELECT DISTINCT os "
                        + "FROM OptionalStandardEntity os "
                        + "LEFT JOIN FETCH os.criteria crit "
                        + "LEFT JOIN FETCH crit.certificationEdition "
                        + "LEFT JOIN FETCH crit.rule "
                        + "WHERE (NOT os.deleted = true) ", OptionalStandardEntity.class)
                .getResultList();
        return entities.stream()
                .map(e -> e.toDomain())
                .peek(os -> os.getCriteria().sort(criteriaComparator))
                .collect(Collectors.toList());
    }

    public List<OptionalStandardCriteriaMap> getAllOptionalStandardCriteriaMap() throws EntityRetrievalException {
        return getAllOptionalStandardCriteriaMapEntities().stream()
                .map(e -> e.toDomain())
                .peek(map -> map.getOptionalStandard().getCriteria().sort(criteriaComparator))
                .collect(Collectors.toList());
    }

    public OptionalStandard getByCitation(String citation) {
        List<OptionalStandardEntity> entities = getEntitiesByCitation(citation);
        OptionalStandard obj = null;
        if (entities != null && entities.size() > 0) {
            obj = entities.get(0).toDomain();
            obj.getCriteria().sort(criteriaComparator);
        }
        return obj;
    }

    private List<OptionalStandardEntity> getEntitiesByCitation(String citation) {
        String osQuery = "SELECT DISTINCT os "
                + "FROM OptionalStandardEntity os "
                + "LEFT JOIN FETCH os.criteria crit "
                + "LEFT JOIN FETCH crit.certificationEdition "
                + "LEFT JOIN FETCH crit.rule "
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
        return entityManager.createQuery("SELECT DISTINCT mapping "
                        + "FROM OptionalStandardCriteriaMapEntity mapping "
                        + "JOIN FETCH mapping.criteria mappingCrit "
                        + "LEFT JOIN FETCH mappingCrit.certificationEdition "
                        + "LEFT JOIN FETCH mappingCrit.rule "
                        + "JOIN FETCH mapping.optionalStandard os "
                        + "LEFT JOIN FETCH os.criteria osCrit "
                        + "LEFT JOIN FETCH osCrit.certificationEdition "
                        + "LEFT JOIN FETCH osCrit.rule "
                        + "WHERE mapping.deleted <> true "
                        + "AND os.deleted <> true ",
                        OptionalStandardCriteriaMapEntity.class)
                .getResultList();
    }
}
