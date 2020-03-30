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
import gov.healthit.chpl.dto.TestDataCriteriaMapDTO;
import gov.healthit.chpl.dto.TestDataDTO;
import gov.healthit.chpl.entity.TestDataCriteriaMapEntity;
import gov.healthit.chpl.entity.TestDataEntity;

@Repository("testDataDAO")
public class TestDataDAO extends BaseDAOImpl {
    private static Logger LOGGER = LogManager.getLogger(TestDataDAO.class);


    public List<TestDataDTO> getByCriterionId(Long criterionId) {
        Set<TestDataEntity> entities = getTestDataByCertificationCriteria(criterionId);
        List<TestDataDTO> dtos = new ArrayList<TestDataDTO>();

        for (TestDataEntity entity : entities) {
            TestDataDTO dto = new TestDataDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }


    public TestDataDTO getByCriterionAndValue(Long criterionId, String value) {
        TestDataEntity entity = getTestDataByCertificationCriteriaAndValue(criterionId, value);
        if (entity == null) {
            return null;
        }
        return new TestDataDTO(entity);
    }


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

    private Set<TestDataEntity> getTestDataByCertificationCriteria(Long criterionId) {
        Query query = entityManager.createQuery("SELECT tdMap "
                + "FROM TestDataCriteriaMapEntity tdMap "
                + "JOIN FETCH tdMap.testData td "
                + "JOIN FETCH tdMap.certificationCriterion cce "
                + "JOIN FETCH cce.certificationEdition "
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
                + "JOIN FETCH cce.certificationEdition "
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
