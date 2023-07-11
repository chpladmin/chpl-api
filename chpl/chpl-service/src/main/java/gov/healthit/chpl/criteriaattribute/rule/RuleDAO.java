package gov.healthit.chpl.criteriaattribute.rule;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Component
public class RuleDAO extends BaseDAOImpl {
    public RuleEntity getRuleEntityById(Long id) {
        RuleEntity entity = null;
        Query query = entityManager.createQuery(
                "FROM RuleEntity "
                + "WHERE (NOT deleted = true) "
                + "AND (id = :entityid) ", RuleEntity.class);
        query.setParameter("entityid", id);
        List<RuleEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }
}
