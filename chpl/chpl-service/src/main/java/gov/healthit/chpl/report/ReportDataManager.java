package gov.healthit.chpl.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.report.criteriaattribute.TestToolListingReport;
import gov.healthit.chpl.report.criteriaattribute.TestToolReport;
import gov.healthit.chpl.report.criteriaattribute.TestToolReportService;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportDenormalized;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportService;
import gov.healthit.chpl.report.developer.DeveloperReportsService;
import gov.healthit.chpl.report.developer.UniqueDeveloperCount;
import gov.healthit.chpl.report.servicebaseurllistreport.ServiceBaseUrlListReportService;
import gov.healthit.chpl.report.servicebaseurllistreport.UrlUptimeMonitorEx;
import gov.healthit.chpl.report.surveillance.CapCounts;
import gov.healthit.chpl.report.surveillance.NonconformityCounts;
import gov.healthit.chpl.report.surveillance.SurveillanceActivityCounts;
import gov.healthit.chpl.report.surveillance.SurveillanceReportsService;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ReportDataManager {
    private final Object lock = new Object();

    private CriteriaMigrationReportService criteriaMigrationReportService;
    private SurveillanceReportsService surveillanceReportsService;
    private DeveloperReportsService developerReportsService;
    private TestToolReportService testToolReportService;
    private ServiceBaseUrlListReportService serviceBaseUrlListReportService;

    @Autowired
    public ReportDataManager(CriteriaMigrationReportService criteriaMigrationReportService, DeveloperReportsService developerReportsService,
            SurveillanceReportsService surveillanceReportsService, TestToolReportService testToolReportService,
            ServiceBaseUrlListReportService serviceBaseUrlListReportService) {
        this.criteriaMigrationReportService = criteriaMigrationReportService;
        this.developerReportsService = developerReportsService;
        this.surveillanceReportsService = surveillanceReportsService;
        this.testToolReportService = testToolReportService;
        this.serviceBaseUrlListReportService = serviceBaseUrlListReportService;
    }

    @Synchronized("lock")
    public List<CriteriaMigrationReportDenormalized> getHti1CriteriaMigrationReport() {
        return criteriaMigrationReportService.getHtiReportData(CriteriaMigrationReportService.HTI1_REPORT_ID);
    }

    @Synchronized("lock")
    public SurveillanceActivityCounts getSurveillanceActivityCounts() {
        return surveillanceReportsService.getSurveiilanceActivityCounts();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getOpenSurveillanceActivityCountsByAcb() {
        return surveillanceReportsService.getOpenSurveillanceActivityCountsByAcb();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getListingsWithOpenSurveillance() {
        return surveillanceReportsService.getListingsWithOpenSurveillance();
    }

    @Synchronized("lock")
    public NonconformityCounts getNonconformityCounts() {
        return surveillanceReportsService.getNonconformityCounts();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getOpenNonconformityCountsByAcb() {
        return surveillanceReportsService.getOpenNonconformityCountsByAcb();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getListingsWithOpenNonconformity() {
        return surveillanceReportsService.getListingsWithOpenNonconformity();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getOpenCapCountsByAcb() {
        return surveillanceReportsService.getOpenCapCountsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getClosedCapCountsByAcb() {
        return surveillanceReportsService.getClosedCapCountsByAcb();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getListingsWithOpenCap() {
        return surveillanceReportsService.getListingsWithOpenCap();
    }

    @Synchronized("lock")
    public List<ListingSearchResult> getListingsWithClosedCap() {
        return surveillanceReportsService.getListingsWithClosedCap();
    }

    @Synchronized("lock")
    public CapCounts getCapCounts() {
        return surveillanceReportsService.getCapCounts();
    }

    @Synchronized("lock")
    public UniqueDeveloperCount getUniqueDeveloperCount() {
        return developerReportsService.getUniqueDeveloperCount();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getDeveloperCountsWithActiveListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithActiveListingsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getDeveloperCountsWithWithdrawnListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithWithdrawnListingsByAcb();
    }

    @Synchronized("lock")
    public List<DeveloperSearchResult> getDevelopersWithWithdrawnListingsByAcb() {
        return developerReportsService.getDevelopersWithWithdrawnListingsByAcb();
    }

    @Synchronized("lock")
    public List<CertificationBodyStatistic> getDeveloperCountsWithSuspendedListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithSuspendedListingsByAcb();
    }

    @Synchronized("lock")
    public List<DeveloperSearchResult> getDevelopersWithSuspendedListingsByAcb() {
        return developerReportsService.getDevelopersWithSuspendedListingsByAcb();
    }

    @Synchronized("lock")
    public List<TestToolReport> getTestToolReports() {
        return testToolReportService.getTestToolReports();
    }

    @Synchronized("lock")
    public List<TestToolListingReport> getTestToolListingReports() {
        return testToolReportService.getTestToolListingReports();
    }

    @Synchronized("lock")
    public List<UrlUptimeMonitorEx> getUrlUptimeMonitors() {
        return serviceBaseUrlListReportService.getUrlUptimeMonitors();
    }
}
