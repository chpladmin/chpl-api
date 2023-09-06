package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.TestProcedureCriteriaMapDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.entity.TestProcedureCriteriaMapEntity;
import gov.healthit.chpl.entity.TestProcedureEntity;

@Repository("testProcedureDAO")
public class TestProcedureDAO extends BaseDAOImpl {
    private static Logger LOGGER = LogManager.getLogger(TestProcedureDAO.class);

    public List<TestProcedureDTO> getByCriterionId(Long criterionId) {
        Set<TestProcedureEntity> entities = getTestProcedureByCertificationCriteria(criterionId);
        List<TestProcedureDTO> dtos = new ArrayList<TestProcedureDTO>();

        for (TestProcedureEntity entity : entities) {
            TestProcedureDTO dto = new TestProcedureDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }


    public TestProcedureDTO getByCriterionIdAndValue(Long criterionId, String value) {
        TestProcedureEntity entity = getTestProcedureByCertificationCriteriaAndValue(criterionId, value);
        if (entity == null) {
            return null;
        }
        return new TestProcedureDTO(entity);
    }


    public List<TestProcedureCriteriaMapDTO> findAllWithMappedCriteria() {

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
        List<TestProcedureCriteriaMapDTO> dtos = new ArrayList<TestProcedureCriteriaMapDTO>();

        for (TestProcedureCriteriaMapEntity entity : entities) {
            TestProcedureCriteriaMapDTO dto = new TestProcedureCriteriaMapDTO(entity);
            dtos.add(dto);
        }
        return dtos;
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
