package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.testtool.TestTool;
import gov.healthit.chpl.testtool.TestToolDAO;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class TestToolReportDao extends BaseDAOImpl {

    private TestToolDAO testToolDao;
    private CertificationCriterionDAO certificationCriterionDao;

    @Autowired
    public TestToolReportDao(TestToolDAO testToolDao, CertificationCriterionDAO certificationCriterionDao) {
        this.testToolDao = testToolDao;
        this.certificationCriterionDao = certificationCriterionDao;
    }

    private TestTool getTestTool(Long id) {
        return testToolDao.getById(id);
    }

    private CertificationCriterion getCertificationCriterion(Long id) {
        try {
            return certificationCriterionDao.getById(id);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve criterion id: {}", id, e);
            return null;
        }
    }

    public List<TestToolReport> getTestToolReports() {
        String hql = "SELECT cc.id as certificationCriterionId, tt.id as testToolId, count(*) as testToolCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultTestToolEntity crtt, "
                + "TestToolEntity tt "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crtt.certificationResultId "
                + "AND crtt.testTool.id = tt.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crtt.deleted = false "
                + "AND cpd.deleted = false "
                + "GROUP BY cc.id, tt.id ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> TestToolReport.builder()
                        .criterion(getCertificationCriterion((Long) result[0]))
                        .testTool(getTestTool((Long) result[1]))
                        .count((Long) result[2])
                        .build())
                .toList();
    }

}
