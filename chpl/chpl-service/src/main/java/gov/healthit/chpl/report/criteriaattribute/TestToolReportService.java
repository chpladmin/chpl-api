package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestToolReportService {
    private TestToolReportDao testToolReportDao;

    public TestToolReportService(TestToolReportDao testToolReportDao) {
        this.testToolReportDao = testToolReportDao;
    }

    @Transactional
    public List<TestToolReport> getTestToolReports() {
        return testToolReportDao.getTestToolReports();
    }

    @Transactional
    public List<TestToolListingReport> getTestToolListingReports() {
        return testToolReportDao.getTestToolListingReports();
    }
}
