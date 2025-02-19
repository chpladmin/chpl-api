package gov.healthit.chpl.report.realworldtesting;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
public class RealWorldTestingReportDataService {
    private RealWorldTestingPlanSummaryReportDao realWorldTestingPlanSummaryReportDao;
    private RealWorldTestingResultsSummaryReportDao realWorldTestingResultsSummaryReportDao;

    @Autowired
    public RealWorldTestingReportDataService(RealWorldTestingPlanSummaryReportDao realWorldTestingPlanSummaryReportDao,
            RealWorldTestingResultsSummaryReportDao realWorldTestingResultsSummaryReportDao) {

        this.realWorldTestingPlanSummaryReportDao = realWorldTestingPlanSummaryReportDao;
        this.realWorldTestingResultsSummaryReportDao = realWorldTestingResultsSummaryReportDao;
    }

    @Transactional
    public List<RealWorldTestingSummaryReport> getRealWorldTestingPlanSummaryReports() {
        Optional<Long> rwtYear =  realWorldTestingPlanSummaryReportDao.getMaxRealWorldTestingYear();
        if (rwtYear.isPresent()) {
            return realWorldTestingPlanSummaryReportDao.getRealWorldTestingReportsByTestingYear(rwtYear.get());
        } else {
            return List.of();
        }
    }

    @Transactional
    public List<RealWorldTestingSummaryReport> getRealWorldTestingResultsSummaryReports() {
        Optional<Long> rwtYear =  realWorldTestingResultsSummaryReportDao.getMaxRealWorldTestingYear();
        if (rwtYear.isPresent()) {
            return realWorldTestingResultsSummaryReportDao.getRealWorldTestingReportsByTestingYear(rwtYear.get());
        } else {
            return List.of();
        }
    }
}
