package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StandardReportService {
    private StandardReportDao standardReportDao;

    @Autowired
    public StandardReportService(StandardReportDao standardReportDao) {
        this.standardReportDao = standardReportDao;
    }

    @Transactional
    public List<StandardReport> getStandardReports() {
        return standardReportDao.getStandardReports();
    }

    @Transactional
    public List<StandardListingReport> getStandardListingReports() {
        return standardReportDao.getStandardListingReports();
    }

}
