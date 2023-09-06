package gov.healthit.chpl.conformanceMethod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodCriteriaMapEntity;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("conformanceMethodDAO")
public class ConformanceMethodDAO extends BaseDAOImpl {

    private CertificationCriterionComparator criteriaComparator;

    @Autowired
    public ConformanceMethodDAO(CertificationCriterionComparator criteriaComparator) {
        this.criteriaComparator = criteriaComparator;
    }

    public List<ConformanceMethod> getAllWithCriteria() {
        Query query = entityManager.createQuery("SELECT DISTINCT cm "
                + "FROM ConformanceMethodEntity cm "
                + "LEFT JOIN FETCH cm.criteria crit "
                + "LEFT JOIN FETCH crit.certificationEdition "
                + "LEFT JOIN FETCH crit.rule "
                + "WHERE cm.deleted <> true ",
                ConformanceMethodEntity.class);
        List<ConformanceMethodEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .peek(cm -> cm.getCriteria().sort(criteriaComparator))
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
        List<ConformanceMethodCriteriaMapEntity> entities = entityManager.createQuery("SELECT DISTINCT mapping "
                + "FROM ConformanceMethodCriteriaMapEntity mapping "
                + "JOIN FETCH mapping.certificationCriterion mappingCrit "
                + "LEFT JOIN FETCH mappingCrit.certificationEdition "
                + "LEFT JOIN FETCH mappingCrit.rule "
                + "JOIN FETCH mapping.conformanceMethod mappingCm "
                + "JOIN FETCH mappingCm.criteria mappingCmCrit "
                + "LEFT JOIN FETCH mappingCmCrit.certificationEdition "
                + "LEFT JOIN FETCH mappingCmCrit.rule "
                + "WHERE mapping.deleted <> true "
                + "AND mappingCm.deleted <> true ",
                ConformanceMethodCriteriaMapEntity.class)
        .getResultList();

        return entities.stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
    }
}
