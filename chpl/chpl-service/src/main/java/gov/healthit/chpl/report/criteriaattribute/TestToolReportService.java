package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TestToolReportService {
    private TestToolReportDao testToolReportDao;

    public TestToolReportService(TestToolReportDao testToolReportDao) {
        this.testToolReportDao = testToolReportDao;
    }

    // Criteria, TestTool, Listing Count
    public List<TestToolReport> getTestToolReports() {
        return testToolReportDao.getTestToolReports();
    }
}
