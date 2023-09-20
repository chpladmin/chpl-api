package gov.healthit.chpl.conformanceMethod;

import java.util.List;
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
                .map(entity -> entity.toDomainWithCriteria())
                .peek(cm -> cm.getCriteria().sort(criteriaComparator))
                .collect(Collectors.toList());
    }

    public List<ConformanceMethodCriteriaMap> getAllConformanceMethodCriteriaMap() throws EntityRetrievalException {
        List<ConformanceMethodCriteriaMapEntity> entities = entityManager.createQuery("SELECT DISTINCT mapping "
                + "FROM ConformanceMethodCriteriaMapEntity mapping "
                + "JOIN FETCH mapping.certificationCriterion mappingCrit "
                + "LEFT JOIN FETCH mappingCrit.certificationEdition "
                + "LEFT JOIN FETCH mappingCrit.rule "
                + "JOIN FETCH mapping.conformanceMethod mappingCm "
                + "WHERE mapping.deleted <> true "
                + "AND mappingCm.deleted <> true ",
                ConformanceMethodCriteriaMapEntity.class)
        .getResultList();

        return entities.stream()
                .map(e -> e.toDomain())
                .collect(Collectors.toList());
    }
}
