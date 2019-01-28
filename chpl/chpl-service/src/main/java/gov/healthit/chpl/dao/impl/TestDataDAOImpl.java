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

import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dto.TestDataCriteriaMapDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.entity.TestDataCriteriaMapEntity;
import gov.healthit.chpl.entity.TestDataEntity;

@Repository("testDataDAO")
public class TestDataDAOImpl extends BaseDAOImpl implements TestDataDAO {
    private static final Logger LOGGER = LogManager.getLogger(TestDataDAOImpl.class);
    @Autowired
    MessageSource messageSource;

    @Override
    public List<TestDataDTO> getByCriteriaNumber(String criteriaNumber) {
        Set<TestDataEntity> entities = getTestDataByCertificationCriteria(criteriaNumber);
        List<TestDataDTO> dtos = new ArrayList<TestDataDTO>();

        for (TestDataEntity entity : entities) {
            TestDataDTO dto = new TestDataDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    @Override
    public TestDataDTO getByCriteriaNumberAndValue(String criteriaNumber, String value) {
        TestDataEntity entity = getTestDataByCertificationCriteriaAndValue(criteriaNumber, value);
        if (entity == null) {
            return null;
        }
        return new TestDataDTO(entity);
    }

    @Override
    public List<TestDataCriteriaMapDTO> findAllWithMappedCriteria() {

        List<TestDataCriteriaMapEntity> entities =
                entityManager.createQuery("SELECT tdMap "
                        + "FROM TestDataCriteriaMapEntity tdMap "
                        + "JOIN FETCH tdMap.testData td "
                        + "JOIN FETCH tdMap.certificationCriterion cce "
                        + "JOIN FETCH cce.certificationEdition "
                        + "WHERE tdMap.deleted <> true "
                        + "AND td.deleted <> true ",
                        TestDataCriteriaMapEntity.class).getResultList();
        List<TestDataCriteriaMapDTO> dtos = new ArrayList<TestDataCriteriaMapDTO>();

        for (TestDataCriteriaMapEntity entity : entities) {
            TestDataCriteriaMapDTO dto = new TestDataCriteriaMapDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    private Set<TestDataEntity> getTestDataByCertificationCriteria(final String criteriaNumber) {
        Query query = entityManager.createQuery("SELECT tdMap "
                + "FROM TestDataCriteriaMapEntity tdMap "
                + "JOIN FETCH tdMap.testData td "
                + "JOIN FETCH tdMap.certificationCriterion cce "
                + "JOIN FETCH cce.certificationEdition "
                + "WHERE tdMap.deleted <> true "
                + "AND td.deleted <> true "
                + "AND (UPPER(cce.number) = :criteriaNumber)",
                TestDataCriteriaMapEntity.class);
        query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
        List<TestDataCriteriaMapEntity> results = query.getResultList();
        Set<TestDataEntity> tds = new HashSet<TestDataEntity>();
        for (TestDataCriteriaMapEntity result : results) {
            tds.add(result.getTestData());
        }
        return tds;
    }

    private TestDataEntity getTestDataByCertificationCriteriaAndValue(final String criteriaNumber, final String value) {
        Query query = entityManager.createQuery("SELECT tdMap "
                + "FROM TestDataCriteriaMapEntity tdMap "
                + "JOIN FETCH tdMap.testData td "
                + "JOIN FETCH tdMap.certificationCriterion cce "
                + "JOIN FETCH cce.certificationEdition "
                + "WHERE tdMap.deleted <> true "
                + "AND td.deleted <> true "
                + "AND (UPPER(cce.number) = :criteriaNumber) "
                + "AND (UPPER(td.name) = :value)",
                TestDataCriteriaMapEntity.class);
        query.setParameter("criteriaNumber", criteriaNumber.trim().toUpperCase());
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
