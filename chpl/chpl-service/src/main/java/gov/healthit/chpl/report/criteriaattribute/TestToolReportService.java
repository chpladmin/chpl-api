package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestToolReportService {
    private TestToolReportDao testToolReportDao;

    @Autowired
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
