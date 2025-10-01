package gov.healthit.chpl.testdata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Repository("testDataDAO")
public class TestDataDAO extends BaseDAOImpl {

    public List<TestData> getAll() {
        return getAllEntities().stream()
                .map(entity -> entity.toDomainWithCriteria())
                .toList();
    }

    public List<TestData> getByCriterionId(Long criterionId) {
        Set<TestDataEntity> entities = getTestDataByCertificationCriteria(criterionId);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public TestData getByCriterionAndValue(Long criterionId, String value) {
        TestDataEntity entity = getTestDataByCertificationCriteriaAndValue(criterionId, value);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    private List<TestDataEntity> getAllEntities() {
        return entityManager.createQuery("SELECT DISTINCT td "
                + "FROM TestDataEntity td "
                + "LEFT JOIN FETCH td.criteria crit "
                + "LEFT JOIN FETCH crit.certificationEdition "
                + "LEFT JOIN FETCH crit.rule "
                + "WHERE td.deleted <> true ", TestDataEntity.class)
                .getResultList();
    }

    private Set<TestDataEntity> getTestDataByCertificationCriteria(Long criterionId) {
        Query query = entityManager.createQuery("SELECT tdMap "
                + "FROM TestDataCriteriaMapEntity tdMap "
                + "JOIN FETCH tdMap.testData td "
                + "JOIN FETCH tdMap.certificationCriterion cce "
                + "LEFT JOIN FETCH cce.certificationEdition "
                + "LEFT JOIN FETCH cce.rule "
                + "WHERE tdMap.deleted <> true "
                + "AND td.deleted <> true "
                + "AND cce.id = :criterionId",
                TestDataCriteriaMapEntity.class);
        query.setParameter("criterionId", criterionId);
        List<TestDataCriteriaMapEntity> results = query.getResultList();
        Set<TestDataEntity> tds = new HashSet<TestDataEntity>();
        for (TestDataCriteriaMapEntity result : results) {
            tds.add(result.getTestData());
        }
        return tds;
    }

    private TestDataEntity getTestDataByCertificationCriteriaAndValue(Long criterionId, String value) {
        Query query = entityManager.createQuery("SELECT tdMap "
                + "FROM TestDataCriteriaMapEntity tdMap "
                + "JOIN FETCH tdMap.testData td "
                + "JOIN FETCH tdMap.certificationCriterion cce "
                + "LEFT JOIN FETCH cce.certificationEdition "
                + "LEFT JOIN FETCH cce.rule "
                + "WHERE tdMap.deleted <> true "
                + "AND td.deleted <> true "
                + "AND cce.id = :criterionId "
                + "AND (UPPER(td.name) = :value)",
                TestDataCriteriaMapEntity.class);
        query.setParameter("criterionId", criterionId);
        query.setParameter("value", value.trim().toUpperCase());

        List<TestDataCriteriaMapEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        List<TestDataEntity> tds = new ArrayList<TestDataEntity>();
        for (TestDataCriteriaMapEntity result : results) {
            tds.add(result.getTestData());
        }
        return tds.get(0);
    }
}
