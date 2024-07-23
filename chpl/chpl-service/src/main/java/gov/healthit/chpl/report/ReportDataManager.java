package gov.healthit.chpl.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReportService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ReportDataManager {

    private CriteriaMigrationReportService criteriaMigrationReportService;

    @Autowired
    public ReportDataManager(CriteriaMigrationReportService criteriaMigrationReportService) {
        this.criteriaMigrationReportService = criteriaMigrationReportService;
    }


    public CriteriaMigrationReport getHti1CriteriaMigrationReport() {
        return criteriaMigrationReportService.getReport(CriteriaMigrationReportService.HTI1_REPORT_ID);
    }
}

