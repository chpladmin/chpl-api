package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dto.TestProcedureCriteriaMapDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.entity.TestProcedureCriteriaMapEntity;
import gov.healthit.chpl.entity.TestProcedureEntity;

@Repository("testProcedureDAO")
public class TestProcedureDAOImpl extends BaseDAOImpl implements TestProcedureDAO {
    private static final Logger LOGGER = LogManager.getLogger(TestProcedureDAOImpl.class);
    @Autowired
    MessageSource messageSource;

    @Override
    public List<TestProcedureDTO> getByCriteriaNumber(String criteriaNumber) {
        Set<TestProcedureEntity> entities = getTestProcedureByCertificationCriteria(criteriaNumber);
        List<TestProcedureDTO> dtos = new ArrayList<TestProcedureDTO>();

        for (TestProcedureEntity entity : entities) {
            TestProcedureDTO dto = new TestProcedureDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    @Override
    public TestProcedureDTO getByCriteriaNumberAndValue(String criteriaNumber, String value) {
        TestProcedureEntity entity = getTestProcedureByCertificationCriteriaAndValue(criteriaNumber, value);
        if (entity == null) {
            return null;
        }
        return new TestProcedureDTO(entity);
    }

    @Override
    public List<TestProcedureCriteriaMapDTO> findAllWithMappedCriteria() {

        List<TestProcedureCriteriaMapEntity> entities =
                entityManager.createQuery("SELECT tpMap "
                        + "FROM TestProcedureCriteriaMapEntity tpMap "
                        + "LEFT JOIN FETCH tpMap.testProcedure tp "
                        + "LEFT JOIN FETCH tpMap.certificationCriterion cce "
                        + "LEFT JOIN FETCH cce.certificationEdition "
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
    private Set<TestProcedureEntity> getTestProcedureByCertificationCriteria(final String criteriaNumber) {
        Query query = entityManager.createQuery("SELECT tpMap "
                + "FROM TestProcedureCriteriaMapEntity tpMap "
                + "JOIN FETCH tpMap.testProcedure tp "
                + "JOIN FETCH tpMap.certificationCriterion cce "
                + "JOIN FETCH cce.certificationEdition "
                + "WHERE tpMap.deleted <> true "
                + "AND tp.deleted <> true "
                + "AND (UPPER(cce.number) = :criteriaNumber)",
                TestProcedureCriteriaMapEntity.class);
        query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
        List<TestProcedureCriteriaMapEntity> results = query.getResultList();

        Set<TestProcedureEntity> tps = new HashSet<TestProcedureEntity>();
        for (TestProcedureCriteriaMapEntity result : results) {
            tps.add(result.getTestProcedure());
        }
        return tps;
    }

    private TestProcedureEntity getTestProcedureByCertificationCriteriaAndValue(final String criteriaNumber,
            final String value) {
        Query query = entityManager.createQuery("SELECT tpMap "
                + "FROM TestProcedureCriteriaMapEntity tpMap "
                + "JOIN FETCH tpMap.testProcedure tp "
                + "JOIN FETCH tpMap.certificationCriterion cce "
                + "JOIN FETCH cce.certificationEdition "
                + "WHERE tpMap.deleted <> true "
                + "AND tp.deleted <> true "
                + "AND (UPPER(cce.number) = :criteriaNumber) "
                + "AND (UPPER(tp.name) = :value)",
                TestProcedureCriteriaMapEntity.class);
        query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
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
