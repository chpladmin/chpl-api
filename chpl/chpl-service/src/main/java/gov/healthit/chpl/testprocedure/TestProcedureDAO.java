package gov.healthit.chpl.testprocedure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository("testProcedureDAO")
public class TestProcedureDAO extends BaseDAOImpl {

    public List<TestProcedure> getAll() {
        List<TestProcedureEntity> entities =
                entityManager.createQuery("SELECT tp "
                        + "FROM TestProcedureEntity tp "
                        + "WHERE tp.deleted <> true ",
                        TestProcedureEntity.class).getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public List<TestProcedure> getByCriterionId(Long criterionId) {
        Set<TestProcedureEntity> entities = getTestProcedureByCertificationCriteria(criterionId);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public TestProcedure getByCriterionIdAndValue(Long criterionId, String value) {
        TestProcedureEntity entity = getTestProcedureByCertificationCriteriaAndValue(criterionId, value);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public List<TestProcedureCriteriaMap> findAllWithMappedCriteria() {
        List<TestProcedureCriteriaMapEntity> entities =
                entityManager.createQuery("SELECT tpMap "
                        + "FROM TestProcedureCriteriaMapEntity tpMap "
                        + "LEFT JOIN FETCH tpMap.testProcedure tp "
                        + "LEFT JOIN FETCH tpMap.certificationCriterion cce "
                        + "LEFT JOIN FETCH cce.certificationEdition "
                        + "LEFT JOIN FETCH cce.rule "
                        + "WHERE tpMap.deleted <> true "
                        + "AND tp.deleted <> true ",
                        TestProcedureCriteriaMapEntity.class).getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    private Set<TestProcedureEntity> getTestProcedureByCertificationCriteria(Long criterionId) {
        Query query = entityManager.createQuery("SELECT tpMap "
                + "FROM TestProcedureCriteriaMapEntity tpMap "
                + "JOIN FETCH tpMap.testProcedure tp "
                + "JOIN FETCH tpMap.certificationCriterion cce "
                + "LEFT JOIN FETCH cce.certificationEdition "
                + "LEFT JOIN FETCH cce.rule "
                + "WHERE tpMap.deleted <> true "
                + "AND tp.deleted <> true "
                + "AND cce.id = :criterionId",
                TestProcedureCriteriaMapEntity.class);
        query.setParameter("criterionId", criterionId);
        List<TestProcedureCriteriaMapEntity> results = query.getResultList();

        Set<TestProcedureEntity> tps = new HashSet<TestProcedureEntity>();
        for (TestProcedureCriteriaMapEntity result : results) {
            tps.add(result.getTestProcedure());
        }
        return tps;
    }

    private TestProcedureEntity getTestProcedureByCertificationCriteriaAndValue(Long criterionId,
            String value) {
        Query query = entityManager.createQuery("SELECT tpMap "
                + "FROM TestProcedureCriteriaMapEntity tpMap "
                + "JOIN FETCH tpMap.testProcedure tp "
                + "JOIN FETCH tpMap.certificationCriterion cce "
                + "LEFT JOIN FETCH cce.certificationEdition "
                + "LEFT JOIN FETCH cce.rule "
                + "WHERE tpMap.deleted <> true "
                + "AND tp.deleted <> true "
                + "AND cce.id = :criterionId "
                + "AND (UPPER(tp.name) = :value)",
                TestProcedureCriteriaMapEntity.class);
        query.setParameter("criterionId", criterionId);
        query.setParameter("value", value.trim().toUpperCase());

        List<TestProcedureCriteriaMapEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        List<TestProcedureEntity> tps = new ArrayList<TestProcedureEntity>();
        for (TestProcedureCriteriaMapEntity result : results) {
            tps.add(result.getTestProcedure());
        }
        return tps.get(0);
    }
}
