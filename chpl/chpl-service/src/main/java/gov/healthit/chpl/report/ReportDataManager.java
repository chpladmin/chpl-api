package gov.healthit.chpl.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportService;
import gov.healthit.chpl.report.developer.DeveloperReportsService;
import gov.healthit.chpl.report.developer.UniqueDeveloperCount;
import gov.healthit.chpl.scheduler.job.summarystatistics.data.CertificationBodyStatistic;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ReportDataManager {

    private CriteriaMigrationReportService criteriaMigrationReportService;
    private DeveloperReportsService developerReportsService;

    @Autowired
    public ReportDataManager(CriteriaMigrationReportService criteriaMigrationReportService, DeveloperReportsService developerReportsService) {
        this.criteriaMigrationReportService = criteriaMigrationReportService;
        this.developerReportsService = developerReportsService;
    }

    public CriteriaMigrationReport getHti1CriteriaMigrationReport() {
        return criteriaMigrationReportService.getReport(CriteriaMigrationReportService.HTI1_REPORT_ID);
    }

    public UniqueDeveloperCount getUniqueDeveloperCount() {
        return developerReportsService.getUniqueDeveloperCount();
    }

    public List<CertificationBodyStatistic> getDeveloperCountsWithActiveListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithActiveListingsByAcb();
    }

    public List<CertificationBodyStatistic> getDeveloperCountsWithWithdrawnListingsByAcb() {
        return developerReportsService.getDeveloperCountsWithWithdrawnListingsByAcb();
    }
 }

