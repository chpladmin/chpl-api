package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestDataReportService {
    private TestDataReportDao testDataReportDao;

    @Autowired
    public TestDataReportService(TestDataReportDao testDataReportDao) {
        this.testDataReportDao = testDataReportDao;
    }

    @Transactional
    public List<TestDataReport> getTestDataReports() {
        return testDataReportDao.getTestDataReports();
    }

    @Transactional
    public List<TestDataListingReport> getTestDataListingReports() {
        return testDataReportDao.getTestDataListingReports();
    }

}
