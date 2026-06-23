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
    public List<RealWorldTestingSummaryByAcbReport> getRealWorldTestingPlanSummaryByAcbReports() {
        Optional<Long> rwtYear =  realWorldTestingPlanSummaryReportDao.getMaxRealWorldTestingYearForAcbSummary();
        if (rwtYear.isPresent()) {
            return realWorldTestingPlanSummaryReportDao.getRealWorldTestingSummaryByAcbReportsByTestingYear(rwtYear.get());
        } else {
            return List.of();
        }
    }

    @Transactional
    public List<RealWorldTestingSummaryByAcbReport> getRealWorldTestingResultsSummaryByAcbReports() {
        Optional<Long> rwtYear =  realWorldTestingResultsSummaryReportDao.getMaxRealWorldTestingYearForAcbSummary();
        if (rwtYear.isPresent()) {
            return realWorldTestingResultsSummaryReportDao.getRealWorldTestingSummaryByAcbReportsByTestingYear(rwtYear.get());
        } else {
            return List.of();
        }
    }

    @Transactional
    public List<RealWorldTestingSummaryByDeveloperReport> getRealWorldTestingResultsSummaryByDeveloperReports() {
        Optional<Long> rwtYear =  realWorldTestingResultsSummaryReportDao.getMaxRealWorldTestingYearForDeveloperSummary();
        if (rwtYear.isPresent()) {
            return realWorldTestingResultsSummaryReportDao.getRealWorldTestingSummaryByDeveloperReportsByTestingYear(rwtYear.get());
        } else {
            return List.of();
        }
    }
}
