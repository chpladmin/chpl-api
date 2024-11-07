package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class StandardReportService {
    private StandardReportDao standardReportDao;

    public StandardReportService(StandardReportDao standardReportDao) {
        this.standardReportDao = standardReportDao;
    }

    // Criteria, TestTool, Listing Count
    public List<StandardReport> getStandardReports() {
        return standardReportDao.getStandardReports();
    }

    public List<StandardListingReport> getStandardListingReports() {
        return standardReportDao.getStandardListingReports();
    }

}
