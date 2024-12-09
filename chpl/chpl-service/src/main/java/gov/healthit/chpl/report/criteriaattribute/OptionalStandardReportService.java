package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OptionalStandardReportService {
    private OptionalStandardReportDao optionalStandardReportDao;

    @Autowired
    public OptionalStandardReportService(OptionalStandardReportDao optionalStandardReportDao) {
        this.optionalStandardReportDao = optionalStandardReportDao;
    }

    @Transactional
    public List<OptionalStandardReport> getOptionalStandardReports() {
        return optionalStandardReportDao.getOptionalStandardReports();
    }

    @Transactional
    public List<OptionalStandardListingReport> getOptionalStandardListingReports() {
        return optionalStandardReportDao.getOptionalStandardListingReports();
    }

}
