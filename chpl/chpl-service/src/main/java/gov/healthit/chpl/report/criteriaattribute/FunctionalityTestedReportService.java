package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FunctionalityTestedReportService {
    private FunctionalityTestedReportDao functionalityTestedReportDao;

    @Autowired
    public FunctionalityTestedReportService(FunctionalityTestedReportDao functionalityTestedReportDao) {
        this.functionalityTestedReportDao = functionalityTestedReportDao;
    }

    @Transactional
    public List<FunctionalityTestedReport> getFunctionalityTestedReports() {
        return functionalityTestedReportDao.getFunctionalityTestedReports();
    }

    @Transactional
    public List<FunctionalityTestedListingReport> getFunctionalityTestedListingReports() {
        return functionalityTestedReportDao.getFunctionalityTestedListingReports();
    }

}
