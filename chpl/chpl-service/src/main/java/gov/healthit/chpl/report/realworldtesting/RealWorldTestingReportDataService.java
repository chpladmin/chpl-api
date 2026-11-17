package gov.healthit.chpl.report.realworldtesting;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;

@Component
public class RealWorldTestingReportDataService {
    private RealWorldTestingResultsSummaryReportPreHti5Dao realWorldTestingResultsSummaryReportPreHti5Dao;
    private RealWorldTestingResultsSummaryReportDao realWorldTestingResultsSummaryReportDao;

    @Autowired
    public RealWorldTestingReportDataService(RealWorldTestingResultsSummaryReportPreHti5Dao realWorldTestingResultsSummaryReportPreHti5Dao,
            RealWorldTestingResultsSummaryReportDao realWorldTestingResultsSummaryReportDao) {
        this.realWorldTestingResultsSummaryReportPreHti5Dao = realWorldTestingResultsSummaryReportPreHti5Dao;
        this.realWorldTestingResultsSummaryReportDao = realWorldTestingResultsSummaryReportDao;
    }

    @Deprecated
    @Transactional
    public List<RealWorldTestingSummaryByAcbReportPreHti5> getRealWorldTestingResultsSummaryByAcbReportsPreHti5() {
        Optional<Long> rwtYear =  realWorldTestingResultsSummaryReportPreHti5Dao.getMaxRealWorldTestingYearForAcbSummary();
        if (rwtYear.isPresent()) {
            return realWorldTestingResultsSummaryReportPreHti5Dao.getRealWorldTestingSummaryByAcbReportsByTestingYear(rwtYear.get());
        } else {
            return List.of();
        }
    }

    @Deprecated
    @Transactional
    public List<RealWorldTestingSummaryByDeveloperReportPreHti5> getRealWorldTestingResultsSummaryByDeveloperReportsPreHti5() {
        Optional<Long> rwtYear =  realWorldTestingResultsSummaryReportPreHti5Dao.getMaxRealWorldTestingYearForDeveloperSummary();
        if (rwtYear.isPresent()) {
            return realWorldTestingResultsSummaryReportPreHti5Dao.getRealWorldTestingSummaryByDeveloperReportsByTestingYear(rwtYear.get());
        } else {
            return List.of();
        }
    }

    @Transactional
    public List<RealWorldTestingSummaryByAcbReport> getRealWorldTestingResultsSummariesByAcbReports() {
        Optional<Long> rwtYear =  realWorldTestingResultsSummaryReportDao.getMaxRealWorldTestingYearForAcbSummary();
        if (rwtYear.isPresent()) {
            return realWorldTestingResultsSummaryReportDao.getRealWorldTestingSummaryByAcbReportsByTestingYear(rwtYear.get());
        } else {
            return List.of();
        }
    }

    @Transactional
    public List<RealWorldTestingSummaryByDeveloperReport> getRealWorldTestingResultsSummariesByDeveloperReports() {
        Optional<Long> rwtYear =  realWorldTestingResultsSummaryReportDao.getMaxRealWorldTestingYearForDeveloperSummary();
        if (rwtYear.isPresent()) {
            return realWorldTestingResultsSummaryReportDao.getRealWorldTestingSummaryByDeveloperReportsByTestingYear(rwtYear.get());
        } else {
            return List.of();
        }
    }
}
