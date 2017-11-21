package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dto.TestProcedureCriteriaMapDTO;
import gov.healthit.chpl.dto.TestProcedureDTO;
import gov.healthit.chpl.entity.TestDataEntity;
import gov.healthit.chpl.entity.TestProcedureCriteriaMapEntity;
import gov.healthit.chpl.entity.TestProcedureEntity;

@Repository("testProcedureDAO")
public class TestProcedureDAOImpl extends BaseDAOImpl implements TestProcedureDAO {
    private static final Logger LOGGER = LogManager.getLogger(TestProcedureDAOImpl.class);
    @Autowired
    MessageSource messageSource;
    
    @Override
    public List<TestProcedureDTO> getByCriteriaNumber(String criteriaNumber) {
        List<TestProcedureEntity> entities = getTestProcedureByCertificationCriteria(criteriaNumber);
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
                        + "JOIN FETCH tpMap.testProcedure tp "
                        + "JOIN FETCH tpMap.certificationCriterion cce "
                        + "JOIN FETCH cce.certificationEdition "
                        + "WHERE tpMap.deleted <> true "
                        + "AND tp.deleted <> true "
                        , TestProcedureCriteriaMapEntity.class).getResultList();
        List<TestProcedureCriteriaMapDTO> dtos = new ArrayList<TestProcedureCriteriaMapDTO>();

        for (TestProcedureCriteriaMapEntity entity : entities) {
            TestProcedureCriteriaMapDTO dto = new TestProcedureCriteriaMapDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }
    private List<TestProcedureEntity> getTestProcedureByCertificationCriteria(String criteriaNumber) {
        Query query = entityManager.createQuery("SELECT tp "
                + "FROM TestProcedureCriteriaMapEntity tpMap "
                + "JOIN FETCH tpMap.testProcedure tp "
                + "JOIN FETCH tpMap.certificationCriterion cce "
                + "JOIN FETCH cce.certificationEdition "
                + "WHERE tpMap.deleted <> true "
                + "AND tp.deleted <> true "
                + "AND (UPPER(cce.number) = :criteriaNumber)"
                , TestProcedureEntity.class);
        query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
        List<TestProcedureEntity> result = query.getResultList();
        return result;
    }

    private TestProcedureEntity getTestProcedureByCertificationCriteriaAndValue(String criteriaNumber, String value) {
        Query query = entityManager.createQuery("SELECT tp "
                + "FROM TestProcedureCriteriaMapEntity tpMap "
                + "JOIN FETCH tpMap.testProcedure tp "
                + "JOIN FETCH tpMap.certificationCriterion cce "
                + "JOIN FETCH cce.certificationEdition "
                + "WHERE tpMap.deleted <> true "
                + "AND tp.deleted <> true "
                + "AND (UPPER(cce.number) = :criteriaNumber) "
                + "AND (UPPER(mme.value) = :value)"
                , TestDataEntity.class);
        query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
        query.setParameter("value", value.trim().toUpperCase());
        
        List<TestProcedureEntity> result = query.getResultList();
        if (result == null || result.size() == 0) {
            return null;
        }
        return result.get(0);
    }
}
